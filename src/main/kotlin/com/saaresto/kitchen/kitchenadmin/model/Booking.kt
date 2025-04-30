package com.saaresto.kitchen.kitchenadmin.model

import java.time.LocalDateTime
import java.util.UUID

/**
 * Enum representing the status of a booking.
 */
enum class BookingStatus {
    PENDING,
    CONFIRMED,
    DECLINED
}

/**
 * Entity representing a restaurant booking.
 */
data class Booking(
    val id: UUID = UUID.randomUUID(),
    val status: BookingStatus = BookingStatus.PENDING,
    val mainVisitorName: String,
    val mainVisitorPhone: String,
    val visitorsCount: Int,
    val dateTime: LocalDateTime,
    val tableId: Int,
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
