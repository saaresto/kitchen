package com.saaresto.kitchen.kitchenadmin.controller

import com.saaresto.kitchen.kitchenadmin.model.DisabledDate
import com.saaresto.kitchen.kitchenadmin.service.DisabledDateService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@Controller
@RequestMapping("/admin/settings")
class DisabledDateController(private val disabledDateService: DisabledDateService) {

    @GetMapping
    fun showSettings(model: Model): String {
        val disabledDates = disabledDateService.getAllDisabledDates()
        model.addAttribute("disabledDates", disabledDates)
        model.addAttribute("newDisabledDate", DisabledDate(
            date = LocalDate.now(),
            startTime = null,
            endTime = null,
            description = "",
            isRecurring = false
        ))
        model.addAttribute("activeTab", "settings")
        model.addAttribute("content", "admin/settings :: content")
        return "admin/layout"
    }

    @PostMapping("/disabled-dates/create")
    fun createDisabledDate(
        @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        @RequestParam("startTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) startTime: LocalTime?,
        @RequestParam("endTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) endTime: LocalTime?,
        @RequestParam("description", required = false) description: String?,
        @RequestParam("isRecurring", required = false, defaultValue = "false") isRecurring: Boolean,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            // Validate time range if provided
            if (startTime != null && endTime != null && startTime >= endTime) {
                redirectAttributes.addFlashAttribute("error", "Start time must be before end time")
                return "redirect:/admin/settings"
            }

            val disabledDate = DisabledDate(
                date = date,
                startTime = startTime,
                endTime = endTime,
                description = description,
                isRecurring = isRecurring
            )

            disabledDateService.createDisabledDate(disabledDate)
            redirectAttributes.addFlashAttribute("message", "Disabled date created successfully")
            redirectAttributes.addFlashAttribute("messageType", "success")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("message", "Error creating disabled date: ${e.message}")
            redirectAttributes.addFlashAttribute("messageType", "danger")
        }

        return "redirect:/admin/settings"
    }

    @PostMapping("/disabled-dates/{id}/delete")
    fun deleteDisabledDate(
        @PathVariable id: UUID,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            disabledDateService.deleteDisabledDate(id)
            redirectAttributes.addFlashAttribute("message", "Disabled date deleted successfully")
            redirectAttributes.addFlashAttribute("messageType", "success")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("message", "Error deleting disabled date: ${e.message}")
            redirectAttributes.addFlashAttribute("messageType", "danger")
        }

        return "redirect:/admin/settings"
    }

    @GetMapping("/disabled-dates/{id}/edit")
    fun editDisabledDateForm(
        @PathVariable id: UUID,
        model: Model
    ): String {
        try {
            val disabledDate = disabledDateService.getDisabledDateById(id)
            model.addAttribute("disabledDate", disabledDate)
            model.addAttribute("activeTab", "settings")
            model.addAttribute("content", "admin/settings-edit :: content")
            return "admin/layout"
        } catch (e: NoSuchElementException) {
            return "redirect:/admin/settings"
        }
    }

    @PostMapping("/disabled-dates/{id}/edit")
    fun updateDisabledDate(
        @PathVariable id: UUID,
        @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        @RequestParam("startTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) startTime: LocalTime?,
        @RequestParam("endTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) endTime: LocalTime?,
        @RequestParam("description", required = false) description: String?,
        @RequestParam("isRecurring", required = false, defaultValue = "false") isRecurring: Boolean,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            // Validate time range if provided
            if (startTime != null && endTime != null && startTime >= endTime) {
                redirectAttributes.addFlashAttribute("error", "Start time must be before end time")
                return "redirect:/admin/settings/disabled-dates/${id}/edit"
            }

            val disabledDate = DisabledDate(
                id = id,
                date = date,
                startTime = startTime,
                endTime = endTime,
                description = description,
                isRecurring = isRecurring
            )

            disabledDateService.updateDisabledDate(id, disabledDate)
            redirectAttributes.addFlashAttribute("message", "Disabled date updated successfully")
            redirectAttributes.addFlashAttribute("messageType", "success")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("message", "Error updating disabled date: ${e.message}")
            redirectAttributes.addFlashAttribute("messageType", "danger")
        }

        return "redirect:/admin/settings"
    }
}