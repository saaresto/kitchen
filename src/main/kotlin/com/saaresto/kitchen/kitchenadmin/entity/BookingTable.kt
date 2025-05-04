package com.saaresto.kitchen.kitchenadmin.entity

import com.saaresto.kitchen.kitchenadmin.model.BookingStatus
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.util.UUID

/**
 * Exposed table definition for the Booking entity.
 */
object BookingTable : UUIDTable("bookings") {
    val status: Column<String> = varchar("status", 20)
    val mainVisitorName: Column<String> = varchar("main_visitor_name", 255)
    val mainVisitorPhone: Column<String> = varchar("main_visitor_phone", 20)
    val visitorsCount: Column<Int> = integer("visitors_count")
    val dateTime: Column<LocalDateTime> = datetime("date_time")
    val tableId: Column<String> = varchar("table_id", 50)
    val notes: Column<String?> = text("notes").nullable()
    val createdAt: Column<LocalDateTime> = datetime("created_at")
}
