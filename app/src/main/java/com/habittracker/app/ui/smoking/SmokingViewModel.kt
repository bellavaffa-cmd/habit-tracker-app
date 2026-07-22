package com.habittracker.app.ui.smoking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.habittracker.app.data.smoking.SmokingLog
import com.habittracker.app.data.smoking.SmokingRepository
import com.habittracker.app.ui.common.StreakUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SmokingViewModel(private val repository: SmokingRepository) : ViewModel() {

    val entries: StateFlow<List<SmokingLog>> = repository.entries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayCount: StateFlow<Int> = entries.map { list ->
        val start = StreakUtils.startOfToday()
        list.count { it.timestampMillis >= start }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val weekCount: StateFlow<Int> = entries.map { list ->
        val start = StreakUtils.startOfWeek()
        list.count { it.timestampMillis >= start }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val lastLogTimestamp: StateFlow<Long?> = entries
        .map { list -> list.maxOfOrNull { it.timestampMillis } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun logNow() {
        viewModelScope.launch { repository.logNow() }
    }

    fun delete(entry: SmokingLog) {
        viewModelScope.launch { repository.delete(entry) }
    }

    class Factory(private val repository: SmokingRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SmokingViewModel(repository) as T
        }
    }
}
