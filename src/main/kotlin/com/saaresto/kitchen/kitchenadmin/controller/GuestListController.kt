package com.saaresto.kitchen.kitchenadmin.controller

import com.saaresto.kitchen.kitchenadmin.service.VisitorService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.time.format.DateTimeFormatter

/**
 * Controller for the Guest List page.
 */
@Controller
@RequestMapping("/admin/guests")
class GuestListController(private val visitorService: VisitorService) {

    /**
     * Display the Guest List page.
     */
    @GetMapping
    fun guestList(
        @RequestParam(defaultValue = "0") page: Int,
        model: Model
    ): String {
        // Set active tab
        model.addAttribute("activeTab", "guests")
        
        // Page size
        val pageSize = 20
        
        // Get visitors with confirmed bookings
        val visitors = visitorService.getVisitorsWithConfirmedBookings(page, pageSize)
        
        // Get total count for pagination
        val totalVisitors = visitorService.countVisitorsWithConfirmedBookings()
        val totalPages = (totalVisitors + pageSize - 1) / pageSize
        
        // Add data to model
        model.addAttribute("visitors", visitors)
        model.addAttribute("currentPage", page)
        model.addAttribute("totalPages", totalPages)
        model.addAttribute("content", "admin/guests :: content")
        
        return "admin/layout"
    }
    
    /**
     * Get confirmed bookings for a specific visitor (for AJAX call).
     */
    @GetMapping("/bookings")
    @ResponseBody
    fun getVisitorBookings(@RequestParam phoneNumber: String): List<Map<String, Any>> {
        val bookings = visitorService.getConfirmedBookingsForVisitor(phoneNumber)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        
        return bookings.map { (dateTime, visitorCount) ->
            mapOf(
                "dateTime" to dateTime.format(formatter),
                "visitorCount" to visitorCount
            )
        }
    }
}