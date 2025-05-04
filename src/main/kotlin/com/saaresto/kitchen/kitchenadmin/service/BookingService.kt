package com.saaresto.kitchen.kitchenadmin.service

import com.saaresto.kitchen.kitchenadmin.model.Booking
import com.saaresto.kitchen.kitchenadmin.model.BookingStatus
import com.saaresto.kitchen.kitchenadmin.model.Visitor
import com.saaresto.kitchen.kitchenadmin.repository.BookingRepository
import com.saaresto.kitchen.kitchenadmin.repository.VisitorRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class BookingService(
    private val bookingRepository: BookingRepository,
    private val visitorRepository: VisitorRepository,
    private val notificationService: NotificationService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Get all bookings.
     */
    fun getAllBookings(): List<Booking> = bookingRepository.findAll()

    /**
     * Get a booking by ID.
     * @throws NoSuchElementException if booking not found
     */
    fun getBookingById(id: UUID): Booking = bookingRepository.findById(id)
        ?: throw NoSuchElementException("Booking with ID $id not found")

    /**
     * Get bookings by status.
     */
    fun getBookingsByStatus(status: BookingStatus): List<Booking> = 
        bookingRepository.findByStatus(status)

    /**
     * Get pending bookings sorted by createdAt (earliest first).
     * This includes PENDING, WAIT_LIST, and CALL_AGAIN statuses.
     */
    fun getPendingBookingsOrderByCreatedAt(): List<Booking> =
        bookingRepository.findByStatusesOrderByCreatedAt(listOf(BookingStatus.PENDING, BookingStatus.WAIT_LIST, BookingStatus.CALL_AGAIN))

    /**
     * Get bookings for today.
     */
    fun getTodayBookings(): List<Booking> = 
        bookingRepository.findByDate(LocalDateTime.now()).filter { it.status == BookingStatus.CONFIRMED }

    /**
     * Get bookings for today with optional visitor name and phone filters.
     */
    fun getTodayBookingsWithFilters(visitorName: String? = null, visitorPhone: String? = null): List<Booking> =
        bookingRepository.findByDateAndFilters(LocalDateTime.now(), visitorName, visitorPhone)
            .filter { it.status == BookingStatus.CONFIRMED }

    /**
     * Get bookings within a date range with optional visitor name and phone filters.
     */
    fun getBookingsByDateRangeWithFilters(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        visitorName: String? = null,
        visitorPhone: String? = null
    ): List<Booking> =
        bookingRepository.findByDateRangeAndFilters(startDate, endDate, visitorName, visitorPhone)

    /**
     * Create a new booking.
     * @throws IllegalArgumentException if booking time is invalid
     */
    fun createBooking(booking: Booking): Booking {
        // Validate booking time (must be at the beginning of the hour or half past)
        validateBookingTime(booking.dateTime)

        // Save the booking
        val savedBooking = bookingRepository.save(booking)

        // Create a visitor record for the main visitor
        val visitor = Visitor(
            phoneNumber = booking.mainVisitorPhone,
            name = booking.mainVisitorName,
            notes = "Created at ${booking.dateTime.format(DateTimeFormatter.ISO_DATE_TIME)} from booking ${booking.id}"
        )
        visitorRepository.save(visitor)

        // Send notification to staff members if booking is pending
        if (savedBooking.status == BookingStatus.PENDING) {
            logger.info("Sending notification for pending booking: ${savedBooking.id}")
            notificationService.sendBookingNotification(savedBooking)
        }

        return savedBooking
    }

    /**
     * Update an existing booking.
     * @throws NoSuchElementException if booking not found
     * @throws IllegalArgumentException if booking time is invalid
     */
    fun updateBooking(id: UUID, booking: Booking): Booking {
        // Check if booking exists
        bookingRepository.findById(id)
            ?: throw NoSuchElementException("Booking with ID $id not found")

        // Validate booking time
        validateBookingTime(booking.dateTime)

        // Update with the provided ID
        return bookingRepository.save(booking.copy(id = id))
    }

    /**
     * Confirm a booking.
     * @throws NoSuchElementException if booking not found
     */
    fun confirmBooking(id: UUID): Booking {
        val booking = getBookingById(id)
        return bookingRepository.save(booking.copy(status = BookingStatus.CONFIRMED))
    }

    /**
     * Decline a booking.
     * @throws NoSuchElementException if booking not found
     */
    fun declineBooking(id: UUID): Booking {
        val booking = getBookingById(id)
        return bookingRepository.save(booking.copy(status = BookingStatus.DECLINED))
    }

    /**
     * Move a booking to wait list.
     * @throws NoSuchElementException if booking not found
     */
    fun waitListBooking(id: UUID): Booking {
        val booking = getBookingById(id)
        return bookingRepository.save(booking.copy(status = BookingStatus.WAIT_LIST))
    }

    /**
     * Move a booking to call again status.
     * @throws NoSuchElementException if booking not found
     */
    fun callAgainBooking(id: UUID): Booking {
        val booking = getBookingById(id)
        return bookingRepository.save(booking.copy(status = BookingStatus.CALL_AGAIN))
    }

    /**
     * Delete a booking by ID.
     * @throws NoSuchElementException if booking not found
     */
    fun deleteBooking(id: UUID) {
        if (!bookingRepository.deleteById(id)) {
            throw NoSuchElementException("Booking with ID $id not found")
        }
    }

    /**
     * Validate that booking time is at the beginning of the hour or half past.
     * @throws IllegalArgumentException if booking time is invalid
     */
    private fun validateBookingTime(dateTime: LocalDateTime) {
        val minutes = dateTime.minute
        if (minutes != 0 && minutes != 30) {
            throw IllegalArgumentException("Booking time must be at the beginning of the hour or half past (e.g., 9:00 or 9:30)")
        }

        // Truncate seconds and nanos for consistency
        val truncatedDateTime = dateTime.truncatedTo(ChronoUnit.MINUTES)
        if (truncatedDateTime != dateTime) {
            throw IllegalArgumentException("Booking time must not include seconds or milliseconds")
        }
    }
}
