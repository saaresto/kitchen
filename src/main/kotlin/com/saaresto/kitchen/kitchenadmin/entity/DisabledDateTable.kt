package com.saaresto.kitchen.kitchenadmin.entity

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.time
import java.time.LocalDate
import java.time.LocalTime

/**
 * Exposed table definition for the DisabledDate entity.
 */
object DisabledDateTable : UUIDTable("disabled_dates") {
    val date: Column<LocalDate> = date("date")
    val startTime: Column<LocalTime?> = time("start_time").nullable()
    val endTime: Column<LocalTime?> = time("end_time").nullable()
    val description: Column<String?> = text("description").nullable()
    val isRecurring: Column<Boolean> = bool("is_recurring")
    val createdAt: Column<LocalDate> = date("created_at")
}