package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String? = null
)

data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null
)

data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

data class Candidate(
    val content: Content? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

object GeminiHelper {
    suspend fun generateDescription(title: String, category: String, brand: String, condition: String, specs: String): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
        
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY") {
            // High fidelity realistic template simulation for offline sandbox
            return@withContext """
                🌟 [CHIC CLASSIFIED DISPLAY] Introducing our curated $title under the $category department!
                
                ✨ HIGHLIGHTS & KEY DETAILS:
                - Brand/Label: $brand
                - Selected Condition: Premium $condition grade.
                ${if (specs.isNotEmpty()) "- Custom Field Specs: $specs" else ""}
                - Aesthetic Quality: Curated design aligned with Nexkart's premium C2C specifications.
                
                🛡️ TRANSACTION ASSURANCE:
                This advertisement is covered by Nexkart Anti-Scam safeguards. Buy with absolute peace of mind. Use 'Make an Offer' to bargain and chat!
            """.trimIndent()
        }

        val prompt = "Write a highly professional, stylish, and engaging classified description for a $category product with the following details:\n" +
                "Title: $title\n" +
                "Brand: $brand\n" +
                "Condition: $condition\n" +
                "Extra Details: $specs\n" +
                "Add a professional trust segment reminding buyers about Nexkart Anti-Scam protection. Keep it markdown formatted and scannable."

        val requestBody = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.7f),
            systemInstruction = Content(parts = listOf(Part(text = "You are a world-class e-commerce copywriter. Write exquisite, high-converting product descriptions.")))
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, requestBody)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Failed to generate description text. Please verify specs fields."
        } catch (e: Exception) {
            "Excellent quality $title ($brand) in $condition condition. $specs. Fully checked and verified on Nexkart!"
        }
    }
}
