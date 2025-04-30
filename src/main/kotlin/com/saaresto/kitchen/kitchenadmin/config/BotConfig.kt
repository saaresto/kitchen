package com.saaresto.kitchen.kitchenadmin.config

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.saaresto.kitchen.kitchenadmin.service.StaffService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BotConfig {

    @Value("\${notification.token}")
    lateinit var botToken: String

    @Bean
    fun bot(staffService: StaffService): Bot {
        return com.github.kotlintelegrambot.bot {

            token = botToken

            dispatch {
                text {
                    if (message.text != null && message.text!!.contains("@")) {
                        val username = message.text!!.split("@").first()
                        val pwd = message.text!!.split("@").last()
                        if (pwd.equals("makaroshk1")) {
                            staffService.createStaffMember(
                                username = message.from?.username ?: username,
                                message.chat.id.toString()
                            )
                            bot.sendMessage(
                                chatId = ChatId.fromId(message.chat.id),
                                text = "Vamos, $username!",
                                protectContent = true,
                                disableNotification = false,
                            )
                        } else {
                            bot.sendMessage(
                                chatId = ChatId.fromId(message.chat.id),
                                text = "Sorry po",
                                protectContent = true,
                                disableNotification = false,
                            )
                        }
                    }
                }

                command("start") {
                    val result = bot.sendMessage(
                        chatId = ChatId.fromId(message.chat.id),
                        text = "Now send your desired username and password (provided by Paw) like following: username@password"
                    )
                }
            }

        }
    }

}