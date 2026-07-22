package com.habittracker.app.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.habittracker.app.data.profile.UserProfile
import com.habittracker.app.data.profile.UserProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    application: Application,
    private val repository: UserProfileRepository
) : AndroidViewModel(application) {

    val profile: StateFlow<UserProfile> = repository.profile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    fun save(sex: String, heightCm: Float, weightKg: Float, age: Int) {
        viewModelScope.launch { repository.save(sex, heightCm, weightKg, age) }
    }

    class Factory(
        private val application: Application,
        private val repository: UserProfileRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(application, repository) as T
        }
    }
}
