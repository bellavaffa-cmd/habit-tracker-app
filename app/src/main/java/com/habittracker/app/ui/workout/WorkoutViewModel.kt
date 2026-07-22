package com.habittracker.app.ui.workout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.habittracker.app.data.workout.WorkoutLog
import com.habittracker.app.data.workout.WorkoutRepository
import com.habittracker.app.ui.common.StreakUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutViewModel(
    application: Application,
    private val repository: WorkoutRepository
) : AndroidViewModel(application) {

    val entries: StateFlow<List<WorkoutLog>> = repository.entries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayMinutes: StateFlow<Int> = entries.map { list ->
        val start = StreakUtils.startOfToday()
        list.filter { it.timestampMillis >= start }.sumOf { it.durationMinutes }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val weekMinutes: StateFlow<Int> = entries.map { list ->
        val start = StreakUtils.startOfWeek()
        list.filter { it.timestampMillis >= start }.sumOf { it.durationMinutes }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val weekWorkoutCount: StateFlow<Int> = entries.map { list ->
        val start = StreakUtils.startOfWeek()
        list.count { it.timestampMillis >= start }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun logWorkout(type: String, durationMinutes: Int, notes: String?) {
        viewModelScope.launch { repository.logWorkout(type, durationMinutes, notes) }
    }

    fun delete(entry: WorkoutLog) {
        viewModelScope.launch { repository.delete(entry) }
    }

    class Factory(
        private val application: Application,
        private val repository: WorkoutRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WorkoutViewModel(application, repository) as T
        }
    }
}
