package com.saaresto.kitchen.kitchenadmin

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KitchenAdminApplication(
    private val bot: Bot
) {
    @PostConstruct
    fun init() {
        bot.startPolling()
    }
}

fun main(args: Array<String>) {
    runApplication<KitchenAdminApplication>(*args)
}