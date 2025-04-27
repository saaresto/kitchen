package com.saaresto.kitchen.kitchenadmin.entity

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import java.util.UUID

/**
 * Exposed table definition for the Visitor entity.
 */
object VisitorTable : UUIDTable("visitors") {
    val phoneNumber: Column<String> = varchar("phone_number", 20)
    val name: Column<String> = varchar("name", 255)
    val notes: Column<String?> = varchar("notes", 1000).nullable()
}