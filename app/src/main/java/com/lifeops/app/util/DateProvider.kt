package com.lifeops.app.util

import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides the current date for the app
 * Can be overridden for debugging/testing purposes
 */
@Singleton
class DateProvider @Inject constructor() {
    
    /**
     * Debug offset in days (0 = real today)
     */
    private var debugDaysOffset: Long = 0
    
    /**
     * Get the current date (with debug offset applied)
     */
    fun now(): LocalDate {
        return LocalDate.now().plusDays(debugDaysOffset)
    }
    
    /**
     * Advance the debug date by the specified number of days
     */
    fun advanceDebugDate(days: Int) {
        debugDaysOffset += days
    }
    
    /**
     * Reset debug date to real today
     */
    fun resetDebugDate() {
        debugDaysOffset = 0
    }
    
    /**
     * Get the current debug offset
     */
    fun getDebugOffset(): Long = debugDaysOffset
}
