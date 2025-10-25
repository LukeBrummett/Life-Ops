package com.lifeops.app.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lifeops.app.data.local.entity.DayOfWeek
import com.lifeops.app.data.local.entity.Difficulty
import com.lifeops.app.data.local.entity.IntervalUnit
import java.time.LocalDate

/**
 * Room Type Converters for handling complex data types
 * Converts between Kotlin types and types that SQLite can store
 */
class Converters {
    
    private val gson = Gson()
    
    // ============================================
    // LocalDate Converters
    // ============================================
    
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }
    
    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it) }
    }
    
    // ============================================
    // List<String> Converters (for task IDs - UUIDs)
    // ============================================
    
    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return list?.let { gson.toJson(it) }
    }
    
    @TypeConverter
    fun toStringList(json: String?): List<String>? {
        if (json == null) return null
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type)
    }
    
    // ============================================
    // List<LocalDate> Converters (for excluded dates)
    // ============================================
    
    @TypeConverter
    fun fromLocalDateList(list: List<LocalDate>?): String? {
        return list?.let { dates ->
            gson.toJson(dates.map { it.toString() })
        }
    }
    
    @TypeConverter
    fun toLocalDateList(json: String?): List<LocalDate>? {
        if (json == null) return null
        val type = object : TypeToken<List<String>>() {}.type
        val dateStrings: List<String> = gson.fromJson(json, type)
        return dateStrings.map { LocalDate.parse(it) }
    }
    
    // ============================================
    // List<DayOfWeek> Converters
    // ============================================
    
    @TypeConverter
    fun fromDayOfWeekList(list: List<DayOfWeek>?): String? {
        return list?.let { days ->
            gson.toJson(days.map { it.name })
        }
    }
    
    @TypeConverter
    fun toDayOfWeekList(json: String?): List<DayOfWeek>? {
        if (json == null) return null
        val type = object : TypeToken<List<String>>() {}.type
        val dayStrings: List<String> = gson.fromJson(json, type)
        return dayStrings.map { DayOfWeek.valueOf(it) }
    }
    
    // ============================================
    // Enum Converters
    // ============================================
    
    @TypeConverter
    fun fromIntervalUnit(unit: IntervalUnit): String {
        return unit.name
    }
    
    @TypeConverter
    fun toIntervalUnit(name: String): IntervalUnit {
        return IntervalUnit.valueOf(name)
    }
    
    @TypeConverter
    fun fromDifficulty(difficulty: Difficulty?): String? {
        return difficulty?.name
    }
    
    @TypeConverter
    fun toDifficulty(name: String?): Difficulty? {
        return name?.let { Difficulty.valueOf(it) }
    }
}
