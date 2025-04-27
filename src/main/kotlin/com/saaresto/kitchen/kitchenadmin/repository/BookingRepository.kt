package com.saaresto.kitchen.kitchenadmin.repository

import com.saaresto.kitchen.kitchenadmin.entity.BookingTable
import com.saaresto.kitchen.kitchenadmin.model.Booking
import com.saaresto.kitchen.kitchenadmin.model.BookingStatus
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
class BookingRepository {

    /**
     * Find all bookings in the database.
     */
    fun findAll(): List<Booking> = transaction {
        BookingTable.selectAll().map { it.toBooking() }
    }

    /**
     * Find a booking by ID.
     */
    fun findById(id: UUID): Booking? = transaction {
        BookingTable.selectAll().where { BookingTable.id eq id }
            .map { it.toBooking() }
            .singleOrNull()
    }

    /**
     * Find bookings by status.
     */
    fun findByStatus(status: BookingStatus): List<Booking> = transaction {
        BookingTable.selectAll().where { BookingTable.status eq status.name }
            .map { it.toBooking() }
    }

    /**
     * Find bookings for a specific date.
     */
    fun findByDate(date: LocalDateTime): List<Booking> = transaction {
        val startOfDay = date.toLocalDate().atStartOfDay()
        val endOfDay = startOfDay.plusDays(1)
        
        BookingTable.selectAll()
            .where { 
                (BookingTable.dateTime greaterEq startOfDay) and
                (BookingTable.dateTime less endOfDay)
            }
            .map { it.toBooking() }
    }

    /**
     * Save a new booking or update an existing one.
     */
    fun save(booking: Booking): Booking = transaction {
        val existingBooking = BookingTable.selectAll().where { BookingTable.id eq booking.id }.singleOrNull()

        if (existingBooking == null) {
            // Insert new booking
            BookingTable.insert {
                it[id] = booking.id
                it[status] = booking.status.name
                it[mainVisitorName] = booking.mainVisitorName
                it[mainVisitorPhone] = booking.mainVisitorPhone
                it[visitorsCount] = booking.visitorsCount
                it[dateTime] = booking.dateTime
                it[tableId] = booking.tableId
                it[notes] = booking.notes
            }
        } else {
            // Update existing booking
            BookingTable.update({ BookingTable.id eq booking.id }) {
                it[status] = booking.status.name
                it[mainVisitorName] = booking.mainVisitorName
                it[mainVisitorPhone] = booking.mainVisitorPhone
                it[visitorsCount] = booking.visitorsCount
                it[dateTime] = booking.dateTime
                it[tableId] = booking.tableId
                it[notes] = booking.notes
            }
        }
        booking
    }

    /**
     * Delete a booking by ID.
     */
    fun deleteById(id: UUID): Boolean = transaction {
        BookingTable.deleteWhere { BookingTable.id eq id } > 0
    }

    /**
     * Convert a ResultRow to a Booking model.
     */
    private fun ResultRow.toBooking(): Booking = Booking(
        id = this[BookingTable.id].value,
        status = BookingStatus.valueOf(this[BookingTable.status]),
        mainVisitorName = this[BookingTable.mainVisitorName],
        mainVisitorPhone = this[BookingTable.mainVisitorPhone],
        visitorsCount = this[BookingTable.visitorsCount],
        dateTime = this[BookingTable.dateTime],
        tableId = this[BookingTable.tableId],
        notes = this[BookingTable.notes]
    )
}