package com.saaresto.kitchen.kitchenadmin.controller

import com.saaresto.kitchen.kitchenadmin.dto.BookingRequest
import com.saaresto.kitchen.kitchenadmin.model.Booking
import com.saaresto.kitchen.kitchenadmin.model.BookingStatus
import com.saaresto.kitchen.kitchenadmin.service.BookingService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Controller
@RequestMapping("/admin/bookings")
class AdminBookingController(private val bookingService: BookingService) {

    @GetMapping
    fun bookings(
        @RequestParam(required = false, defaultValue = "pending") tab: String,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(required = false) tableId: String?,
        @RequestParam(required = false) visitorName: String?,
        @RequestParam(required = false) visitorPhone: String?,
        model: Model
    ): String {
        // Set active tab
        model.addAttribute("activeTab", tab)

        // Load data for pending bookings tab
        if (tab == "pending" || tab == "all") {
            val filterDate = date ?: LocalDate.now()
            val pendingBookings = if (date != null) {
                // Filter by date
                val startOfDay = filterDate.atStartOfDay()
                val endOfDay = filterDate.plusDays(1).atStartOfDay()
                bookingService.getAllBookings().filter { 
                    (it.status == BookingStatus.PENDING || it.status == BookingStatus.WAIT_LIST || it.status == BookingStatus.CALL_AGAIN) && 
                    it.dateTime >= startOfDay && 
                    it.dateTime < endOfDay 
                }
            } else {
                // Show all pending bookings, sorted by createdAt (earliest first)
                bookingService.getPendingBookingsOrderByCreatedAt()
            }

            // Group bookings by status
            val groupedBookings = pendingBookings.groupBy { it.status }

            model.addAttribute("pendingBookings", pendingBookings)
            model.addAttribute("groupedBookings", groupedBookings)
            model.addAttribute("filterDate", filterDate)
        }

        // Load data for today's bookings tab
        if (tab == "today" || tab == "all") {
            // Use the provided date or default to today
            val filterDate = date ?: LocalDate.now()

            // Get bookings for the specified date with filters
            val bookings = bookingService.getBookingsByDateWithFilters(
                filterDate.atStartOfDay(),
                visitorName,
                visitorPhone
            )

            // Filter by table ID if provided
            val filteredBookings = if (!tableId.isNullOrBlank()) {
                bookings.filter { it.tableId == tableId }
            } else {
                bookings
            }

            model.addAttribute("todayBookings", filteredBookings)
            model.addAttribute("filterDate", filterDate)
            model.addAttribute("tableId", tableId)
            model.addAttribute("visitorName", visitorName)
            model.addAttribute("visitorPhone", visitorPhone)
        }

        // Load data for booking history tab
        if (tab == "history" || tab == "all") {
            val start = startDate ?: LocalDate.now().minusMonths(1)
            val end = endDate ?: LocalDate.now()

            // Get all bookings in the date range with filters
            val allBookings = bookingService.getBookingsByDateRangeWithFilters(
                start.atStartOfDay(),
                end.atStartOfDay(),
                visitorName,
                visitorPhone
            )

            // Simple pagination
            val pageSize = 10
            val totalPages = (allBookings.size + pageSize - 1) / pageSize
            val currentPage = page.coerceIn(0, maxOf(0, totalPages - 1))
            val historyBookings = allBookings.drop(currentPage * pageSize).take(pageSize)

            model.addAttribute("historyBookings", historyBookings)
            model.addAttribute("startDate", start)
            model.addAttribute("endDate", end)
            model.addAttribute("currentPage", currentPage)
            model.addAttribute("totalPages", totalPages)
            model.addAttribute("visitorName", visitorName)
            model.addAttribute("visitorPhone", visitorPhone)
        }

        model.addAttribute("content", "admin/bookings :: content")

        return "admin/layout"
    }

    // For backward compatibility, redirect old URLs to the new endpoint
    @GetMapping("/pending")
    fun pendingBookings(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?
    ): String {
        return "redirect:/admin/bookings?tab=pending" + (date?.let { "&date=$it" } ?: "")
    }

    @GetMapping("/today")
    fun todayBookings(@RequestParam(required = false) tableId: String?): String {
        return "redirect:/admin/bookings?tab=today" + (tableId?.let { "&tableId=$it" } ?: "")
    }

    @GetMapping("/history")
    fun bookingHistory(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?,
        @RequestParam(defaultValue = "0") page: Int
    ): String {
        return "redirect:/admin/bookings?tab=history" + 
               (startDate?.let { "&startDate=$it" } ?: "") +
               (endDate?.let { "&endDate=$it" } ?: "") +
               "&page=$page"
    }

    @GetMapping("/{id}/edit")
    fun editBookingForm(@PathVariable id: UUID, model: Model): String {
        try {
            val booking = bookingService.getBookingById(id)

            // Create BookingRequest from Booking
            val bookingRequest = BookingRequest(
                mainVisitorName = booking.mainVisitorName,
                mainVisitorPhone = booking.mainVisitorPhone,
                visitorsCount = booking.visitorsCount,
                dateTime = booking.dateTime,
                tableId = booking.tableId,
                notes = booking.notes
            )

            model.addAttribute("booking", booking)
            model.addAttribute("bookingRequest", bookingRequest)
            model.addAttribute("bookingDateTime", bookingRequest.dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
            model.addAttribute("content", "admin/booking-edit :: content")

            return "admin/layout"
        } catch (e: NoSuchElementException) {
            return "redirect:/admin/bookings?tab=pending"
        }
    }

    @PostMapping("/{id}/edit")
    fun updateBooking(
        @PathVariable id: UUID,
        @ModelAttribute bookingRequest: BookingRequest,
        @RequestParam status: String,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes
    ): String {
        // Basic validation
        if (bookingRequest.mainVisitorName.isBlank()) {
            bindingResult.rejectValue("mainVisitorName", "error.mainVisitorName", "Name is required")
        }

        if (bookingRequest.mainVisitorPhone.isBlank()) {
            bindingResult.rejectValue("mainVisitorPhone", "error.mainVisitorPhone", "Phone number is required")
        }

        if (bookingRequest.visitorsCount < 1) {
            bindingResult.rejectValue("visitorsCount", "error.visitorsCount", "Number of guests must be at least 1")
        }

        if (bindingResult.hasErrors()) {
            return "admin/booking-edit"
        }

        try {
            // Get existing booking to preserve ID
            val existingBooking = bookingService.getBookingById(id)

            // Normalize phone number by removing all non-digit characters
            val normalizedPhone = bookingRequest.mainVisitorPhone.replace(Regex("[^0-9]"), "")

            // Create updated booking
            val booking = Booking(
                id = id,
                status = BookingStatus.valueOf(status),
                mainVisitorName = bookingRequest.mainVisitorName,
                mainVisitorPhone = normalizedPhone,
                visitorsCount = bookingRequest.visitorsCount,
                dateTime = bookingRequest.dateTime,
                tableId = bookingRequest.tableId ?: "-1",
                notes = bookingRequest.notes,
//                createdAt = existingBooking.createdAt.minusHours(5)  // Preserve the original createdAt date
                createdAt = existingBooking.createdAt  // Preserve the original createdAt date
            )

            bookingService.updateBooking(id, booking)
            redirectAttributes.addFlashAttribute("message", "Booking updated successfully")
            redirectAttributes.addFlashAttribute("messageType", "success")

            return "redirect:/admin/bookings?tab=pending"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", e.message)
            redirectAttributes.addFlashAttribute("bookingRequest", bookingRequest)
            return "redirect:/admin/bookings/${id}/edit"
        }
    }

    @PostMapping("/{id}/confirm")
    fun confirmBooking(
        @PathVariable id: UUID, 
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            bookingService.confirmBooking(id)
            redirectAttributes.addFlashAttribute("message", "Booking confirmed successfully")
            redirectAttributes.addFlashAttribute("messageType", "success")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("message", "Error confirming booking: ${e.message}")
            redirectAttributes.addFlashAttribute("messageType", "danger")
        }
        return "redirect:/admin/bookings?tab=pending"
    }

    @PostMapping("/{id}/decline")
    fun declineBooking(
        @PathVariable id: UUID, 
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            bookingService.declineBooking(id)
            redirectAttributes.addFlashAttribute("message", "Booking declined successfully")
            redirectAttributes.addFlashAttribute("messageType", "success")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("message", "Error declining booking: ${e.message}")
            redirectAttributes.addFlashAttribute("messageType", "danger")
        }
        return "redirect:/admin/bookings?tab=pending"
    }

    @PostMapping("/{id}/waitlist")
    fun waitListBooking(
        @PathVariable id: UUID, 
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            bookingService.waitListBooking(id)
            redirectAttributes.addFlashAttribute("message", "Booking moved to wait list successfully")
            redirectAttributes.addFlashAttribute("messageType", "success")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("message", "Error moving booking to wait list: ${e.message}")
            redirectAttributes.addFlashAttribute("messageType", "danger")
        }
        return "redirect:/admin/bookings?tab=pending"
    }

    @PostMapping("/{id}/callagain")
    fun callAgainBooking(
        @PathVariable id: UUID, 
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            bookingService.callAgainBooking(id)
            redirectAttributes.addFlashAttribute("message", "Booking moved to call again status successfully")
            redirectAttributes.addFlashAttribute("messageType", "success")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("message", "Error moving booking to call again status: ${e.message}")
            redirectAttributes.addFlashAttribute("messageType", "danger")
        }
        return "redirect:/admin/bookings?tab=pending"
    }
}
