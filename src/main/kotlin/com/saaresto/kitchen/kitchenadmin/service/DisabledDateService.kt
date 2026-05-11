package com.saaresto.kitchen.kitchenadmin.service

import com.saaresto.kitchen.kitchenadmin.model.DisabledDate
import com.saaresto.kitchen.kitchenadmin.repository.DisabledDateRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

@Service
class DisabledDateService(private val disabledDateRepository: DisabledDateRepository) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Get all disabled dates.
     */
    fun getAllDisabledDates(): List<DisabledDate> = disabledDateRepository.findAll()

    /**
     * Get a disabled date by ID.
     * @throws NoSuchElementException if disabled date not found
     */
    fun getDisabledDateById(id: UUID): DisabledDate = disabledDateRepository.findById(id)
        ?: throw NoSuchElementException("Disabled date with ID $id not found")

    /**
     * Get disabled dates for a specific date.
     */
    fun getDisabledDatesByDate(date: LocalDate): List<DisabledDate> = 
        disabledDateRepository.findByDate(date)

    /**
     * Get disabled dates within a date range.
     */
    fun getDisabledDatesByDateRange(startDate: LocalDate, endDate: LocalDate): List<DisabledDate> =
        disabledDateRepository.findByDateRange(startDate, endDate)

    /**
     * Get recurring disabled dates.
     */
    fun getRecurringDisabledDates(): List<DisabledDate> =
        disabledDateRepository.findRecurring()

    /**
     * Create a new disabled date.
     */
    fun createDisabledDate(disabledDate: DisabledDate): DisabledDate {
        logger.info("Creating disabled date for ${disabledDate.date}")
        return disabledDateRepository.save(disabledDate)
    }

    /**
     * Update an existing disabled date.
     * @throws NoSuchElementException if disabled date not found
     */
    fun updateDisabledDate(id: UUID, disabledDate: DisabledDate): DisabledDate {
        // Check if disabled date exists
        disabledDateRepository.findById(id)
            ?: throw NoSuchElementException("Disabled date with ID $id not found")

        // Update with the provided ID
        return disabledDateRepository.save(disabledDate.copy(id = id))
    }

    /**
     * Delete a disabled date by ID.
     * @throws NoSuchElementException if disabled date not found
     */
    fun deleteDisabledDate(id: UUID) {
        if (!disabledDateRepository.deleteById(id)) {
            throw NoSuchElementException("Disabled date with ID $id not found")
        }
    }

    /**
     * Check if a date is disabled.
     */
    fun isDateDisabled(date: LocalDate): Boolean {
        val disabledDates = disabledDateRepository.findByDate(date)
        val recurringDates = disabledDateRepository.findRecurring()
            .filter { it.date.dayOfMonth == date.dayOfMonth && it.date.month == date.month }

        return disabledDates.isNotEmpty() || recurringDates.isNotEmpty()
    }

    /**
     * Check if a date and time is disabled.
     */
    fun isDateTimeDisabled(dateTime: LocalDateTime): Boolean {
        val date = dateTime.toLocalDate()
        val time = dateTime.toLocalTime()

        // Check if the date is fully disabled
        val disabledDates = disabledDateRepository.findByDate(date)
        if (disabledDates.any { it.startTime == null && it.endTime == null }) {
            return true
        }

        // Check if the time is within a disabled time range
        if (disabledDates.any { 
            it.startTime != null && it.endTime != null && 
            time >= it.startTime && time <= it.endTime 
        }) {
            return true
        }

        // Check recurring dates
        val recurringDates = disabledDateRepository.findRecurring()
            .filter { it.date.dayOfMonth == date.dayOfMonth && it.date.month == date.month }

        if (recurringDates.any { it.startTime == null && it.endTime == null }) {
            return true
        }

        if (recurringDates.any { 
            it.startTime != null && it.endTime != null && 
            time >= it.startTime && time <= it.endTime 
        }) {
            return true
        }

        return false
    }

    /**
     * Get all disabled dates and times for a date range.
     * Returns a list of LocalDateTime objects representing disabled dates and times.
     * Optimized to avoid memory issues by limiting the number of generated objects.
     */
    fun getDisabledDateTimesForRange(startDate: LocalDate, endDate: LocalDate): List<LocalDateTime> {
        logger.debug("Getting disabled date times for range: $startDate to $endDate")

        // Limit the date range to prevent excessive memory usage
        val maxRangeMonths = 6L
        val actualEndDate = if (startDate.plusMonths(maxRangeMonths).isBefore(endDate)) {
            logger.warn("Date range too large, limiting to $maxRangeMonths months from start date")
            startDate.plusMonths(maxRangeMonths)
        } else {
            endDate
        }

        val result = mutableSetOf<LocalDateTime>() // Use Set to avoid duplicates

        // Get all disabled dates in the range
        val disabledDates = disabledDateRepository.findByDateRange(startDate, actualEndDate)
        logger.debug("Found ${disabledDates.size} disabled dates in range")

        // Get all recurring disabled dates
        val recurringDates = disabledDateRepository.findRecurring()
        logger.debug("Found ${recurringDates.size} recurring disabled dates")

        // Process regular disabled dates
        for (disabledDate in disabledDates) {
            addDisabledTimesForDate(result, disabledDate.date, disabledDate.startTime, disabledDate.endTime)
        }

        // Process recurring disabled dates - use sequence to avoid creating large intermediate lists
        startDate.datesUntil(actualEndDate.plusDays(1)).use { dateSequence ->
            dateSequence.forEach { date ->
                for (recurringDate in recurringDates) {
                    if (recurringDate.date.dayOfMonth == date.dayOfMonth && recurringDate.date.month == date.month) {
                        addDisabledTimesForDate(result, date, recurringDate.startTime, recurringDate.endTime)
                    }
                }
            }
        }

        logger.debug("Generated ${result.size} disabled date times")
        return result.toList()
    }

    /**
     * Helper method to add disabled times for a specific date to avoid code duplication.
     * Limits the number of time slots to prevent memory issues.
     */
    private fun addDisabledTimesForDate(
        result: MutableSet<LocalDateTime>, 
        date: LocalDate, 
        startTime: LocalTime?, 
        endTime: LocalTime?
    ) {
        if (startTime == null && endTime == null) {
            // Whole day is disabled, add booking times (12:00 to 22:30 with 30 min intervals)
            // Limit to reasonable booking hours to prevent excessive memory usage
            var time = LocalTime.of(12, 0)
            val maxTime = LocalTime.of(22, 30)
            while (time <= maxTime && result.size < 10000) { // Safety limit
                result.add(LocalDateTime.of(date, time))
                val nextTime = time.plusMinutes(30)
                if (nextTime <= time) break // Wrapped around or didn't advance
                time = nextTime
            }
        } else if (startTime != null && endTime != null) {
            // Specific time range is disabled
            var time = startTime!!
            val endTimeNonNull = endTime!!
            while (time <= endTimeNonNull && result.size < 10000) { // Safety limit
                result.add(LocalDateTime.of(date, time))
                val nextTime = time.plusMinutes(30)
                if (nextTime <= time) break // Wrapped around or didn't advance
                time = nextTime
            }
        }
    }
}
