package com.saaresto.kitchen.kitchenadmin.service

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.saaresto.kitchen.kitchenadmin.model.Booking
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
class NotificationService(
    private val bot: Bot,
    private val staffService: StaffService
) {

    /**
     * Send a notification to all staff members about a new booking.
     */
    fun sendBookingNotification(booking: Booking) {
        val staffMembers = staffService.getAllStaffMembers()

        if (staffMembers.isEmpty()) {
            return
        }

        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        val message = """
            🔔 *New Booking Alert!* 🔔

            👤 *Visitor:* ${booking.mainVisitorName}
            📱 *Phone:* ${booking.mainVisitorPhone}
            📅 *Date:* ${booking.dateTime.format(dateFormatter)}
            🕒 *Time:* ${booking.dateTime.format(timeFormatter)}
            👥 *Guests:* ${booking.visitorsCount}
            ${if (booking.notes != null && booking.notes.isNotEmpty()) "📝 *Notes:* ${booking.notes}" else ""}
        """.trimIndent()

        staffMembers.forEach { staffMember ->
            try {
                bot.sendMessage(
                    chatId = ChatId.fromId(staffMember.chatId.toLong()),
                    text = message,
                    parseMode = ParseMode.MARKDOWN
                )
            } catch (e: Exception) {
                // Log the error but continue with other staff members
                println("Failed to send notification to staff member ${staffMember.username}: ${e.message}")
            }
        }
    }

}
