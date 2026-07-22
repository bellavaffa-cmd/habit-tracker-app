package com.habittracker.app.ui.hydration

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.habittracker.app.data.hydration.HydrationLog
import com.habittracker.app.data.hydration.HydrationRepository
import com.habittracker.app.data.hydration.recommendedDailyMl
import com.habittracker.app.data.profile.UserProfileRepository
import com.habittracker.app.ui.common.StreakUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HydrationViewModel(
    application: Application,
    private val repository: HydrationRepository,
    private val profileRepository: UserProfileRepository
) : AndroidViewModel(application) {

    val entries: StateFlow<List<HydrationLog>> = repository.entries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayMl: StateFlow<Int> = entries.map { list ->
        val start = StreakUtils.startOfToday()
        list.filter { it.timestampMillis >= start }.sumOf { it.amountMl }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recommendedMl: StateFlow<Int> = profileRepository.profile
        .map { recommendedDailyMl(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2500)

    fun logDrink(amountMl: Int) {
        viewModelScope.launch { repository.log(amountMl) }
    }

    fun delete(entry: HydrationLog) {
        viewModelScope.launch { repository.delete(entry) }
    }

    class Factory(
        private val application: Application,
        private val repository: HydrationRepository,
        private val profileRepository: UserProfileRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HydrationViewModel(application, repository, profileRepository) as T
        }
    }
}
