package com.modernapps.dishcrafter

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object GeminiApi {

    private val client = OkHttpClient()
    private const val BASE_URL = "https://api.groq.com/openai/v1/chat/completions"

    fun generateRecipe(ingredients: String): String {
        val apiKey = BuildConfig.AI_API_KEY
        if (apiKey.isBlank()) {
            return "Ошибка: API ключ не указан. Добавьте AI_API_KEY в local.properties."
        }

        val prompt = """
            На основе следующих продуктов предложи рецепт блюда.
            Продукты: $ingredients

            Ответь в формате:
            🍽 Название блюда

            📝 Ингредиенты:
            (список с количеством)

            👨‍🍳 Приготовление:
            (пошаговые инструкции)

            ⏱ Время приготовления: (примерное время)
        """.trimIndent()

        val body = JSONObject().apply {
            put("model", "llama-3.3-70b-versatile")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
        }

        val request = Request.Builder()
            .url(BASE_URL)
            .header("Authorization", "Bearer $apiKey")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val message = when (response.code) {
                    429 -> "Превышен лимит запросов. Подождите минуту и попробуйте снова."
                    401, 403 -> "Неверный API ключ. Проверьте local.properties."
                    else -> "Ошибка API: ${response.code}"
                }
                throw IOException(message)
            }
            val json = JSONObject(response.body!!.string())
            return json
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        }
    }
}
