package com.habittracker.app.data.calories

import android.graphics.Bitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Detects a barcode in a photo on-device (ML Kit, no network call, no AI vision model), then looks
 * the product up in Open Food Facts — a free, keyless nutrition database — as a non-AI alternative
 * to photographing the food itself.
 */
class BarcodeLookupClient {
    private val scanner = BarcodeScanning.getClient()
    private val http = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    fun lookup(bitmap: Bitmap): FoodAnalysis {
        val code = detectBarcode(bitmap) ?: throw IOException("No barcode found in the photo.")
        return fetchProduct(code)
    }

    private fun detectBarcode(bitmap: Bitmap): String? {
        val image = InputImage.fromBitmap(bitmap, 0)
        val barcodes = Tasks.await(scanner.process(image), 15, TimeUnit.SECONDS)
        return barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }?.rawValue
    }

    private fun fetchProduct(barcode: String): FoodAnalysis {
        val request = Request.Builder()
            .url("https://world.openfoodfacts.org/api/v2/product/$barcode.json?fields=product_name,nutriments")
            .build()

        http.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) throw IOException("Product lookup failed (HTTP ${resp.code}).")
            val json = JSONObject(resp.body?.string().orEmpty())
            if (json.optInt("status", 0) != 1) {
                throw IOException("Barcode $barcode isn't in the product database.")
            }

            val product = json.getJSONObject("product")
            val nutriments = product.optJSONObject("nutriments") ?: JSONObject()
            val name = product.optString("product_name").ifBlank { "Scanned product" }

            val hasServing = nutriments.has("energy-kcal_serving")
            fun value(servingKey: String, per100Key: String): Double =
                if (hasServing) nutriments.optDouble(servingKey, 0.0) else nutriments.optDouble(per100Key, 0.0)

            return FoodAnalysis(
                foodDescription = if (hasServing) name else "$name (per 100g — adjust for your portion)",
                calories = value("energy-kcal_serving", "energy-kcal_100g").toInt(),
                proteinGrams = value("proteins_serving", "proteins_100g"),
                carbsGrams = value("carbohydrates_serving", "carbohydrates_100g"),
                fatGrams = value("fat_serving", "fat_100g"),
                confidence = if (hasServing) "high" else "medium"
            )
        }
    }
}
