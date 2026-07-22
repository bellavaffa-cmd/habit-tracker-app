package com.habittracker.app.ui.workout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.habittracker.app.data.calories.CaloriesSettingsRepository
import com.habittracker.app.data.calories.RateLimitException
import com.habittracker.app.data.profile.UserProfile
import com.habittracker.app.data.profile.UserProfileRepository
import com.habittracker.app.data.steps.HealthConnectStatus
import com.habittracker.app.data.steps.StepsRepository
import com.habittracker.app.data.workout.DEFAULT_WEIGHT_KG
import com.habittracker.app.data.workout.GymExerciseLog
import com.habittracker.app.data.workout.GymExerciseRepository
import com.habittracker.app.data.workout.WorkoutLog
import com.habittracker.app.data.workout.WorkoutPlan
import com.habittracker.app.data.workout.WorkoutPlanClient
import com.habittracker.app.data.workout.WorkoutRepository
import com.habittracker.app.data.workout.caloriesBurnedFor
import com.habittracker.app.ui.common.StreakUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface WorkoutPlanState {
    data object Idle : WorkoutPlanState
    data object Generating : WorkoutPlanState
    data class Ready(val plan: WorkoutPlan) : WorkoutPlanState
    data class Error(val message: String) : WorkoutPlanState
}

class WorkoutViewModel(
    application: Application,
    private val repository: WorkoutRepository,
    private val profileRepository: UserProfileRepository,
    private val gymRepository: GymExerciseRepository,
    private val anthropicSettingsRepository: CaloriesSettingsRepository,
    private val stepsRepository: StepsRepository
) : AndroidViewModel(application) {

    private val planClient = WorkoutPlanClient()

    val stepsStatus: HealthConnectStatus = stepsRepository.status()
    val stepsPermission: String = stepsRepository.readStepsPermission

    private val _hasStepsPermission = MutableStateFlow(false)
    val hasStepsPermission: StateFlow<Boolean> = _hasStepsPermission.asStateFlow()

    private val _todaySteps = MutableStateFlow(0L)
    val todaySteps: StateFlow<Long> = _todaySteps.asStateFlow()

    private val _weekSteps = MutableStateFlow(0L)
    val weekSteps: StateFlow<Long> = _weekSteps.asStateFlow()

    fun refreshSteps() {
        viewModelScope.launch {
            val granted = stepsRepository.hasPermission()
            _hasStepsPermission.value = granted
            if (granted) {
                _todaySteps.value = stepsRepository.readSteps(StreakUtils.startOfToday(), System.currentTimeMillis())
                _weekSteps.value = stepsRepository.readSteps(StreakUtils.startOfWeek(), System.currentTimeMillis())
            }
        }
    }

    val entries: StateFlow<List<WorkoutLog>> = repository.entries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gymEntries: StateFlow<List<GymExerciseLog>> = gymRepository.entries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val profile: StateFlow<UserProfile> = profileRepository.profile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    private val _planState = MutableStateFlow<WorkoutPlanState>(WorkoutPlanState.Idle)
    val planState: StateFlow<WorkoutPlanState> = _planState.asStateFlow()

    /** The weight used for calorie-burn estimates — from Profile if set, else a generic default. */
    val effectiveWeightKg: StateFlow<Float> = profileRepository.profile
        .map { it.weightKg?.takeIf { w -> w > 0f } ?: DEFAULT_WEIGHT_KG }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DEFAULT_WEIGHT_KG)

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

    val todayCaloriesBurned: StateFlow<Int> = combine(entries, effectiveWeightKg) { list, weight ->
        val start = StreakUtils.startOfToday()
        list.filter { it.timestampMillis >= start }.sumOf { caloriesBurnedFor(it.type, it.durationMinutes, weight) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val weekCaloriesBurned: StateFlow<Int> = combine(entries, effectiveWeightKg) { list, weight ->
        val start = StreakUtils.startOfWeek()
        list.filter { it.timestampMillis >= start }.sumOf { caloriesBurnedFor(it.type, it.durationMinutes, weight) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun logWorkout(type: String, durationMinutes: Int, notes: String?) {
        viewModelScope.launch { repository.logWorkout(type, durationMinutes, notes) }
    }

    fun delete(entry: WorkoutLog) {
        viewModelScope.launch { repository.delete(entry) }
    }

    fun logGymExercise(exerciseName: String, muscleGroup: String, sets: Int, reps: Int, weightKg: Float) {
        viewModelScope.launch { gymRepository.logExercise(exerciseName, muscleGroup, sets, reps, weightKg) }
    }

    fun deleteGymEntry(entry: GymExerciseLog) {
        viewModelScope.launch { gymRepository.delete(entry) }
    }

    fun generatePlan(goal: String, durationMinutes: Int, focusExercise: String?) {
        val apiKey = anthropicSettingsRepository.getApiKey()
        if (apiKey.isNullOrBlank()) {
            _planState.value = WorkoutPlanState.Error("Add your Anthropic API key in Calories Settings first.")
            return
        }
        _planState.value = WorkoutPlanState.Generating
        viewModelScope.launch {
            try {
                val plan = withContext(Dispatchers.IO) {
                    planClient.generatePlan(apiKey, goal, durationMinutes, focusExercise, profile.value)
                }
                _planState.value = WorkoutPlanState.Ready(plan)
            } catch (e: RateLimitException) {
                _planState.value = WorkoutPlanState.Error("Anthropic rate limit reached — try again shortly.")
            } catch (e: Exception) {
                _planState.value = WorkoutPlanState.Error(e.message ?: "Something went wrong generating the plan.")
            }
        }
    }

    fun dismissPlan() {
        _planState.value = WorkoutPlanState.Idle
    }

    class Factory(
        private val application: Application,
        private val repository: WorkoutRepository,
        private val profileRepository: UserProfileRepository,
        private val gymRepository: GymExerciseRepository,
        private val anthropicSettingsRepository: CaloriesSettingsRepository,
        private val stepsRepository: StepsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WorkoutViewModel(
                application, repository, profileRepository, gymRepository, anthropicSettingsRepository, stepsRepository
            ) as T
        }
    }
}
