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
     */
    fun getDisabledDateTimesForRange(startDate: LocalDate, endDate: LocalDate): List<LocalDateTime> {
        val result = mutableListOf<LocalDateTime>()

        // Get all disabled dates in the range
        val disabledDates = disabledDateRepository.findByDateRange(startDate, endDate)

        // Get all recurring disabled dates
        val recurringDates = disabledDateRepository.findRecurring()

        // Process regular disabled dates
        for (disabledDate in disabledDates) {
            if (disabledDate.startTime == null && disabledDate.endTime == null) {
                // Whole day is disabled, add all booking times (12:00 to 22:30 with 30 min intervals)
                var time = LocalTime.of(12, 0)
                while (time <= LocalTime.of(22, 30)) {
                    result.add(LocalDateTime.of(disabledDate.date, time))
                    time = time.plusMinutes(30)
                }
            } else if (disabledDate.startTime != null && disabledDate.endTime != null) {
                // Specific time range is disabled
                val startTime = disabledDate.startTime ?: continue
                val endTime = disabledDate.endTime ?: continue
                var time = startTime
                while (time <= endTime) {
                    result.add(LocalDateTime.of(disabledDate.date, time))
                    time = time.plusMinutes(30)
                }
            }
        }

        // Process recurring disabled dates
        for (date in startDate.datesUntil(endDate.plusDays(1)).toList()) {
            for (recurringDate in recurringDates) {
                if (recurringDate.date.dayOfMonth == date.dayOfMonth && recurringDate.date.month == date.month) {
                    if (recurringDate.startTime == null && recurringDate.endTime == null) {
                        // Whole day is disabled, add all booking times (12:00 to 22:30 with 30 min intervals)
                        var time = LocalTime.of(12, 0)
                        while (time <= LocalTime.of(22, 30)) {
                            result.add(LocalDateTime.of(date, time))
                            time = time.plusMinutes(30)
                        }
                    } else if (recurringDate.startTime != null && recurringDate.endTime != null) {
                        // Specific time range is disabled
                        val startTime = recurringDate.startTime ?: continue
                        val endTime = recurringDate.endTime ?: continue
                        var time = startTime
                        while (time <= endTime) {
                            result.add(LocalDateTime.of(date, time))
                            time = time.plusMinutes(30)
                        }
                    }
                }
            }
        }

        return result
    }
}
