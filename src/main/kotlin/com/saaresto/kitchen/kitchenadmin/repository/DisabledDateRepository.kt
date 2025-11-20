package com.saaresto.kitchen.kitchenadmin.repository

import com.saaresto.kitchen.kitchenadmin.entity.DisabledDateTable
import com.saaresto.kitchen.kitchenadmin.model.DisabledDate
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
class DisabledDateRepository {

    /**
     * Find all disabled dates in the database.
     */
    fun findAll(): List<DisabledDate> = transaction {
        DisabledDateTable.selectAll().map { it.toDisabledDate() }
    }

    /**
     * Find a disabled date by ID.
     */
    fun findById(id: UUID): DisabledDate? = transaction {
        DisabledDateTable.selectAll().where { DisabledDateTable.id eq id }
            .map { it.toDisabledDate() }
            .singleOrNull()
    }

    /**
     * Find disabled dates for a specific date.
     */
    fun findByDate(date: LocalDate): List<DisabledDate> = transaction {
        DisabledDateTable.selectAll()
            .where { DisabledDateTable.date eq date }
            .map { it.toDisabledDate() }
    }

    /**
     * Find disabled dates within a date range.
     */
    fun findByDateRange(startDate: LocalDate, endDate: LocalDate): List<DisabledDate> = transaction {
        DisabledDateTable.selectAll()
            .where { 
                (DisabledDateTable.date greaterEq startDate) and
                (DisabledDateTable.date lessEq endDate)
            }
            .map { it.toDisabledDate() }
    }

    /**
     * Find recurring disabled dates.
     */
    fun findRecurring(): List<DisabledDate> = transaction {
        DisabledDateTable.selectAll()
            .where { DisabledDateTable.isRecurring eq true }
            .map { it.toDisabledDate() }
    }

    /**
     * Save a new disabled date or update an existing one.
     */
    fun save(disabledDate: DisabledDate): DisabledDate = transaction {
        val existingDisabledDate = DisabledDateTable.selectAll().where { DisabledDateTable.id eq disabledDate.id }.singleOrNull()

        if (existingDisabledDate == null) {
            // Insert new disabled date
            DisabledDateTable.insert {
                it[id] = disabledDate.id
                it[date] = disabledDate.date
                it[startTime] = disabledDate.startTime
                it[endTime] = disabledDate.endTime
                it[description] = disabledDate.description
                it[isRecurring] = disabledDate.isRecurring
                it[createdAt] = disabledDate.createdAt
            }
        } else {
            // Update existing disabled date
            DisabledDateTable.update({ DisabledDateTable.id eq disabledDate.id }) {
                it[date] = disabledDate.date
                it[startTime] = disabledDate.startTime
                it[endTime] = disabledDate.endTime
                it[description] = disabledDate.description
                it[isRecurring] = disabledDate.isRecurring
                it[createdAt] = disabledDate.createdAt
            }
        }
        disabledDate
    }

    /**
     * Delete a disabled date by ID.
     */
    fun deleteById(id: UUID): Boolean = transaction {
        DisabledDateTable.deleteWhere { DisabledDateTable.id eq id } > 0
    }

    /**
     * Convert a ResultRow to a DisabledDate model.
     */
    private fun ResultRow.toDisabledDate(): DisabledDate = DisabledDate(
        id = this[DisabledDateTable.id].value,
        date = this[DisabledDateTable.date],
        startTime = this[DisabledDateTable.startTime],
        endTime = this[DisabledDateTable.endTime],
        description = this[DisabledDateTable.description],
        isRecurring = this[DisabledDateTable.isRecurring],
        createdAt = this[DisabledDateTable.createdAt]
    )
}