package com.lifeops.app.util

import android.util.Log
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
        val date = LocalDate.now().plusDays(debugDaysOffset)
        Log.d("DateProvider", "now() called - returning: $date (offset: $debugDaysOffset)")
        return date
    }
    
    /**
     * Advance the debug date by the specified number of days
     */
    fun advanceDebugDate(days: Int) {
        Log.d("DateProvider", "advanceDebugDate($days) - old offset: $debugDaysOffset")
        debugDaysOffset += days
        Log.d("DateProvider", "advanceDebugDate($days) - new offset: $debugDaysOffset, new date: ${now()}")
    }
    
    /**
     * Get the current debug offset
     */
    fun getDebugOffset(): Long = debugDaysOffset
}
