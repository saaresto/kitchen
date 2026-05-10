package com.saaresto.kitchen.kitchenadmin.config

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.saaresto.kitchen.kitchenadmin.service.StaffService
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BotConfig {

    @Value("\${notification.token}")
    lateinit var botToken: String

    @PostConstruct
    fun configureHttpClientProperties() {
        // AGGRESSIVE MEMORY OPTIMIZATION - Disable all caching and limit connections
        System.setProperty("http.keepAlive", "false")
        System.setProperty("http.maxConnections", "1")
        System.setProperty("http.maxRedirects", "1")
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true")

        // Completely disable OkHttp caching and optimize for minimal memory usage
        System.setProperty("okhttp.platform.conscrypt", "false")
        System.setProperty("okhttp3.OkHttpClient.cache.size", "0") // Disable cache entirely
        System.setProperty("okhttp3.OkHttpClient.connectionPool.maxIdleConnections", "1")
        System.setProperty("okhttp3.OkHttpClient.connectionPool.keepAliveDuration", "5000") // 5 seconds
        System.setProperty("okhttp3.OkHttpClient.connectTimeout", "10000") // 10 seconds
        System.setProperty("okhttp3.OkHttpClient.readTimeout", "15000") // 15 seconds
        System.setProperty("okhttp3.OkHttpClient.writeTimeout", "15000") // 15 seconds

        // Disable HTTP/2 and force HTTP/1.1 to avoid buffer accumulation issues
        System.setProperty("http2.disable", "true")
        System.setProperty("jdk.httpclient.HttpClient.log", "errors")
        System.setProperty("jdk.httpclient.allowRestrictedHeaders", "true")

        // Additional JVM optimizations for memory management
        System.setProperty("java.net.useSystemProxies", "false")
        System.setProperty("networkaddress.cache.ttl", "60")
        System.setProperty("networkaddress.cache.negative.ttl", "10")

        // Force garbage collection more frequently
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "1")

        // Telegram bot specific optimizations
        System.setProperty("telegram.bot.connectionTimeout", "10000")
        System.setProperty("telegram.bot.readTimeout", "15000")
    }

    @Bean
    fun bot(staffService: StaffService): Bot {
        return com.github.kotlintelegrambot.bot {

            token = botToken

            dispatch {
                text {
                    if (message.text != null && message.text!!.contains("@")) {
                        if (!message.text!!.contains(" ")) {
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
