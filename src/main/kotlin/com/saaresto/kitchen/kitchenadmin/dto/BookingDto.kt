package com.saaresto.kitchen.kitchenadmin.dto

import com.saaresto.kitchen.kitchenadmin.model.Booking
import com.saaresto.kitchen.kitchenadmin.model.BookingStatus
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime
import java.util.UUID

/**
 * DTO for creating or updating a booking.
 */
data class BookingRequest(
    val mainVisitorName: String,
    val mainVisitorPhone: String,
    val visitorsCount: Int,
    @DateTimeFormat(pattern = "dd.MM.yyyy, HH:mm") val dateTime: LocalDateTime,
    val tableId: String? = "0",
    val notes: String? = null
)

/**
 * DTO for booking response.
 */
data class BookingResponse(
    val id: UUID,
    val status: BookingStatus,
    val mainVisitorName: String,
    val mainVisitorPhone: String,
    val visitorsCount: Int,
    val dateTime: LocalDateTime,
    val tableId: String,
    val notes: String?,
    val createdAt: LocalDateTime
) {
    companion object {
        fun fromBooking(booking: Booking): BookingResponse = BookingResponse(
            id = booking.id,
            status = booking.status,
            mainVisitorName = booking.mainVisitorName,
            mainVisitorPhone = booking.mainVisitorPhone,
            visitorsCount = booking.visitorsCount,
            dateTime = booking.dateTime,
            tableId = booking.tableId,
            notes = booking.notes,
            createdAt = booking.createdAt
        )
    }
}

/**
 * DTO for updating booking status.
 */
data class BookingStatusUpdateRequest(
    val status: BookingStatus
)
