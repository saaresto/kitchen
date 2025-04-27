package com.saaresto.kitchen.kitchenadmin.dto

import com.saaresto.kitchen.kitchenadmin.model.Visitor
import java.util.UUID

/**
 * DTO for creating or updating a visitor.
 */
data class VisitorRequest(
    val phoneNumber: String,
    val name: String,
    val notes: String? = null
)

/**
 * DTO for visitor response.
 */
data class VisitorResponse(
    val id: UUID,
    val phoneNumber: String,
    val name: String,
    val notes: String?
) {
    companion object {
        fun fromVisitor(visitor: Visitor): VisitorResponse = VisitorResponse(
            id = visitor.id,
            phoneNumber = visitor.phoneNumber,
            name = visitor.name,
            notes = visitor.notes
        )
    }
}