package com.saaresto.kitchen.kitchenadmin.controller

import com.saaresto.kitchen.kitchenadmin.dto.BookingRequest
import com.saaresto.kitchen.kitchenadmin.dto.BookingResponse
import com.saaresto.kitchen.kitchenadmin.dto.BookingStatusUpdateRequest
import com.saaresto.kitchen.kitchenadmin.model.Booking
import com.saaresto.kitchen.kitchenadmin.model.BookingStatus
import com.saaresto.kitchen.kitchenadmin.service.BookingService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@RestController
@RequestMapping("/api/bookings")
class BookingApiController(private val bookingService: BookingService) {

    @GetMapping
    fun getAllBookings(): ResponseEntity<List<BookingResponse>> {
        val bookings = bookingService.getAllBookings()
        return ResponseEntity.ok(bookings.map { BookingResponse.fromBooking(it) })
    }

    @GetMapping("/{id}")
    fun getBookingById(@PathVariable id: UUID): ResponseEntity<BookingResponse> {
        return try {
            val booking = bookingService.getBookingById(id)
            ResponseEntity.ok(BookingResponse.fromBooking(booking))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/status/{status}")
    fun getBookingsByStatus(@PathVariable status: BookingStatus): ResponseEntity<List<BookingResponse>> {
        val bookings = bookingService.getBookingsByStatus(status)
        return ResponseEntity.ok(bookings.map { BookingResponse.fromBooking(it) })
    }

    @GetMapping("/today")
    fun getTodayBookings(): ResponseEntity<List<BookingResponse>> {
        val bookings = bookingService.getTodayBookings()
        return ResponseEntity.ok(bookings.map { BookingResponse.fromBooking(it) })
    }

    @PostMapping
    fun createBooking(@RequestBody request: BookingRequest): ResponseEntity<Any> {
        return try {
            // Normalize phone number by removing all non-digit characters
            val normalizedPhone = request.mainVisitorPhone.replace(Regex("[^0-9]"), "")

            val booking = Booking(
                mainVisitorName = request.mainVisitorName,
                mainVisitorPhone = normalizedPhone,
                visitorsCount = request.visitorsCount,
                dateTime = request.dateTime,
                tableId = request.tableId ?: "-1",
                notes = request.notes
            )
            val createdBooking = bookingService.createBooking(booking)
            ResponseEntity.status(HttpStatus.CREATED)
                .body(BookingResponse.fromBooking(createdBooking))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PutMapping("/{id}")
    fun updateBooking(
        @PathVariable id: UUID,
        @RequestBody request: BookingRequest
    ): ResponseEntity<Any> {
        return try {
            // Normalize phone number by removing all non-digit characters
            val normalizedPhone = request.mainVisitorPhone.replace(Regex("[^0-9]"), "")

            val booking = Booking(
                id = id,
                status = bookingService.getBookingById(id).status, // Preserve existing status
                mainVisitorName = request.mainVisitorName,
                mainVisitorPhone = normalizedPhone,
                visitorsCount = request.visitorsCount,
                dateTime = request.dateTime,
                tableId = request.tableId ?: "-1",
                notes = request.notes
            )
            val updatedBooking = bookingService.updateBooking(id, booking)
            ResponseEntity.ok(BookingResponse.fromBooking(updatedBooking))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PatchMapping("/{id}/status")
    fun updateBookingStatus(
        @PathVariable id: UUID,
        @RequestBody request: BookingStatusUpdateRequest
    ): ResponseEntity<Any> {
        return try {
            val booking = when (request.status) {
                BookingStatus.CONFIRMED -> bookingService.confirmBooking(id)
                BookingStatus.DECLINED -> bookingService.declineBooking(id)
                else -> {
                    val existingBooking = bookingService.getBookingById(id)
                    bookingService.updateBooking(id, existingBooking.copy(status = request.status))
                }
            }
            ResponseEntity.ok(BookingResponse.fromBooking(booking))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    fun deleteBooking(@PathVariable id: UUID): ResponseEntity<Unit> {
        return try {
            bookingService.deleteBooking(id)
            ResponseEntity.noContent().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }
}
