package com.microhabits.data.db

import androidx.room.*
import com.microhabits.data.model.Completion
import com.microhabits.data.model.DailyStats
import com.microhabits.data.model.Habit
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    // ── Habits ──────────────────────────────────────────────────────────────

    @Query("SELECT * FROM habits ORDER BY sortOrder ASC")
    fun getAllHabits(): Flow<List<Habit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("UPDATE habits SET sortOrder = :order WHERE id = :id")
    suspend fun updateSortOrder(id: Int, order: Int)

    // ── Completions ──────────────────────────────────────────────────────────

    @Query("SELECT * FROM completions WHERE completedDate = :date")
    fun getCompletionsForDate(date: String): Flow<List<Completion>>

    @Query("SELECT habitId FROM completions WHERE completedDate = :date")
    suspend fun getCompletedHabitIdsForDate(date: String): List<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCompletion(completion: Completion)

    @Query("DELETE FROM completions WHERE habitId = :habitId AND completedDate = :date")
    suspend fun deleteCompletion(habitId: Int, date: String)

    // ── Streak ───────────────────────────────────────────────────────────────

    @Query("""
        SELECT COUNT(*) FROM (
            SELECT DISTINCT completedDate FROM completions
            WHERE habitId = :habitId
            ORDER BY completedDate DESC
        )
    """)
    suspend fun getTotalCompletionDays(habitId: Int): Int

    // Returns dates with at least one completion, newest first
    @Query("""
        SELECT DISTINCT completedDate FROM completions
        WHERE habitId = :habitId
        ORDER BY completedDate DESC
        LIMIT 60
    """)
    suspend fun getRecentCompletionDates(habitId: Int): List<String>

    // ── Stats ────────────────────────────────────────────────────────────────

    @Query("""
        SELECT c.completedDate as date,
               COUNT(c.id) as completedCount,
               SUM(h.xpReward) as totalXp
        FROM completions c
        JOIN habits h ON c.habitId = h.id
        WHERE c.completedDate >= :fromDate
        GROUP BY c.completedDate
        ORDER BY c.completedDate ASC
    """)
    fun getDailyStats(fromDate: String): Flow<List<DailyStats>>

    @Query("SELECT SUM(h.xpReward) FROM completions c JOIN habits h ON c.habitId = h.id")
    fun getTotalXp(): Flow<Int?>

    @Query("SELECT COUNT(DISTINCT completedDate) FROM completions")
    fun getTotalActiveDays(): Flow<Int>

    @Query("SELECT COUNT(*) FROM completions")
    fun getTotalCompletions(): Flow<Int>
}
