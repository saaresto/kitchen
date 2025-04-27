package com.saaresto.kitchen.kitchenadmin.dto

import com.saaresto.kitchen.kitchenadmin.model.Booking
import com.saaresto.kitchen.kitchenadmin.model.BookingStatus
import java.time.LocalDateTime
import java.util.UUID

/**
 * DTO for creating or updating a booking.
 */
data class BookingRequest(
    val mainVisitorName: String,
    val mainVisitorPhone: String,
    val visitorsCount: Int,
    val dateTime: LocalDateTime,
    val tableId: Int,
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
    val tableId: Int,
    val notes: String?
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
            notes = booking.notes
        )
    }
}

/**
 * DTO for updating booking status.
 */
data class BookingStatusUpdateRequest(
    val status: BookingStatus
)