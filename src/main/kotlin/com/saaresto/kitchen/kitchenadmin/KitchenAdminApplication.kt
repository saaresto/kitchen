package com.saaresto.kitchen.kitchenadmin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KitchenAdminApplication

fun main(args: Array<String>) {
    runApplication<KitchenAdminApplication>(*args)
}
