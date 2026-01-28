package com.sergeapps.stock.data

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.create
import kotlinx.serialization.json.Json
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import java.util.concurrent.TimeUnit

object StockApiFactory {

    fun create(
        settings: StockSettings
    ): StockApiService {
        val normalizedBase = settings.baseUrl.trim().removeSuffix("/")
        val portPart = settings.port.trim().takeIf { it.isNotBlank() }?.let { ":$it" } ?: ""
        val baseUrl = "$normalizedBase$portPart/api/stock/"
        val headerInterceptor = Interceptor { chain ->
            val original = chain.request()
            val builder = original.newBuilder()

            if (settings.apiKey.isNotBlank()) {
                builder.header("X-API-Key", settings.apiKey)
            }

            chain.proceed(builder.build())
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .callTimeout(15, TimeUnit.SECONDS)
            .build()

        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        val contentType = "application/json".toMediaType()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create()
    }
}

