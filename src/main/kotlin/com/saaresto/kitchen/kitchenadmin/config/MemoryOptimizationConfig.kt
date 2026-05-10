package com.saaresto.kitchen.kitchenadmin.config

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import java.lang.management.ManagementFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Configuration
class MemoryOptimizationConfig {
    
    private val logger = LoggerFactory.getLogger(MemoryOptimizationConfig::class.java)
    
    @PostConstruct
    fun configureMemoryOptimizations() {
        // Set aggressive JVM memory management properties
        System.setProperty("java.awt.headless", "true")
        System.setProperty("file.encoding", "UTF-8")
        
        // Optimize garbage collection
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "1")
        
        // Reduce thread stack size
        System.setProperty("java.lang.Thread.stackSize", "256k")
        
        // Optimize NIO and networking
        System.setProperty("java.nio.channels.DefaultThreadPool.threadFactory", "cached")
        System.setProperty("java.nio.channels.DefaultThreadPool.initialSize", "1")
        
        // Log current memory settings
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory() / 1024 / 1024
        val totalMemory = runtime.totalMemory() / 1024 / 1024
        val freeMemory = runtime.freeMemory() / 1024 / 1024
        
        logger.info("Memory configuration - Max: {}MB, Total: {}MB, Free: {}MB", maxMemory, totalMemory, freeMemory)
        
        // Schedule periodic garbage collection
        schedulePeriodicGC()
    }
    
    private fun schedulePeriodicGC() {
        val scheduler = Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r, "memory-cleanup").apply {
                isDaemon = true
                priority = Thread.MIN_PRIORITY
            }
        }
        
        scheduler.scheduleAtFixedRate({
            try {
                val beforeGC = Runtime.getRuntime().freeMemory()
                System.gc()
                val afterGC = Runtime.getRuntime().freeMemory()
                val freedMemory = (afterGC - beforeGC) / 1024 / 1024
                
                if (freedMemory > 10) { // Only log if significant memory was freed
                    logger.debug("Periodic GC freed {}MB of memory", freedMemory)
                }
            } catch (e: Exception) {
                logger.warn("Error during periodic GC: {}", e.message)
            }
        }, 2, 2, TimeUnit.MINUTES) // Run every 2 minutes
    }
}