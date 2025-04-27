package com.saaresto.kitchen.kitchenadmin.repository

import com.saaresto.kitchen.kitchenadmin.entity.VisitorTable
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
}
