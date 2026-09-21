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

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    const val DEFAULT_MODEL = "gemini-2.5-flash"

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

    fun getEffectiveApiKey(customKey: String?): String {
        val userCustom = customKey?.trim() ?: ""
        if (userCustom.isNotEmpty() && !userCustom.startsWith("MY_")) {
            return userCustom
        }
        val buildKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Throwable) {
            ""
        }
        if (buildKey.isNotBlank() && !buildKey.startsWith("MY_")) {
            return buildKey
        }
        return ""
    }

    suspend fun uriToBase64(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            bitmap?.let {
                val outputStream = ByteArrayOutputStream()
                // Resize if too large to optimize payload
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
