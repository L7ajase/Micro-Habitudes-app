package com.microhabits.data.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val emoji: String,
    val durationMinutes: Int,
    val xpReward: Int,
    val colorHex: String,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "completions")
data class Completion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitId: Int,
    val completedDate: String,
    val completedAt: Long = System.currentTimeMillis()
)

@Immutable
data class HabitWithStatus(
    val habit: Habit,
    val completedToday: Boolean,
    val streakDays: Int
)

@Immutable
data class DailyStats(
    val date: String,
    val completedCount: Int,
    val totalXp: Int
)