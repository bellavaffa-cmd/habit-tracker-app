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
 * Calls the Claude Messages API directly from the device using the user's own Anthropic API key
 * (see [CaloriesSettingsRepository]). Uses structured outputs (`output_config.format`) so the
 * response is guaranteed-valid JSON rather than freeform text we'd have to parse defensively.
 */
class ClaudeVisionClient {
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
            put("additionalProperties", false)
        }

        val requestJson = JSONObject().apply {
            put("model", "claude-opus-4-8")
            put("max_tokens", 1024)
            put(
                "output_config",
                JSONObject().put(
                    "format",
                    JSONObject().apply {
                        put("type", "json_schema")
                        put("schema", schema)
                    }
                )
            )
            put(
                "messages",
                JSONArray().put(
                    JSONObject().apply {
                        put("role", "user")
                        put(
                            "content",
                            JSONArray().apply {
                                put(
                                    JSONObject().apply {
                                        put("type", "image")
                                        put(
                                            "source",
                                            JSONObject().apply {
                                                put("type", "base64")
                                                put("media_type", "image/jpeg")
                                                put("data", base64Image)
                                            }
                                        )
                                    }
                                )
                                put(
                                    JSONObject().apply {
                                        put("type", "text")
                                        put(
                                            "text",
                                            "Identify the food in this photo and estimate its nutritional " +
                                                "content for the visible portion: total calories, protein (g), " +
                                                "carbs (g), and fat (g). Give your best estimate even if uncertain."
                                        )
                                    }
                                )
                            }
                        )
                    }
                )
            )
        }

        val request = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("content-type", "application/json")
            .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        http.newCall(request).execute().use { resp ->
            val responseBody = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                val message = runCatching {
                    JSONObject(responseBody).getJSONObject("error").getString("message")
                }.getOrNull()
                throw IOException(message ?: "Claude API returned HTTP ${resp.code}")
            }

            val json = JSONObject(responseBody)
            if (json.optString("stop_reason") == "refusal") {
                throw IOException("The photo couldn't be analyzed (declined by the model).")
            }

            val content = json.getJSONArray("content")
            val textBlock = (0 until content.length())
                .map { content.getJSONObject(it) }
                .firstOrNull { it.optString("type") == "text" }
                ?: throw IOException("No analysis returned.")

            val result = JSONObject(textBlock.getString("text"))
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
}
