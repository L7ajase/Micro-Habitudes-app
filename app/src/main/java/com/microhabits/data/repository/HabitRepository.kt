package com.microhabits.data.repository

import com.microhabits.data.db.HabitDao
import com.microhabits.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepository @Inject constructor(private val dao: HabitDao) {

    val allHabits: Flow<List<Habit>> = dao.getAllHabits()
    val totalXp: Flow<Int?> = dao.getTotalXp()
    val totalActiveDays: Flow<Int> = dao.getTotalActiveDays()
    val totalCompletions: Flow<Int> = dao.getTotalCompletions()

    fun getHabitsWithStatus(date: String): Flow<List<HabitWithStatus>> =
        combine(
            dao.getAllHabits(),
            dao.getCompletionsForDate(date)
        ) { habits, completions ->
            val completedIds = completions.map { it.habitId }.toSet()
            habits.map { habit ->
                HabitWithStatus(
                    habit = habit,
                    completedToday = habit.id in completedIds,
                    streakDays = calculateStreak(habit.id)
                )
            }
        }

    fun getDailyStats(fromDate: String): Flow<List<DailyStats>> =
        dao.getDailyStats(fromDate)

    suspend fun addHabit(habit: Habit) = dao.insertHabit(habit)

    suspend fun updateHabit(habit: Habit) = dao.updateHabit(habit)

    suspend fun deleteHabit(habit: Habit) = dao.deleteHabit(habit)

    suspend fun updateSortOrders(habits: List<Habit>) {
        habits.forEachIndexed { index, habit ->
            dao.updateSortOrder(habit.id, index)
        }
    }

    suspend fun toggleCompletion(habitId: Int, date: String, completed: Boolean) {
        if (completed) {
            dao.insertCompletion(Completion(habitId = habitId, completedDate = date))
        } else {
            dao.deleteCompletion(habitId, date)
        }
    }

    private suspend fun calculateStreak(habitId: Int): Int {
        val dates = dao.getRecentCompletionDates(habitId)
            .map { LocalDate.parse(it) }
            .sortedDescending()
        if (dates.isEmpty()) return 0
        var streak = 0
        var expected = LocalDate.now()
        for (date in dates) {
            if (date == expected || date == expected.minusDays(1).also { expected = it }) {
                streak++
                expected = date.minusDays(1)
            } else break
        }
        return streak
    }

    suspend fun seedDefaultHabits() {
        val defaults = listOf(
            Habit(name = "Boire un verre d'eau", emoji = "💧", durationMinutes = 1,  xpReward = 20,  colorHex = "#4FC3F7", sortOrder = 0),
            Habit(name = "2 min d'étirements",   emoji = "🧘", durationMinutes = 2,  xpReward = 30,  colorHex = "#2ECF8A", sortOrder = 1),
            Habit(name = "Respiration profonde",  emoji = "🌬", durationMinutes = 1,  xpReward = 20,  colorHex = "#FF6584", sortOrder = 2),
            Habit(name = "Lire 5 pages",          emoji = "📖", durationMinutes = 10, xpReward = 50,  colorHex = "#FFB547", sortOrder = 3),
            Habit(name = "Écrire 3 gratitudes",   emoji = "✏️", durationMinutes = 3,  xpReward = 35,  colorHex = "#9B59B6", sortOrder = 4),
        )
        defaults.forEach { dao.insertHabit(it) }
    }
}
