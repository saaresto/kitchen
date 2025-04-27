package com.saaresto.kitchen.kitchenadmin.controller

import com.saaresto.kitchen.kitchenadmin.dto.VisitorRequest
import com.saaresto.kitchen.kitchenadmin.dto.VisitorResponse
import com.saaresto.kitchen.kitchenadmin.model.Visitor
import com.saaresto.kitchen.kitchenadmin.service.VisitorService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/visitors")
class VisitorController(private val visitorService: VisitorService) {

    @GetMapping
    fun getAllVisitors(): ResponseEntity<List<VisitorResponse>> {
        val visitors = visitorService.getAllVisitors()
        return ResponseEntity.ok(visitors.map { VisitorResponse.fromVisitor(it) })
    }

    @GetMapping("/{id}")
    fun getVisitorById(@PathVariable id: UUID): ResponseEntity<VisitorResponse> {
        return try {
            val visitor = visitorService.getVisitorById(id)
            ResponseEntity.ok(VisitorResponse.fromVisitor(visitor))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun createVisitor(@RequestBody request: VisitorRequest): ResponseEntity<VisitorResponse> {
        val visitor = Visitor(
            phoneNumber = request.phoneNumber,
            name = request.name,
            notes = request.notes
        )
        val createdVisitor = visitorService.createVisitor(visitor)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(VisitorResponse.fromVisitor(createdVisitor))
    }

    @PutMapping("/{id}")
    fun updateVisitor(
        @PathVariable id: UUID,
        @RequestBody request: VisitorRequest
    ): ResponseEntity<VisitorResponse> {
        return try {
            val visitor = Visitor(
                id = id,
                phoneNumber = request.phoneNumber,
                name = request.name,
                notes = request.notes
            )
            val updatedVisitor = visitorService.updateVisitor(id, visitor)
            ResponseEntity.ok(VisitorResponse.fromVisitor(updatedVisitor))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    fun deleteVisitor(@PathVariable id: UUID): ResponseEntity<Unit> {
        return try {
            visitorService.deleteVisitor(id)
            ResponseEntity.noContent().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }
}