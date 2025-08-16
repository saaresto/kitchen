package com.saaresto.kitchen.kitchenadmin.service

import com.saaresto.kitchen.kitchenadmin.model.Visitor
import com.saaresto.kitchen.kitchenadmin.repository.VisitorRepository
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class VisitorService(private val visitorRepository: VisitorRepository) {

    /**
     * Get all visitors.
     */
    fun getAllVisitors(): List<Visitor> = visitorRepository.findAll()

    /**
     * Get a visitor by ID.
     * @throws NoSuchElementException if visitor not found
     */
    fun getVisitorById(id: UUID): Visitor = visitorRepository.findById(id)
        ?: throw NoSuchElementException("Visitor with ID $id not found")

    /**
     * Create a new visitor.
     * @throws DuplicateKeyException if a visitor with the same phone number already exists
     */
    fun createVisitor(visitor: Visitor): Visitor {
        return visitorRepository.findByPhoneNumber(visitor.phoneNumber.replace(Regex("[^0-9]"), ""))
            ?: visitorRepository.save(visitor)
    }

    /**
     * Update an existing visitor.
     * @throws NoSuchElementException if visitor not found
     * @throws DuplicateKeyException if another visitor with the same phone number already exists
     */
    fun updateVisitor(id: UUID, visitor: Visitor): Visitor {
        // Check if visitor exists
        visitorRepository.findById(id)
            ?: throw NoSuchElementException("Visitor with ID $id not found")

        // Check if another visitor with the same phone number already exists
        visitorRepository.findByPhoneNumber(visitor.phoneNumber.replace(Regex("[^0-9]"), ""))?.let {
            if (it.id != id) {
                throw DuplicateKeyException("Another visitor with phone number ${visitor.phoneNumber} already exists")
            }
        }

        // Update with the provided ID
        return visitorRepository.save(visitor.copy(id = id))
    }

    /**
     * Delete a visitor by ID.
     * @throws NoSuchElementException if visitor not found
     */
    fun deleteVisitor(id: UUID) {
        if (!visitorRepository.deleteById(id)) {
            throw NoSuchElementException("Visitor with ID $id not found")
        }
    }

    /**
     * Get visitors with confirmed bookings, including the count of confirmed bookings for each visitor.
     * @param page The page number (0-based)
     * @param pageSize The number of items per page
     * @return A list of visitors with confirmed bookings, with the count of confirmed bookings for each visitor
     */
    fun getVisitorsWithConfirmedBookings(page: Int = 0, pageSize: Int = 20): List<Pair<Visitor, Int>> {
        return visitorRepository.findVisitorsWithConfirmedBookings(page, pageSize)
    }

    /**
     * Get the total number of visitors with confirmed bookings.
     * @return The total count of visitors with confirmed bookings
     */
    fun countVisitorsWithConfirmedBookings(): Int {
        return visitorRepository.countVisitorsWithConfirmedBookings()
    }

    /**
     * Get confirmed bookings for a specific visitor by phone number.
     * @param phoneNumber The visitor's phone number
     * @return A list of booking date/times and visitor counts
     */
    fun getConfirmedBookingsForVisitor(phoneNumber: String): List<Pair<LocalDateTime, Int>> {
        return visitorRepository.findConfirmedBookingsForVisitor(phoneNumber)
    }
}
