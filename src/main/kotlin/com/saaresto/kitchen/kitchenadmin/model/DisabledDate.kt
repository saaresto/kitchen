package com.saaresto.kitchen.kitchenadmin.model

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

/**
 * Entity representing a disabled date or time for bookings.
 */
data class DisabledDate(
    val id: UUID = UUID.randomUUID(),
    val date: LocalDate,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val description: String? = null,
    val isRecurring: Boolean = false,
    val createdAt: LocalDate = LocalDate.now()
)