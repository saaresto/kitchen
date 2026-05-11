package com.saaresto.kitchen.kitchenadmin.service

import com.saaresto.kitchen.kitchenadmin.model.DisabledDate
import com.saaresto.kitchen.kitchenadmin.repository.DisabledDateRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import org.junit.jupiter.api.Assertions.*

@ExtendWith(MockitoExtension::class)
class DisabledDateMemoryTest {

    @Mock
    private lateinit var disabledDateRepository: DisabledDateRepository

    @InjectMocks
    private lateinit var disabledDateService: DisabledDateService

    @Test
    fun `test getDisabledDateTimesForRange with large date range should limit results`() {
        // Given - simulate a scenario that could cause OOM
        val startDate = LocalDate.now()
        val endDate = startDate.plusYears(2) // Very large range

        // Mock some disabled dates that would generate many time slots
        val disabledDates = listOf(
            DisabledDate(
                id = UUID.randomUUID(),
                date = startDate,
                startTime = null, // Whole day disabled
                endTime = null,
                isRecurring = false
            ),
            DisabledDate(
                id = UUID.randomUUID(),
                date = startDate.plusDays(1),
                startTime = null, // Whole day disabled
                endTime = null,
                isRecurring = false
            )
        )

        val recurringDates = listOf(
            DisabledDate(
                id = UUID.randomUUID(),
                date = LocalDate.of(2024, 1, 1), // Every January 1st
                startTime = null, // Whole day disabled
                endTime = null,
                isRecurring = true
            )
        )

        Mockito.`when`(disabledDateRepository.findByDateRange(any(), any())).thenReturn(disabledDates)
        Mockito.`when`(disabledDateRepository.findRecurring()).thenReturn(recurringDates)

        // When
        val result = disabledDateService.getDisabledDateTimesForRange(startDate, endDate)

        // Then - should not cause OOM and should limit the results
        assertNotNull(result)
        assertTrue(result.size <= 10000, "Result size should be limited to prevent OOM: ${result.size}")

        // Verify that the date range was limited (should not process the full 2 years)
        val maxExpectedResults = 6 * 30 * 21 // 6 months * ~30 days * 21 time slots per day
        assertTrue(result.size <= maxExpectedResults, "Result size should respect date range limits")
    }

    @Test
    fun `test getDisabledDateTimesForRange with reasonable range should work normally`() {
        // Given - normal scenario
        val startDate = LocalDate.now()
        val endDate = startDate.plusMonths(1)

        val disabledDates = listOf(
            DisabledDate(
                id = UUID.randomUUID(),
                date = startDate,
                startTime = LocalTime.of(14, 0),
                endTime = LocalTime.of(16, 0),
                isRecurring = false
            )
        )

        Mockito.`when`(disabledDateRepository.findByDateRange(startDate, endDate)).thenReturn(disabledDates)
        Mockito.`when`(disabledDateRepository.findRecurring()).thenReturn(emptyList())

        // When
        val result = disabledDateService.getDisabledDateTimesForRange(startDate, endDate)

        // Then
        assertNotNull(result)
        assertTrue(result.size > 0, "Should return some disabled times")
        assertTrue(result.size <= 100, "Should not return excessive results for small range")

        // Verify the times are in the expected range
        result.forEach { dateTime ->
            assertTrue(dateTime.toLocalTime() >= LocalTime.of(14, 0))
            assertTrue(dateTime.toLocalTime() <= LocalTime.of(16, 0))
        }
    }

    @Test
    fun `test memory usage stays reasonable with many recurring dates`() {
        // Given - many recurring dates that could cause memory issues
        val startDate = LocalDate.now()
        val endDate = startDate.plusMonths(3)

        val recurringDates = (1..12).map { month ->
            DisabledDate(
                id = UUID.randomUUID(),
                date = LocalDate.of(2024, month, 15), // 15th of every month
                startTime = null, // Whole day
                endTime = null,
                isRecurring = true
            )
        }

        Mockito.`when`(disabledDateRepository.findByDateRange(any(), any())).thenReturn(emptyList())
        Mockito.`when`(disabledDateRepository.findRecurring()).thenReturn(recurringDates)

        // When
        val result = disabledDateService.getDisabledDateTimesForRange(startDate, endDate)

        // Then - should handle many recurring dates without OOM
        assertNotNull(result)
        assertTrue(result.size <= 10000, "Should respect safety limits: ${result.size}")
    }

    @Test
    fun `test addDisabledTimesForDate with potential infinite loop`() {
        // Given
        val startDate = LocalDate.of(2024, 5, 1)
        val endDate = startDate // Same day
        
        val disabledDates = listOf(
            DisabledDate(
                id = UUID.randomUUID(),
                date = startDate,
                startTime = LocalTime.of(23, 0),
                endTime = LocalTime.of(23, 30),
                isRecurring = false
            )
        )

        Mockito.`when`(disabledDateRepository.findByDateRange(any(), any())).thenReturn(disabledDates)
        Mockito.`when`(disabledDateRepository.findRecurring()).thenReturn(emptyList())

        // When/Then - this should not hang
        assertTimeoutPreemptively(java.time.Duration.ofSeconds(5)) {
            disabledDateService.getDisabledDateTimesForRange(startDate, endDate)
        }
    }
}
