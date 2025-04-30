package com.saaresto.kitchen.kitchenadmin.model

import java.util.UUID

/**
 * Entity representing a staff member.
 */
data class StaffMember(
    val id: UUID = UUID.randomUUID(),
    val username: String,
    val chatId: String
)