package com.microhabits.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FocusUiState(
    val habitName: String = "",
    val habitEmoji: String = "",
    val habitId: Int = -1,
    val totalSeconds: Int = 120,
    val remainingSeconds: Int = 120,
    val isRunning: Boolean = false,
    val isFinished: Boolean = false
) {
    val progressFraction: Float
        get() = if (totalSeconds > 0) 1f - remainingSeconds.toFloat() / totalSeconds else 1f

    val formattedTime: String
        get() {
            val m = remainingSeconds / 60
            val s = remainingSeconds % 60
            return "%02d:%02d".format(m, s)
        }
}

@HiltViewModel
class FocusViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(FocusUiState())
    val state: StateFlow<FocusUiState> = _state

    private var timerJob: Job? = null

    fun startFocus(habitId: Int, habitName: String, emoji: String, durationMinutes: Int) {
        val secs = (durationMinutes * 60).coerceAtLeast(60)
        _state.value = FocusUiState(
            habitId = habitId,
            habitName = habitName,
            habitEmoji = emoji,
            totalSeconds = secs,
            remainingSeconds = secs
        )
    }

    fun toggleTimer() {
        val current = _state.value
        if (current.isRunning) pauseTimer() else resumeTimer()
    }

    private fun resumeTimer() {
        _state.value = _state.value.copy(isRunning = true)
        timerJob = viewModelScope.launch {
            while (_state.value.remainingSeconds > 0 && _state.value.isRunning) {
                delay(1000L)
                _state.value = _state.value.copy(
                    remainingSeconds = _state.value.remainingSeconds - 1
                )
            }
            if (_state.value.remainingSeconds == 0) {
                _state.value = _state.value.copy(isRunning = false, isFinished = true)
            }
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _state.value = _state.value.copy(isRunning = false)
    }

    fun reset() {
        timerJob?.cancel()
        _state.value = _state.value.copy(
            remainingSeconds = _state.value.totalSeconds,
            isRunning = false,
            isFinished = false
        )
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
