package com.saaresto.kitchen.kitchenadmin.repository

import com.saaresto.kitchen.kitchenadmin.entity.BookingTable
import com.saaresto.kitchen.kitchenadmin.entity.VisitorTable
import com.saaresto.kitchen.kitchenadmin.model.BookingStatus
import com.saaresto.kitchen.kitchenadmin.model.Visitor
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class VisitorRepository {

    /**
     * Find all visitors in the database.
     */
    fun findAll(): List<Visitor> = transaction {
        VisitorTable.selectAll().map { it.toVisitor() }
    }

    /**
     * Find a visitor by ID.
     */
    fun findById(id: UUID): Visitor? = transaction {
        VisitorTable.selectAll().where { VisitorTable.id eq id }
            .map { it.toVisitor() }
            .singleOrNull()
    }

    /**
     * Find a visitor by phone number.
     */
    fun findByPhoneNumber(phoneNumber: String): Visitor? = transaction {
        VisitorTable.selectAll().where { VisitorTable.phoneNumber eq phoneNumber }
            .map { it.toVisitor() }
            .singleOrNull()
    }

    /**
     * Save a new visitor or update an existing one.
     */
    fun save(visitor: Visitor): Visitor = transaction {
        val existingVisitor = VisitorTable.selectAll().where { VisitorTable.id eq visitor.id }.singleOrNull()

        if (existingVisitor == null) {
            // Insert new visitor
            VisitorTable.insert {
                it[id] = visitor.id
                it[phoneNumber] = visitor.phoneNumber
                it[name] = visitor.name
                it[notes] = visitor.notes
            }
        } else {
            // Update existing visitor
            VisitorTable.update({ VisitorTable.id eq visitor.id }) {
                it[phoneNumber] = visitor.phoneNumber
                it[name] = visitor.name
                it[notes] = visitor.notes
            }
        }
        visitor
    }

    /**
     * Delete a visitor by ID.
     */
    fun deleteById(id: UUID): Boolean = transaction {
        VisitorTable.deleteWhere { VisitorTable.id eq id } > 0
    }

    /**
     * Convert a ResultRow to a Visitor model.
     */
    private fun ResultRow.toVisitor(): Visitor = Visitor(
        id = this[VisitorTable.id].value,
        phoneNumber = this[VisitorTable.phoneNumber],
        name = this[VisitorTable.name],
        notes = this[VisitorTable.notes]
    )

    /**
     * Find visitors with confirmed bookings, including the count of confirmed bookings for each visitor.
     * @param page The page number (0-based)
     * @param pageSize The number of items per page
     * @return A list of visitors with confirmed bookings, with the count of confirmed bookings for each visitor
     */
    fun findVisitorsWithConfirmedBookings(page: Int = 0, pageSize: Int = 20): List<Pair<Visitor, Int>> = transaction {
        // Join visitors with bookings on phone number and filter for confirmed bookings
        val query = VisitorTable
            .join(BookingTable, JoinType.INNER, 
                  additionalConstraint = { VisitorTable.phoneNumber eq BookingTable.mainVisitorPhone })
            .slice(VisitorTable.id, VisitorTable.phoneNumber, VisitorTable.name, VisitorTable.notes)
            .select { BookingTable.status eq BookingStatus.CONFIRMED.name }
            .groupBy(VisitorTable.id, VisitorTable.phoneNumber, VisitorTable.name, VisitorTable.notes)

        // Get all visitors with their booking counts
        val visitorsWithCounts = query.map { row ->
            val visitor = Visitor(
                id = row[VisitorTable.id].value,
                phoneNumber = row[VisitorTable.phoneNumber],
                name = row[VisitorTable.name],
                notes = row[VisitorTable.notes]
            )

            // Count confirmed bookings for this visitor
            val bookingCount = BookingTable
                .select { 
                    (BookingTable.mainVisitorPhone eq visitor.phoneNumber) and 
                    (BookingTable.status eq BookingStatus.CONFIRMED.name) 
                }
                .count()

            Pair(visitor, bookingCount.toInt())
        }

        // Sort by booking count in descending order
        val sortedVisitors = visitorsWithCounts.sortedByDescending { it.second }

        // Apply pagination after sorting
        sortedVisitors.drop(page * pageSize).take(pageSize)
    }

    /**
     * Count the total number of visitors with confirmed bookings.
     * @return The total count of visitors with confirmed bookings
     */
    fun countVisitorsWithConfirmedBookings(): Int = transaction {
        VisitorTable
            .join(BookingTable, JoinType.INNER, 
                  additionalConstraint = { VisitorTable.phoneNumber eq BookingTable.mainVisitorPhone })
            .slice(VisitorTable.id)
            .select { BookingTable.status eq BookingStatus.CONFIRMED.name }
            .groupBy(VisitorTable.id)
            .count().toInt()
    }

    /**
     * Find confirmed bookings for a specific visitor by phone number.
     * @param phoneNumber The visitor's phone number
     * @return A list of booking date/times and visitor counts
     */
    fun findConfirmedBookingsForVisitor(phoneNumber: String): List<Pair<java.time.LocalDateTime, Int>> = transaction {
        BookingTable
            .select { 
                (BookingTable.mainVisitorPhone eq phoneNumber) and 
                (BookingTable.status eq BookingStatus.CONFIRMED.name) 
            }
            .orderBy(BookingTable.dateTime to SortOrder.DESC)
            .map { row ->
                Pair(row[BookingTable.dateTime], row[BookingTable.visitorsCount])
            }
    }
}
