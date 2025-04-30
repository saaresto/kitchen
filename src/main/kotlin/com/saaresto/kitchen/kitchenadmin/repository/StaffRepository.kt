package com.saaresto.kitchen.kitchenadmin.repository

import com.saaresto.kitchen.kitchenadmin.entity.StaffMemberTable
import com.saaresto.kitchen.kitchenadmin.model.StaffMember
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class StaffRepository {

    /**
     * Find all staff members in the database.
     */
    fun findAll(): List<StaffMember> = transaction {
        StaffMemberTable.selectAll().map { it.toStaffMember() }
    }

    /**
     * Find a staff member by ID.
     */
    fun findById(id: UUID): StaffMember? = transaction {
        StaffMemberTable.selectAll().where { StaffMemberTable.id eq id }
            .map { it.toStaffMember() }
            .singleOrNull()
    }

    /**
     * Save a new staff member or update an existing one.
     */
    fun save(staffMember: StaffMember): StaffMember = transaction {
        val existingStaffMember = StaffMemberTable.selectAll().where { StaffMemberTable.id eq staffMember.id }.singleOrNull()

        if (existingStaffMember == null) {
            // Insert new staff member
            StaffMemberTable.insert {
                it[id] = staffMember.id
                it[username] = staffMember.username
                it[chatId] = staffMember.chatId
            }
        } else {
            // Update existing staff member
            StaffMemberTable.update({ StaffMemberTable.id eq staffMember.id }) {
                it[username] = staffMember.username
                it[chatId] = staffMember.chatId
            }
        }
        staffMember
    }

    /**
     * Delete a staff member by ID.
     */
    fun deleteById(id: UUID): Boolean = transaction {
        StaffMemberTable.deleteWhere { StaffMemberTable.id eq id } > 0
    }

    /**
     * Convert a ResultRow to a StaffMember model.
     */
    private fun ResultRow.toStaffMember(): StaffMember = StaffMember(
        id = this[StaffMemberTable.id].value,
        username = this[StaffMemberTable.username],
        chatId = this[StaffMemberTable.chatId]
    )
}
