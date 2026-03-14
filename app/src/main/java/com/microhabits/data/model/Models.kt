package com.microhabits.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val emoji: String,
    val durationMinutes: Int,
    val xpReward: Int,
    val colorHex: String,       // e.g. "#6C63FF"
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "completions")
data class Completion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitId: Int,
    val completedDate: String,  // ISO yyyy-MM-dd
    val completedAt: Long = System.currentTimeMillis()
)

data class HabitWithStatus(
    val habit: Habit,
    val completedToday: Boolean,
    val streakDays: Int
)

data class DailyStats(
    val date: String,
    val completedCount: Int,
    val totalXp: Int
)
