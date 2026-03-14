package com.microhabits.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.microhabits.data.model.*
import com.microhabits.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HomeUiState(
    val habitsWithStatus: List<HabitWithStatus> = emptyList(),
    val todayDate: String = "",
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val progressPercent: Float = 0f,
    val totalXp: Int = 0,
    val xpLevel: Int = 1,
    val xpToNextLevel: Int = 100,
    val xpInCurrentLevel: Int = 0
)

data class StatsUiState(
    val dailyStats: List<DailyStats> = emptyList(),
    val totalXp: Int = 0,
    val totalActiveDays: Int = 0,
    val totalCompletions: Int = 0,
    val currentStreak: Int = 0
)

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    private val today: String
        get() = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    // ── Home state ─────────────────────────────────────────────────────────

    val homeState: StateFlow<HomeUiState> = combine(
        repository.getHabitsWithStatus(today),
        repository.totalXp
    ) { habits, xpRaw ->
        val xp = xpRaw ?: 0
        val done = habits.count { it.completedToday }
        val total = habits.size
        val (level, toNext, inLevel) = computeLevel(xp)
        HomeUiState(
            habitsWithStatus = habits,
            todayDate = today,
            completedCount = done,
            totalCount = total,
            progressPercent = if (total > 0) done.toFloat() / total else 0f,
            totalXp = xp,
            xpLevel = level,
            xpToNextLevel = toNext,
            xpInCurrentLevel = inLevel
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    // ── Stats state ────────────────────────────────────────────────────────

    val statsState: StateFlow<StatsUiState> = combine(
        repository.getDailyStats(LocalDate.now().minusDays(29).format(DateTimeFormatter.ISO_LOCAL_DATE)),
        repository.totalXp,
        repository.totalActiveDays,
        repository.totalCompletions
    ) { stats, xp, activeDays, completions ->
        StatsUiState(
            dailyStats = stats,
            totalXp = xp ?: 0,
            totalActiveDays = activeDays,
            totalCompletions = completions
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsUiState())

    // ── Actions ────────────────────────────────────────────────────────────

    fun toggleHabit(habitId: Int, currentlyDone: Boolean) {
        viewModelScope.launch {
            repository.toggleCompletion(habitId, today, !currentlyDone)
        }
    }

    fun reorderHabits(reordered: List<Habit>) {
        viewModelScope.launch { repository.updateSortOrders(reordered) }
    }

    fun addHabit(name: String, emoji: String, durationMinutes: Int, xpReward: Int, colorHex: String) {
        viewModelScope.launch {
            val currentSize = homeState.value.habitsWithStatus.size
            repository.addHabit(
                Habit(
                    name = name,
                    emoji = emoji,
                    durationMinutes = durationMinutes,
                    xpReward = xpReward,
                    colorHex = colorHex,
                    sortOrder = currentSize
                )
            )
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch { repository.deleteHabit(habit) }
    }

    fun seedIfEmpty() {
        viewModelScope.launch {
            if (homeState.value.habitsWithStatus.isEmpty()) {
                repository.seedDefaultHabits()
            }
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private data class LevelInfo(val level: Int, val toNext: Int, val inLevel: Int)

    private fun computeLevel(totalXp: Int): LevelInfo {
        // XP thresholds: level N requires N * 100 XP
        var xpLeft = totalXp
        var level = 1
        while (xpLeft >= level * 100) {
            xpLeft -= level * 100
            level++
        }
        return LevelInfo(level, level * 100, xpLeft)
    }
}
