package com.saaresto.kitchen.kitchenadmin.service

import com.saaresto.kitchen.kitchenadmin.model.StaffMember
import com.saaresto.kitchen.kitchenadmin.repository.StaffRepository
import org.springframework.stereotype.Service

@Service
class StaffService(
    private val staffRepository: StaffRepository
) {

    /**
     * Create a new staff member.
     */
    fun createStaffMember(username: String, chatId: String): StaffMember {
        val staffMember = StaffMember(
            username = username,
            chatId = chatId
        )
        return staffRepository.save(staffMember)
    }

    /**
     * Get all staff members.
     */
    fun getAllStaffMembers(): List<StaffMember> {
        return staffRepository.findAll()
    }

}
