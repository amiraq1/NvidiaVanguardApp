package com.example.myapp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.util.concurrent.TimeUnit

class NvidiaApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val chatEndpoint = "https://integrate.api.nvidia.com/v1/chat/completions"
    private val modelsEndpoint = "https://integrate.api.nvidia.com/v1/models"
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    // 1. Fetch Available Models Dynamically (Standard GET Request)
    suspend fun fetchAvailableModels(apiKey: String): Result<List<String>> {
        return kotlin.runCatching {
            val request = Request.Builder()
                .url(modelsEndpoint)
                .get()
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Accept", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "Unknown Error"
                    throw IOException("HTTP ${response.code}: $errorBody")
                }

                val responseBody = response.body?.string() ?: throw IOException("Empty response body")
                val jsonObject = JSONObject(responseBody)
                val dataArray = jsonObject.getJSONArray("data")
                val modelsList = mutableListOf<String>()

                for (i in 0 until dataArray.length()) {
                    val modelObj = dataArray.getJSONObject(i)
                    modelsList.add(modelObj.getString("id"))
                }
                modelsList.sorted()
            }
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(it) }
        )
    }

    // 2. Stream Chat Completions (Live SSE Flow Output)
    fun streamChatCompletion(apiKey: String, model: String, prompt: String): Flow<String> = flow {
        val jsonPayload = JSONObject().apply {
            put("model", model)
            put("messages", JSONArray().put(JSONObject().apply {
                put("role", "user")
                put("content", prompt)
            }))
            put("stream", true)
        }

        val requestBody = jsonPayload.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(chatEndpoint)
            .post(requestBody)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "text/event-stream")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Stream Error"
                throw IOException("HTTP ${response.code}: $errorBody")
            }

            val source = response.body?.source() ?: throw IOException("No response source available")
            val reader = BufferedReader(source.inputStream().reader())
            
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val currentLine = line?.trim() ?: continue
                if (currentLine.isEmpty()) continue
                
                if (currentLine.startsWith("data:")) {
                    val dataContent = currentLine.substring(5).trim()
                    if (dataContent == "[DONE]") break
                    
                    try {
                        val jsonChunk = JSONObject(dataContent)
                        val choices = jsonChunk.getJSONArray("choices")
                        if (choices.length() > 0) {
                            val delta = choices.getJSONObject(0).getJSONObject("delta")
                            if (delta.has("content")) {
                                emit(delta.getString("content"))
                            }
                        }
                    } catch (e: Exception) {
                        // Skip malformed chunk lines safely during stream
                    }
                }
            }
        }
    }.flowOn(Dispatchers.IO)
}
