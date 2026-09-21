package com.example.data.api

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

interface GeminiEndpoints {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): Response<GenerateContentResponse>
}

data class GeminiGenerationResult(
    val text: String,
    val webSources: List<String> = emptyList(),
    val thinkingProcess: String? = null
)

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    const val MODEL_FLASH_25 = "gemini-2.5-flash"
    const val MODEL_PRO_25 = "gemini-2.5-pro"
    const val MODEL_FLASH_LATEST = "gemini-flash-latest"
    const val MODEL_FLASH_35 = "gemini-3.5-flash"

    private const val PREFS_NAME = "mindgpt_gemini_prefs"
    private const val KEY_CUSTOM_API_KEY = "custom_gemini_api_key"
    private const val KEY_SELECTED_MODEL = "selected_gemini_model"

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val api: GeminiEndpoints = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(GeminiEndpoints::class.java)

    fun getEffectiveApiKey(context: Context, inMemoryKey: String? = null): String {
        val mem = inMemoryKey?.trim() ?: ""
        if (mem.isNotEmpty() && !mem.startsWith("MY_")) {
            return mem
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_CUSTOM_API_KEY, "")?.trim() ?: ""
        if (saved.isNotEmpty() && !saved.startsWith("MY_")) {
            return saved
        }
        val buildKey = try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (_: Throwable) {
            ""
        }
        if (buildKey.isNotEmpty() && !buildKey.startsWith("MY_")) {
            return buildKey
        }
        return ""
    }

    fun saveCustomApiKey(context: Context, key: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CUSTOM_API_KEY, key.trim()).apply()
    }

    fun getSelectedModel(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SELECTED_MODEL, MODEL_FLASH_25) ?: MODEL_FLASH_25
    }

    fun saveSelectedModel(context: Context, model: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SELECTED_MODEL, model).apply()
    }

    suspend fun executeGenerateContent(
        context: Context,
        prompt: String,
        imageUri: Uri?,
        isThink: Boolean,
        isWebSearch: Boolean,
        inMemoryKey: String? = null
    ): Result<GeminiGenerationResult> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey(context, inMemoryKey)
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("کلید Google AI Studio API تنظیم نشده است. لطفاً از منوی تنظیمات کلید خود را وارد کنید.")
            )
        }

        val parts = mutableListOf<Part>()
        if (imageUri != null) {
            val base64 = uriToBase64(context, imageUri)
            if (base64 != null) {
                parts.add(Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64)))
            }
        }

        val cleanPrompt = if (prompt.isNotBlank()) {
            prompt
        } else if (imageUri != null) {
            "لطفاً این تصویر را با دقت کامل، تحلیل تخصصی و جزئیات بررسی و تشریح کن."
        } else {
            "سلام"
        }
        parts.add(Part(text = cleanPrompt))

        val systemInstruction = Content(
            parts = listOf(
                Part(
                    text = """
                        You are MindGPT, powered by Google AI Studio Gemini models.
                        You are helpful, precise, natural, and friendly.
                        Always respond in the user's language (natively in Persian/Farsi when addressed in Persian).
                        Use clear markdown formatting, bold points, and organized paragraphs.
                        ${if (isThink) "Perform deep reasoning and provide thorough, well-thought-out explanations." else ""}
                    """.trimIndent()
                )
            )
        )

        val generationConfig = GenerationConfig(
            temperature = if (isThink) 0.4f else 0.7f,
            topP = 0.95f,
            thinkingConfig = if (isThink) ThinkingConfig(thinkingBudget = 2048) else null
        )

        val tools = if (isWebSearch) {
            listOf(Tool(googleSearch = GoogleSearchTool()))
        } else null

        val request = GenerateContentRequest(
            contents = listOf(Content(role = "user", parts = parts)),
            systemInstruction = systemInstruction,
            generationConfig = generationConfig,
            tools = tools
        )

        // Try selected model, with automatic fallback list
        val primaryModel = if (isThink) MODEL_PRO_25 else getSelectedModel(context)
        val candidateModels = listOf(primaryModel, MODEL_FLASH_25, MODEL_FLASH_LATEST, MODEL_FLASH_35).distinct()

        var lastErrorMsg = "خطا در ارتباط با سرور گوگل استادیو"

        for (model in candidateModels) {
            try {
                val response = api.generateContent(model = model, apiKey = apiKey, request = request)
                if (response.isSuccessful) {
                    val body = response.body()
                    val candidate = body?.candidates?.firstOrNull()
                    val replyText = candidate?.content?.parts?.mapNotNull { it.text }?.joinToString("\n")

                    if (!replyText.isNullOrBlank()) {
                        val webSources = mutableListOf<String>()
                        candidate.groundingMetadata?.searchChunks?.forEach { chunk ->
                            chunk.web?.let { web ->
                                val title = web.title ?: web.uri ?: ""
                                if (title.isNotBlank()) webSources.add(title)
                            }
                        }

                        val thinking = if (isThink) {
                            "تحلیل با تفکر عمیق Google Gemini در مدل $model به اتمام رسید."
                        } else null

                        return@withContext Result.success(
                            GeminiGenerationResult(
                                text = replyText,
                                webSources = webSources,
                                thinkingProcess = thinking
                            )
                        )
                    }
                } else {
                    val errCode = response.code()
                    val errorBody = response.errorBody()?.string() ?: ""
                    if (errCode == 400 && errorBody.contains("API_KEY_INVALID", ignoreCase = true)) {
                        return@withContext Result.failure(
                            IllegalArgumentException("کلید API وارد شده معتبر نیست. لطفاً یک کلید معتبر از Google AI Studio وارد کنید.")
                        )
                    } else if (errCode == 429) {
                        return@withContext Result.failure(
                            IllegalStateException("سهمیه درخواست‌های Google AI Studio (Quota) به پایان رسیده است.")
                        )
                    } else if (errCode == 404) {
                        // Model not supported on this endpoint, try next candidate model
                        lastErrorMsg = "مدل $model در دسترس نبود."
                        continue
                    } else {
                        lastErrorMsg = "خطای سرور گوگل ($errCode): $errorBody"
                    }
                }
            } catch (e: Exception) {
                lastErrorMsg = e.localizedMessage ?: e.message ?: "خطای شبکه"
            }
        }

        Result.failure(Exception(lastErrorMsg))
    }

    suspend fun uriToBase64(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            bitmap?.let {
                val outputStream = ByteArrayOutputStream()
                val maxDim = 1280
                val ratio = if (it.width > maxDim || it.height > maxDim) {
                    val maxOriginal = maxOf(it.width, it.height)
                    maxDim.toFloat() / maxOriginal
                } else 1f
                val scaled = if (ratio < 1f) {
                    Bitmap.createScaledBitmap(it, (it.width * ratio).toInt(), (it.height * ratio).toInt(), true)
                } else it

                scaled.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                val bytes = outputStream.toByteArray()
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
