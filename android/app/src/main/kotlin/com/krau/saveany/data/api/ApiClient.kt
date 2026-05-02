package com.krau.saveany.data.api

import android.util.Base64
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object ApiClient {
    val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
    }

    private val mediaType = "application/json".toMediaType()

    fun build(baseUrl: String, token: String): SaveAnyApi {
        val parsed = baseUrl.toHttpUrlOrNull()
        val basicHeader = parsed?.let { url ->
            if (url.username.isNotEmpty()) {
                val raw = "${url.username}:${url.password}"
                "Basic " + Base64.encodeToString(raw.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            } else null
        }

        val cleanBaseRaw = parsed?.newBuilder()?.username("")?.password("")?.build()?.toString() ?: baseUrl
        val normalized = if (cleanBaseRaw.endsWith("/")) cleanBaseRaw else "$cleanBaseRaw/"

        val httpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder().apply {
                    when {
                        basicHeader != null -> header("Authorization", basicHeader)
                        token.isNotBlank() -> header("Authorization", "Bearer $token")
                    }
                    header("Accept", "application/json")
                }.build()
                chain.proceed(request)
            }
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .build()

        return Retrofit.Builder()
            .baseUrl(normalized)
            .client(httpClient)
            .addConverterFactory(json.asConverterFactory(mediaType))
            .build()
            .create(SaveAnyApi::class.java)
    }
}
