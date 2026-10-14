package com.example.data.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface UpiSmartApiService {

    @GET("api/health")
    suspend fun getHealth(): Response<NetworkHealthResponse>

    @GET("api/budget/utilization")
    suspend fun getBudgetUtilization(): Response<NetworkBudgetResponse>

    @POST("api/transactions")
    suspend fun postTransaction(@Body request: NetworkPaymentRequest): Response<NetworkPaymentResponse>

    @GET("api/vault")
    suspend fun getVaultStatus(): Response<NetworkVaultResponse>
}

object ApiClient {
    // Default fallback to local emulator loopback or demo endpoint
    private var baseUrl: String = "https://upismart-backend.run.app/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    fun updateBaseUrl(newUrl: String) {
        val sanitized = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        baseUrl = sanitized
    }

    fun getService(): UpiSmartApiService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(UpiSmartApiService::class.java)
    }
}
