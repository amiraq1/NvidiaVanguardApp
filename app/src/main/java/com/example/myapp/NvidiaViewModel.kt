package com.example.myapp

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class NvidiaViewModel(application: Application) : AndroidViewModel(application) {
    private val apiClient = NvidiaApiClient()

    private val masterKey = MasterKey.Builder(application)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val securePreferences = EncryptedSharedPreferences.create(
        application,
        "secret_vanguard_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private object ContextRegistry {
        const val UI_DESIGN_SKILL = "Act as an Elite UI/UX Developer. Enforce the strict Vanguard OS design style: premium dark themes (#050505), sharp high-contrast accents (#00F0FF), asymmetrical bento box layouts, deep typography contrasts, and tactical animations using Jetpack Compose Material 3."
        const val NETWORK_SKILL = "Act as a Senior Network Engineer and API Architect. Focus on SFP+ layouts, high-performance OkHttp configurations, fiber optic optimizations, low-latency threading via Dispatchers.IO, and strict HTTP error capturing."
        const val DEFAULT_ASSISTANT = "Act as a helpful, adaptive AI developer assistant."

        fun getSkillForPrompt(input: String): String {
            val lowerInput = input.lowercase()
            return when {
                lowerInput.contains("صمم") || lowerInput.contains("واجهة") || 
                lowerInput.contains("ui") || lowerInput.contains("compose") || 
                lowerInput.contains("design") || lowerInput.contains("bento") -> UI_DESIGN_SKILL
                lowerInput.contains("اتصال") || lowerInput.contains("شبكة") || 
                lowerInput.contains("api") || lowerInput.contains("endpoint") || 
                lowerInput.contains("404") || lowerInput.contains("okhttp") || 
                lowerInput.contains("network") -> NETWORK_SKILL
                else -> DEFAULT_ASSISTANT
            }
        }
    }

    var apiKey by mutableStateOf("")
        private set

    val availableModels = mutableStateOf<List<String>>(
        listOf(
            "meta/llama-3.1-8b-instruct",
            "meta/llama-3.1-70b-instruct",
            "nvidia/llama-3.1-nemotron-70b-instruct",
            "mistralai/mixtral-8x22b-instruct-v0.1",
            "microsoft/phi-3-medium-128k-instruct"
        )
    )
    var selectedModel by mutableStateOf("meta/llama-3.1-8b-instruct")
    var promptInput by mutableStateOf("")
    
    val messages = mutableStateListOf<ChatMessage>()
    var outputText by mutableStateOf("SYSTEM READY > _") // Keep for legacy/logs if needed

    var loadingState by mutableStateOf(false)
        private set

    val currentRam = mutableStateOf("0 MB")
    val currentTps = mutableStateOf("0.0 tps")

    init {
        // Load stored API key securely on initialization
        apiKey = getStoredApiKey()

        // Initial system message
        messages.add(ChatMessage(content = "VANGUARD OS INITIALIZED. STANDING BY.", isUser = false))

        // Proactive load if key already exists
        if (apiKey.startsWith("nvapi-") && apiKey.length > 20) {
            loadModels(apiKey)
        }

        // Live Telemetry Loop
        viewModelScope.launch(Dispatchers.Default) {
            delay(2000) // Stabilize UI before telemetry
            while (isActive) {
                try {
                    val runtime = Runtime.getRuntime()
                    val usedMem = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
                    currentRam.value = "${usedMem} MB"
                    
                    // Simulate dynamic TPS variation
                    val simulatedTps = 55.0 + (Math.random() * 5.0)
                    currentTps.value = String.format("%.1f tps", simulatedTps)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(1000)
            }
        }
    }

    fun updateApiKey(newKey: String) {
        apiKey = newKey
        saveApiKeySecurely(newKey)
        
        // Reactive trigger: Fetch models if key looks valid
        if (newKey.startsWith("nvapi-") && newKey.length > 20) {
            loadModels(newKey)
        }
    }

    private fun saveApiKeySecurely(key: String) {
        securePreferences.edit().putString("NVIDIA_API_KEY", key).apply()
    }

    private fun getStoredApiKey(): String {
        return securePreferences.getString("NVIDIA_API_KEY", "") ?: ""
    }

    fun onModelSelected(model: String) {
        selectedModel = model
    }

    fun loadModels(key: String) {
        viewModelScope.launch {
            try {
                val result = apiClient.fetchAvailableModels(key)
                result.onSuccess { models ->
                    if (models.isNotEmpty()) {
                        availableModels.value = models
                        if (!models.contains(selectedModel)) {
                            selectedModel = models.first()
                        }
                    } else {
                        // Handle empty list case - keep local fallbacks
                        val msg = "NETWORK_LOG: Remote list empty. Using local fallbacks."
                        messages.add(ChatMessage(content = msg, isUser = false))
                    }
                }.onFailure { error ->
                    // Fallback to local models on network error
                    val msg = "NETWORK_LOG: Fetch failed (${error.localizedMessage}). Using local fallbacks."
                    messages.add(ChatMessage(content = msg, isUser = false))
                }
            } catch (e: Exception) {
                messages.add(ChatMessage(content = "CRITICAL_CRASH: ${e.message}. Preserving local models.", isUser = false))
            }
        }
    }

    fun onSendClicked() {
        if (apiKey.isBlank() || promptInput.isBlank() || loadingState) return

        viewModelScope.launch {
            loadingState = true
            val originalPrompt = promptInput
            promptInput = "" // Clear input early for better UX
            
            // Add user message
            messages.add(ChatMessage(content = originalPrompt, isUser = true))
            
            // Add initial empty AI message for streaming
            val aiMessageId = java.util.UUID.randomUUID().toString()
            messages.add(ChatMessage(id = aiMessageId, content = "", isUser = false))

            val injectedSkill = ContextRegistry.getSkillForPrompt(originalPrompt)
            val engineeredPrompt = "$injectedSkill\n\nUser Request: $originalPrompt"

            apiClient.streamChatCompletion(
                apiKey = apiKey,
                model = selectedModel,
                prompt = engineeredPrompt
            ).catch { error ->
                updateMessageContent(aiMessageId, "STREAM_ERROR: ${error.message}")
                loadingState = false
            }.collect { token ->
                updateMessageContent(aiMessageId, getMessageContent(aiMessageId) + token)
            }
            
            loadingState = false
        }
    }

    private fun updateMessageContent(id: String, newContent: String) {
        val index = messages.indexOfFirst { it.id == id }
        if (index != -1) {
            messages[index] = messages[index].copy(content = newContent)
        }
    }

    private fun getMessageContent(id: String): String {
        return messages.find { it.id == id }?.content ?: ""
    }
}
