package com.saaresto.kitchen.kitchenadmin.controller

import com.saaresto.kitchen.kitchenadmin.dto.BookingRequest
import com.saaresto.kitchen.kitchenadmin.model.Booking
import com.saaresto.kitchen.kitchenadmin.service.BookingService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.time.LocalDateTime

@Controller
@RequestMapping("/bookings")
class BookingFormController(private val bookingService: BookingService) {

    @GetMapping("/create")
    fun showBookingForm(model: Model): String {
        if (!model.containsAttribute("bookingRequest")) {
            model.addAttribute("bookingRequest", BookingRequest(
                mainVisitorName = "",
                mainVisitorPhone = "",
                visitorsCount = 1,
                dateTime = LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0),
                tableId = "1",
                notes = ""
            ))
        }
        return "booking-form"
    }

    @PostMapping("/create")
    fun createBooking(
        @ModelAttribute bookingRequest: BookingRequest,
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
            return "booking-form"
        }

        try {
            // Normalize phone number to format 87771112233
            val digitsOnly = bookingRequest.mainVisitorPhone.replace(Regex("[^0-9]"), "")
            val normalizedPhone = when {
                digitsOnly.length == 10 -> "8$digitsOnly"
                digitsOnly.length == 11 && digitsOnly.startsWith("7") -> "8${digitsOnly.substring(1)}"
                digitsOnly.length == 11 && digitsOnly.startsWith("8") -> digitsOnly
                else -> digitsOnly // Keep as is for other cases
            }

            val booking = Booking(
                mainVisitorName = bookingRequest.mainVisitorName,
                mainVisitorPhone = normalizedPhone,
                visitorsCount = bookingRequest.visitorsCount,
                dateTime = bookingRequest.dateTime,
                tableId = bookingRequest.tableId ?: "-1",
                notes = bookingRequest.notes
            )

            bookingService.createBooking(booking)
            redirectAttributes.addFlashAttribute("success", true)
            return "redirect:/bookings/create"

        } catch (e: IllegalArgumentException) {
            redirectAttributes.addFlashAttribute("error", e.message)
            redirectAttributes.addFlashAttribute("bookingRequest", bookingRequest)
            return "redirect:/bookings/create"
        }
    }
}
