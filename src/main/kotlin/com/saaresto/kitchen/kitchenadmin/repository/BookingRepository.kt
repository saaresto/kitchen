package com.saaresto.kitchen.kitchenadmin.repository

import com.saaresto.kitchen.kitchenadmin.entity.BookingTable
import com.saaresto.kitchen.kitchenadmin.model.Booking
import com.saaresto.kitchen.kitchenadmin.model.BookingStatus
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
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
     * Find bookings by status, sorted by createdAt (earliest first).
     */
    fun findByStatusOrderByCreatedAt(status: BookingStatus): List<Booking> = transaction {
        BookingTable.selectAll()
            .where { BookingTable.status eq status.name }
            .orderBy(BookingTable.createdAt to SortOrder.ASC)
            .map { it.toBooking() }
    }

    /**
     * Find bookings by multiple statuses, sorted by createdAt (earliest first).
     */
    fun findByStatusesOrderByCreatedAt(statuses: List<BookingStatus>): List<Booking> = transaction {
        BookingTable.selectAll()
            .where { BookingTable.status inList statuses.map { it.name } }
            .orderBy(BookingTable.createdAt to SortOrder.ASC)
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
     * Find bookings for a specific date with optional visitor name and phone filters.
     */
    fun findByDateAndFilters(date: LocalDateTime, visitorName: String? = null, visitorPhone: String? = null): List<Booking> = transaction {
        val startOfDay = date.toLocalDate().atStartOfDay()
        val endOfDay = startOfDay.plusDays(1)

        var query = BookingTable.selectAll()
            .where { 
                (BookingTable.dateTime greaterEq startOfDay) and
                (BookingTable.dateTime less endOfDay)
            }

        if (!visitorName.isNullOrBlank()) {
            query = query.andWhere { BookingTable.mainVisitorName.lowerCase() like "%${visitorName.lowercase()}%" }
        }

        if (!visitorPhone.isNullOrBlank()) {
            query = query.andWhere { BookingTable.mainVisitorPhone like "%${visitorPhone}%" }
        }

        query.map { it.toBooking() }
    }

    /**
     * Find bookings within a date range with optional visitor name and phone filters.
     */
    fun findByDateRangeAndFilters(
        startDate: LocalDateTime, 
        endDate: LocalDateTime, 
        visitorName: String? = null, 
        visitorPhone: String? = null
    ): List<Booking> = transaction {
        val startOfDay = startDate.toLocalDate().atStartOfDay()
        val endOfDay = endDate.toLocalDate().plusDays(1).atStartOfDay()

        var query = BookingTable.selectAll()
            .where { 
                (BookingTable.dateTime greaterEq startOfDay) and
                (BookingTable.dateTime less endOfDay)
            }

        if (!visitorName.isNullOrBlank()) {
            query = query.andWhere { BookingTable.mainVisitorName.lowerCase() like "%${visitorName.lowercase()}%" }
        }

        if (!visitorPhone.isNullOrBlank()) {
            query = query.andWhere { BookingTable.mainVisitorPhone like "%${visitorPhone}%" }
        }

        query.orderBy(BookingTable.createdAt to SortOrder.DESC)
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
                it[createdAt] = booking.createdAt
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
                it[createdAt] = booking.createdAt
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
        notes = this[BookingTable.notes],
//        createdAt = this[BookingTable.createdAt].atZone(ZoneId.ofOffset("UTC", ZoneOffset.ofHours(5))).toLocalDateTime()
        createdAt = this[BookingTable.createdAt].plusHours(5)
    )
}
