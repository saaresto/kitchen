package com.saaresto.kitchen.kitchenadmin.service

import com.saaresto.kitchen.kitchenadmin.model.Visitor
import com.saaresto.kitchen.kitchenadmin.repository.VisitorRepository
import org.springframework.stereotype.Service
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
     */
    fun createVisitor(visitor: Visitor): Visitor = visitorRepository.save(visitor)

    /**
     * Update an existing visitor.
     * @throws NoSuchElementException if visitor not found
     */
    fun updateVisitor(id: UUID, visitor: Visitor): Visitor {
        // Check if visitor exists
        visitorRepository.findById(id)
            ?: throw NoSuchElementException("Visitor with ID $id not found")
        
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
}