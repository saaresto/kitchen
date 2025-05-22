package com.saaresto.kitchen.kitchenadmin.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class RootController {

    @GetMapping("/")
    fun redirectToBookingForm(model: Model): String {
        return "redirect:/bookings/create"
    }
}