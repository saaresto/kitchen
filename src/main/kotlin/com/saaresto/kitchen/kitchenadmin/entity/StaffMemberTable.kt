package com.saaresto.kitchen.kitchenadmin.entity

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import java.util.UUID

/**
 * Exposed table definition for the StaffMember entity.
 */
object StaffMemberTable : UUIDTable("staff_members") {
    val username: Column<String> = varchar("username", 255)
    val chatId: Column<String> = varchar("chat_id", 255)
}