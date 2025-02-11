package com.example.test_max.services

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OpenAIService(private val apiKey: String) {
    private val openAI = OpenAI(apiKey)

    suspend fun analyzeData(query: String, context: String): String = withContext(Dispatchers.IO) {
        try {
            val completion = openAI.chatCompletion(
                ChatCompletionRequest(
                    model = ModelId("gpt-3.5-turbo"),
                    messages = listOf(
                        ChatMessage(
                            role = ChatRole.System,
                            content = "You are a data analyst helping to analyze Excel data. " +
                                    "Here's the context of the data: $context"
                        ),
                        ChatMessage(
                            role = ChatRole.User,
                            content = query
                        )
                    )
                )
            )
            
            completion.choices.first().message.content
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
} 