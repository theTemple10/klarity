package com.example.data.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val GEMINI_MODEL = "gemini-2.5-flash"

interface GeminiGateway {
    suspend fun generateText(prompt: String, systemInstruction: String? = null): Result<String>
    suspend fun chat(
        history: List<Pair<String, String>>,
        newMessage: String,
        systemInstruction: String? = null
    ): Result<String>
}

class FirebaseGeminiGateway : GeminiGateway {

    override suspend fun generateText(
        prompt: String,
        systemInstruction: String?
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val model = Firebase.ai.generativeModel(
                modelName = GEMINI_MODEL,
                systemInstruction = systemInstruction?.let { content { text(it) } }
            )
            val response = model.generateContent(prompt)
            val text = response.text
            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                Result.failure(IllegalStateException("Empty response from AI gateway"))
            }
        } catch (e: Exception) {
            Log.w("GeminiGateway", "generateText failed: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun chat(
        history: List<Pair<String, String>>,
        newMessage: String,
        systemInstruction: String?
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val model = Firebase.ai.generativeModel(
                modelName = GEMINI_MODEL,
                systemInstruction = systemInstruction?.let { content { text(it) } }
            )
            val historyContent = history.map { (role, msgText) ->
                content(role = if (role == "user") "user" else "model") {
                    text(msgText)
                }
            }
            val chat = model.startChat(history = historyContent)
            val response = chat.sendMessage(newMessage)
            val text = response.text
            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                Result.failure(IllegalStateException("Empty response from AI chat"))
            }
        } catch (e: Exception) {
            Log.w("GeminiGateway", "chat failed: ${e.message}")
            Result.failure(e)
        }
    }
}
