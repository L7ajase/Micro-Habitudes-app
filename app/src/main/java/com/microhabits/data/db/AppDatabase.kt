package com.microhabits.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.microhabits.data.model.Completion
import com.microhabits.data.model.Habit

@Database(
    entities = [Habit::class, Completion::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
}
