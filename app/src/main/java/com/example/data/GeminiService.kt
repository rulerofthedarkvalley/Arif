package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import com.example.BuildConfig

data class GeminiSparkResult(
    val quote: String,
    val author: String
)

object GeminiClient {
    private val mediaType = "application/json; charset=utf-8".toMediaType()
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    suspend fun generateDailySpark(moodContext: String): GeminiSparkResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext GeminiSparkResult(
                quote = "Intentionality is the bridge between aspiration and reality. Focus on what brings genuine peace inside.",
                author = "Sanctuary Guide"
            )
        }

        try {
            // Build request payload using native JSONObject
            val partsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("text", "Generate a serene, elegant mindful statement for a digital vision board reflecting context '$moodContext'. Keep it short and calming, exactly one sentence. Return a JSON object with keys 'quote' and 'author'.")
                })
            }
            val contentObj = JSONObject().apply {
                put("parts", partsArray)
            }
            val contentsList = JSONArray().apply {
                put(contentObj)
            }

            val systemInstructionObj = JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", "You are a quiet, poetic mindfulness coach. Deliver reassuring, minimalist guidance of exactly one sentence in JSON format.")
                    })
                })
            }

            val configObj = JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.8)
            }

            val requestPayload = JSONObject().apply {
                put("contents", contentsList)
                put("generationConfig", configObj)
                put("systemInstruction", systemInstructionObj)
            }

            val requestBody = requestPayload.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext GeminiSparkResult(
                        quote = "Find stillness in the flow of change today. Let your actions follow gentle intents.",
                        author = "Sanctuary Guide"
                    )
                }

                val bodyString = response.body?.string() ?: ""
                val rootJson = JSONObject(bodyString)
                val candidates = rootJson.getJSONArray("candidates")
                val firstCandidate = candidates.getJSONObject(0)
                val responseContent = firstCandidate.getJSONObject("content")
                val responseParts = responseContent.getJSONArray("parts")
                val innerText = responseParts.getJSONObject(0).getString("text")

                // Parse the response text as a JSON object
                val innerJson = JSONObject(innerText)
                val quoteText = innerJson.optString("quote", "Find stillness in the flow of change today.")
                val authorText = innerJson.optString("author", "Sanctuary Guide")

                GeminiSparkResult(quoteText, authorText)
            }
        } catch (e: Exception) {
            GeminiSparkResult(
                quote = "Quiet the mind, and the soul will guide you to peaceful intents.",
                author = "Sanctuary Guide"
            )
        }
    }
}
