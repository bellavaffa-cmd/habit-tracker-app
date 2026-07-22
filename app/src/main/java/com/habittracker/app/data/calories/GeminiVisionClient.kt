package com.habittracker.app.data.calories

import android.util.Base64
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class FoodAnalysis(
    val foodDescription: String,
    val calories: Int,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val confidence: String
)

/**
 * Calls the free-tier Google Gemini API directly from the device using the user's own API key
 * (from aistudio.google.com/apikey — no billing required for free-tier usage, unlike Claude).
 * Uses generationConfig.response_schema so the response is guaranteed-valid JSON rather than
 * freeform text we'd have to parse defensively.
 */
class GeminiVisionClient {
    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    fun analyzeFood(apiKey: String, imageBytes: ByteArray): FoodAnalysis {
        val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

        val schema = JSONObject().apply {
            put("type", "object")
            put(
                "properties",
                JSONObject().apply {
                    put("foodDescription", JSONObject().put("type", "string"))
                    put("calories", JSONObject().put("type", "integer"))
                    put("proteinGrams", JSONObject().put("type", "number"))
                    put("carbsGrams", JSONObject().put("type", "number"))
                    put("fatGrams", JSONObject().put("type", "number"))
                    put(
                        "confidence",
                        JSONObject().apply {
                            put("type", "string")
                            put("enum", JSONArray(listOf("low", "medium", "high")))
                        }
                    )
                }
            )
            put(
                "required",
                JSONArray(listOf("foodDescription", "calories", "proteinGrams", "carbsGrams", "fatGrams", "confidence"))
            )
        }

        val requestJson = JSONObject().apply {
            put(
                "contents",
                JSONArray().put(
                    JSONObject().put(
                        "parts",
                        JSONArray().apply {
                            put(
                                JSONObject().put(
                                    "text",
                                    "Identify the food in this photo and estimate its nutritional content " +
                                        "for the visible portion: total calories, protein (g), carbs (g), and " +
                                        "fat (g). Give your best estimate even if uncertain."
                                )
                            )
                            put(
                                JSONObject().put(
                                    "inline_data",
                                    JSONObject().apply {
                                        put("mime_type", "image/jpeg")
                                        put("data", base64Image)
                                    }
                                )
                            )
                        }
                    )
                )
            )
            put(
                "generationConfig",
                JSONObject().apply {
                    put("response_mime_type", "application/json")
                    put("response_schema", schema)
                }
            )
        }

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent?key=$apiKey")
            .addHeader("content-type", "application/json")
            .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        http.newCall(request).execute().use { resp ->
            val responseBody = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                val message = runCatching {
                    JSONObject(responseBody).getJSONObject("error").getString("message")
                }.getOrNull()
                throw IOException(message ?: "Gemini API returned HTTP ${resp.code}")
            }

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                val blockReason = json.optJSONObject("promptFeedback")?.optString("blockReason")
                throw IOException(
                    if (!blockReason.isNullOrBlank()) {
                        "The photo couldn't be analyzed (blocked: $blockReason)."
                    } else {
                        "No analysis returned."
                    }
                )
            }

            val candidate = candidates.getJSONObject(0)
            if (candidate.optString("finishReason") in setOf("SAFETY", "RECITATION")) {
                throw IOException("The photo couldn't be analyzed (declined by the model).")
            }

            val parts = candidate.getJSONObject("content").getJSONArray("parts")
            val text = (0 until parts.length())
                .map { parts.getJSONObject(it) }
                .firstOrNull { it.has("text") }
                ?.getString("text")
                ?: throw IOException("No analysis returned.")

            val result = JSONObject(text)
            return FoodAnalysis(
                foodDescription = result.getString("foodDescription"),
                calories = result.getInt("calories"),
                proteinGrams = result.getDouble("proteinGrams"),
                carbsGrams = result.getDouble("carbsGrams"),
                fatGrams = result.getDouble("fatGrams"),
                confidence = result.getString("confidence")
            )
        }
    }

    private companion object {
        const val MODEL = "gemini-3.6-flash"
    }
}
