package com.saaresto.kitchen.kitchenadmin.controller

import com.saaresto.kitchen.kitchenadmin.dto.BookingRequest
import com.saaresto.kitchen.kitchenadmin.service.BookingService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.validation.BindingResult
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import com.saaresto.kitchen.kitchenadmin.model.Booking
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.lastValue

@ExtendWith(MockitoExtension::class)
class PhoneNumberNormalizationTest {

    @Mock
    private lateinit var bookingService: BookingService

    @Mock
    private lateinit var bindingResult: BindingResult

    @Mock
    private lateinit var redirectAttributes: RedirectAttributes

    @InjectMocks
    private lateinit var bookingFormController: BookingFormController

    @Test
    fun `test phone number normalization with plus seven format`() {
        // Given
        val bookingRequest = BookingRequest(
            mainVisitorName = "Test User",
            mainVisitorPhone = "+77771112233",
            visitorsCount = 2,
            dateTime = LocalDateTime.now().withMinute(0),
            tableId = "1",
            notes = "Test booking"
        )

        Mockito.`when`(bindingResult.hasErrors()).thenReturn(false)

        // When
        bookingFormController.createBooking(bookingRequest, bindingResult, redirectAttributes)

        // Then
        val bookingCaptor = argumentCaptor<Booking>()
        Mockito.verify(bookingService).createBooking(bookingCaptor.capture())

        val capturedBooking = bookingCaptor.lastValue
        assertEquals("87771112233", capturedBooking.mainVisitorPhone)
    }

    @Test
    fun `test phone number normalization with seven format`() {
        // Given
        val bookingRequest = BookingRequest(
            mainVisitorName = "Test User",
            mainVisitorPhone = "77771112233",
            visitorsCount = 2,
            dateTime = LocalDateTime.now().withMinute(0),
            tableId = "1",
            notes = "Test booking"
        )

        Mockito.`when`(bindingResult.hasErrors()).thenReturn(false)

        // When
        bookingFormController.createBooking(bookingRequest, bindingResult, redirectAttributes)

        // Then
        val bookingCaptor = argumentCaptor<Booking>()
        Mockito.verify(bookingService).createBooking(bookingCaptor.capture())

        val capturedBooking = bookingCaptor.lastValue
        assertEquals("87771112233", capturedBooking.mainVisitorPhone)
    }

    @Test
    fun `test phone number normalization with ten digits format`() {
        // Given
        val bookingRequest = BookingRequest(
            mainVisitorName = "Test User",
            mainVisitorPhone = "7771112233",
            visitorsCount = 2,
            dateTime = LocalDateTime.now().withMinute(0),
            tableId = "1",
            notes = "Test booking"
        )

        Mockito.`when`(bindingResult.hasErrors()).thenReturn(false)

        // When
        bookingFormController.createBooking(bookingRequest, bindingResult, redirectAttributes)

        // Then
        val bookingCaptor = argumentCaptor<Booking>()
        Mockito.verify(bookingService).createBooking(bookingCaptor.capture())

        val capturedBooking = bookingCaptor.lastValue
        assertEquals("87771112233", capturedBooking.mainVisitorPhone)
    }

    @Test
    fun `test phone number normalization with eight format`() {
        // Given
        val bookingRequest = BookingRequest(
            mainVisitorName = "Test User",
            mainVisitorPhone = "87771112233",
            visitorsCount = 2,
            dateTime = LocalDateTime.now().withMinute(0),
            tableId = "1",
            notes = "Test booking"
        )

        Mockito.`when`(bindingResult.hasErrors()).thenReturn(false)

        // When
        bookingFormController.createBooking(bookingRequest, bindingResult, redirectAttributes)

        // Then
        val bookingCaptor = argumentCaptor<Booking>()
        Mockito.verify(bookingService).createBooking(bookingCaptor.capture())

        val capturedBooking = bookingCaptor.lastValue
        assertEquals("87771112233", capturedBooking.mainVisitorPhone)
    }
}