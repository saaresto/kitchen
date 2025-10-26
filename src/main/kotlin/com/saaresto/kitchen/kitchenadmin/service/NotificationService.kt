package com.saaresto.kitchen.kitchenadmin.service

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.saaresto.kitchen.kitchenadmin.model.Booking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
class NotificationService(
    private val bot: Bot,
    private val staffService: StaffService
) {
    private val logger = LoggerFactory.getLogger(NotificationService::class.java)

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
            📱 *Phone:* ${
            booking.mainVisitorPhone.let { phone ->
                if (phone.startsWith("+")) phone else if (phone.startsWith(
                        "8"
                    )
                ) "+7${phone.substring(1)}" else phone
            }
        }
            📅 *Date:* ${booking.dateTime.format(dateFormatter)}
            🕒 *Time:* ${booking.dateTime.format(timeFormatter)}
            👥 *Guests:* ${booking.visitorsCount}
            ${if (booking.notes != null && booking.notes.isNotEmpty()) "📝 *Notes:* ${booking.notes}" else ""}
        """.trimIndent()

        staffMembers.forEach { staffMember ->
            try {
                logger.info("Sending notification to staff member with chat ID: {}", staffMember.chatId)

                bot.sendMessage(
                    chatId = ChatId.fromId(staffMember.chatId.toLong()),
                    text = message,
                    parseMode = ParseMode.MARKDOWN
                )
            } catch (e: Exception) {
                // Log the error but continue with other staff members
                logger.error("Failed to send notification to staff member ${staffMember.username}: ${e.message}")
            }
        }
    }

}
