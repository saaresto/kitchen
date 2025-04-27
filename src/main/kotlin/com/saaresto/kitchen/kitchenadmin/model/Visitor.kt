package com.saaresto.kitchen.kitchenadmin.model

import java.util.UUID

/**
 * Entity representing a restaurant visitor.
 */
data class Visitor(
    val id: UUID = UUID.randomUUID(),
    val phoneNumber: String,
    val name: String,
    val notes: String? = null
)