package com.habittracker.app.data.workout

import com.habittracker.app.data.calories.RateLimitException
import com.habittracker.app.data.profile.UserProfile
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class PlanExercise(
    val name: String,
    val sets: Int,
    val reps: String,
    val restSeconds: Int,
    val notes: String
)

data class WorkoutPlan(
    val title: String,
    val estimatedDurationMinutes: Int,
    val exercises: List<PlanExercise>,
    val tips: String
)

/**
 * Calls the Claude Messages API (text-only, no image) to generate a workout plan, using the same
 * Anthropic key already stored for the Calories tracker (see [com.habittracker.app.data.calories.CaloriesSettingsRepository])
 * — it's the same account, so there's no reason to make the user enter it twice.
 */
class WorkoutPlanClient {
    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    fun generatePlan(
        apiKey: String,
        goal: String,
        durationMinutes: Int,
        focusExercise: String?,
        profile: UserProfile
    ): WorkoutPlan {
        val schema = JSONObject().apply {
            put("type", "object")
            put(
                "properties",
                JSONObject().apply {
                    put("title", JSONObject().put("type", "string"))
                    put("estimatedDurationMinutes", JSONObject().put("type", "integer"))
                    put(
                        "exercises",
                        JSONObject().apply {
                            put("type", "array")
                            put(
                                "items",
                                JSONObject().apply {
                                    put("type", "object")
                                    put(
                                        "properties",
                                        JSONObject().apply {
                                            put("name", JSONObject().put("type", "string"))
                                            put("sets", JSONObject().put("type", "integer"))
                                            put("reps", JSONObject().put("type", "string"))
                                            put("restSeconds", JSONObject().put("type", "integer"))
                                            put("notes", JSONObject().put("type", "string"))
                                        }
                                    )
                                    put("required", JSONArray(listOf("name", "sets", "reps", "restSeconds", "notes")))
                                    put("additionalProperties", false)
                                }
                            )
                        }
                    )
                    put("tips", JSONObject().put("type", "string"))
                }
            )
            put("required", JSONArray(listOf("title", "estimatedDurationMinutes", "exercises", "tips")))
            put("additionalProperties", false)
        }

        val profileLines = buildString {
            profile.sex?.let { append("Sex: $it. ") }
            profile.age?.let { append("Age: $it. ") }
            profile.heightCm?.let { append("Height: ${it.toInt()}cm. ") }
            profile.weightKg?.let { append("Weight: ${it.toInt()}kg. ") }
        }.ifBlank { "Not provided. " }

        val promptText = buildString {
            append("Create a workout plan. Goal: $goal. Available time: $durationMinutes minutes. ")
            if (!focusExercise.isNullOrBlank()) {
                append("The plan must include \"$focusExercise\" as one of the exercises. ")
            }
            append("User profile — $profileLines")
            append(
                "Return a realistic exercise list that fits within the available time, with sets, a rep " +
                    "range or target per exercise, rest time in seconds between sets, and brief form/safety notes."
            )
        }

        val requestJson = JSONObject().apply {
            put("model", "claude-opus-4-8")
            put("max_tokens", 2048)
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
                        put("content", promptText)
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
                if (resp.code == 429) {
                    throw RateLimitException(message ?: "Anthropic rate limit reached.")
                }
                throw IOException(message ?: "Claude API returned HTTP ${resp.code}")
            }

            val json = JSONObject(responseBody)
            if (json.optString("stop_reason") == "refusal") {
                throw IOException("The plan couldn't be generated (declined by the model).")
            }

            val content = json.getJSONArray("content")
            val textBlock = (0 until content.length())
                .map { content.getJSONObject(it) }
                .firstOrNull { it.optString("type") == "text" }
                ?: throw IOException("No plan returned.")

            val result = JSONObject(textBlock.getString("text"))
            val exercisesJson = result.getJSONArray("exercises")
            val exercises = (0 until exercisesJson.length()).map { i ->
                val ex = exercisesJson.getJSONObject(i)
                PlanExercise(
                    name = ex.getString("name"),
                    sets = ex.getInt("sets"),
                    reps = ex.getString("reps"),
                    restSeconds = ex.getInt("restSeconds"),
                    notes = ex.getString("notes")
                )
            }
            return WorkoutPlan(
                title = result.getString("title"),
                estimatedDurationMinutes = result.getInt("estimatedDurationMinutes"),
                exercises = exercises,
                tips = result.getString("tips")
            )
        }
    }
}
