package com.habittracker.app.data.calories

import android.graphics.Bitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Reads a nutrition-facts label on-device via ML Kit text recognition (no cloud AI call) and
 * regex-parses the calories/protein/carbs/fat lines — a non-AI alternative to vision-based food
 * identification, useful for packaged foods whose barcode isn't in Open Food Facts.
 */
class NutritionLabelScanner {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun scan(bitmap: Bitmap): FoodAnalysis {
        val image = InputImage.fromBitmap(bitmap, 0)
        val text = Tasks.await(recognizer.process(image), 15, TimeUnit.SECONDS).text

        val calories = findNumber(text, Regex("""calories[^\d]{0,10}(\d+(?:\.\d+)?)""", RegexOption.IGNORE_CASE))
            ?: throw IOException("Couldn't find a calorie value on this label.")
        val protein = findNumber(text, Regex("""protein[^\d]{0,10}(\d+(?:\.\d+)?)""", RegexOption.IGNORE_CASE)) ?: 0.0
        val carbs = findNumber(text, Regex("""carb\w*[^\d]{0,10}(\d+(?:\.\d+)?)""", RegexOption.IGNORE_CASE)) ?: 0.0
        val fat = findNumber(text, Regex("""fat[^\d]{0,10}(\d+(?:\.\d+)?)""", RegexOption.IGNORE_CASE)) ?: 0.0

        return FoodAnalysis(
            foodDescription = "Nutrition label",
            calories = calories.toInt(),
            proteinGrams = protein,
            carbsGrams = carbs,
            fatGrams = fat,
            confidence = "medium"
        )
    }

    private fun findNumber(text: String, regex: Regex): Double? =
        regex.find(text)?.groupValues?.get(1)?.toDoubleOrNull()
}
