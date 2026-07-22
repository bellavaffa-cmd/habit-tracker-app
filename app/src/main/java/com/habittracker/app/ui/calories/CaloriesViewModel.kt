package com.habittracker.app.ui.calories

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.habittracker.app.BuildConfig
import com.habittracker.app.data.calories.CalorieLog
import com.habittracker.app.data.calories.CalorieLogRepository
import com.habittracker.app.data.calories.FoodAnalysis
import com.habittracker.app.data.calories.GeminiVisionClient
import com.habittracker.app.ui.common.StreakUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

sealed interface AnalysisState {
    data object Idle : AnalysisState
    data object Analyzing : AnalysisState
    data class Ready(val photoPath: String, val analysis: FoodAnalysis) : AnalysisState
    data class Error(val message: String) : AnalysisState
}

class CaloriesViewModel(
    application: Application,
    private val repository: CalorieLogRepository
) : AndroidViewModel(application) {

    private val visionClient = GeminiVisionClient()

    val entries: StateFlow<List<CalorieLog>> = repository.entries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayCalories: StateFlow<Int> = entries.map { list ->
        val start = StreakUtils.startOfToday()
        list.filter { it.timestampMillis >= start }.sumOf { it.calories }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val weekCalories: StateFlow<Int> = entries.map { list ->
        val start = StreakUtils.startOfWeek()
        list.filter { it.timestampMillis >= start }.sumOf { it.calories }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _analysisState = MutableStateFlow<AnalysisState>(AnalysisState.Idle)
    val analysisState: StateFlow<AnalysisState> = _analysisState.asStateFlow()

    fun analyzePhoto(sourceUri: Uri) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) {
            _analysisState.value = AnalysisState.Error(
                "No Gemini API key baked into this build. Add geminiApiKey to local.properties and rebuild."
            )
            return
        }
        _analysisState.value = AnalysisState.Analyzing
        viewModelScope.launch {
            try {
                val savedPath = withContext(Dispatchers.IO) { savePhoto(sourceUri) }
                val bytes = File(savedPath).readBytes()
                val analysis = withContext(Dispatchers.IO) { visionClient.analyzeFood(apiKey, bytes) }
                _analysisState.value = AnalysisState.Ready(savedPath, analysis)
            } catch (e: Exception) {
                _analysisState.value = AnalysisState.Error(e.message ?: "Something went wrong analyzing the photo.")
            }
        }
    }

    private fun savePhoto(sourceUri: Uri): String {
        val app = getApplication<Application>()
        val original = app.contentResolver.openInputStream(sourceUri)?.use { BitmapFactory.decodeStream(it) }
            ?: throw IOException("Could not read the photo.")
        val scaled = downscale(original, 1024)
        val dir = File(app.filesDir, "calorie_photos").apply { mkdirs() }
        val file = File(dir, "meal_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { out -> scaled.compress(Bitmap.CompressFormat.JPEG, 85, out) }
        return file.absolutePath
    }

    private fun downscale(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val ratio = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
        if (ratio >= 1f) return bitmap
        return Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
    }

    fun dismissAnalysis() {
        _analysisState.value = AnalysisState.Idle
    }

    fun saveEntry(photoPath: String, foodDescription: String, calories: Int, protein: Double, carbs: Double, fat: Double) {
        viewModelScope.launch {
            repository.logEntry(photoPath, foodDescription, calories, protein, carbs, fat)
            _analysisState.value = AnalysisState.Idle
        }
    }

    fun delete(entry: CalorieLog) {
        viewModelScope.launch { repository.delete(entry) }
    }

    class Factory(
        private val application: Application,
        private val repository: CalorieLogRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CaloriesViewModel(application, repository) as T
        }
    }
}
