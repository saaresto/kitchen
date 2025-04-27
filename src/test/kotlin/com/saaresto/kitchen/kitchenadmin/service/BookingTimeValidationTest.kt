package com.saaresto.kitchen.kitchenadmin.service

import com.saaresto.kitchen.kitchenadmin.model.Booking
import com.saaresto.kitchen.kitchenadmin.model.BookingStatus
import com.saaresto.kitchen.kitchenadmin.repository.BookingRepository
import com.saaresto.kitchen.kitchenadmin.repository.VisitorRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import java.time.LocalDateTime
import java.util.UUID

class BookingTimeValidationTest {

    private lateinit var bookingRepository: BookingRepository
    private lateinit var visitorRepository: VisitorRepository
    private lateinit var bookingService: BookingService

    @BeforeEach
    fun setUp() {
        bookingRepository = mock(BookingRepository::class.java)
        visitorRepository = mock(VisitorRepository::class.java)
        bookingService = BookingService(bookingRepository, visitorRepository)
    }

    @Test
    fun `createBooking should accept booking at the beginning of the hour`() {
        // Arrange
        val dateTime = LocalDateTime.of(2023, 5, 20, 9, 0, 0)
        val booking = createSampleBooking(dateTime)

        `when`(bookingRepository.save(booking)).thenReturn(booking)

        // Act & Assert
        // This should not throw an exception
        val result = bookingService.createBooking(booking)

        // Verify the result
        assertEquals(booking, result)
    }

    @Test
    fun `createBooking should accept booking at half past the hour`() {
        // Arrange
        val dateTime = LocalDateTime.of(2023, 5, 20, 9, 30, 0)
        val booking = createSampleBooking(dateTime)

        `when`(bookingRepository.save(booking)).thenReturn(booking)

        // Act & Assert
        // This should not throw an exception
        val result = bookingService.createBooking(booking)

        // Verify the result
        assertEquals(booking, result)
    }

    @Test
    fun `createBooking should reject booking at invalid time`() {
        // Arrange
        val dateTime = LocalDateTime.of(2023, 5, 20, 9, 15, 0)
        val booking = createSampleBooking(dateTime)

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            bookingService.createBooking(booking)
        }

        // Verify the exception message
        assertTrue(exception.message!!.contains("Booking time must be at the beginning of the hour or half past"))

        // Verify no interactions with repositories
        verifyNoInteractions(bookingRepository)
        verifyNoInteractions(visitorRepository)
    }

    @Test
    fun `createBooking should reject booking with seconds or milliseconds`() {
        // Arrange
        val dateTime = LocalDateTime.of(2023, 5, 20, 9, 0, 1)
        val booking = createSampleBooking(dateTime)

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            bookingService.createBooking(booking)
        }

        // Verify the exception message
        assertTrue(exception.message!!.contains("Booking time must not include seconds or milliseconds"))

        // Verify no interactions with repositories
        verifyNoInteractions(bookingRepository)
        verifyNoInteractions(visitorRepository)
    }

    private fun createSampleBooking(dateTime: LocalDateTime): Booking {
        return Booking(
            id = UUID.randomUUID(),
            status = BookingStatus.PENDING,
            mainVisitorName = "John Doe",
            mainVisitorPhone = "+1234567890",
            visitorsCount = 4,
            dateTime = dateTime,
            tableId = 5,
            notes = "Test booking"
        )
    }
}
