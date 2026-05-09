package com.tracelux.api

import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * API 클라이언트 설정 클래스
 */
object RetrofitClient {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val kmaRetrofit = Retrofit.Builder()
        .baseUrl("https://apis.data.go.kr/")
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val openMeteoRetrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val kakaoRetrofit = Retrofit.Builder()
        .baseUrl("https://dapi.kakao.com/")
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val kmaApi: WeatherApi = kmaRetrofit.create(WeatherApi::class.java)
    val openMeteoApi: WeatherApi = openMeteoRetrofit.create(WeatherApi::class.java)
    val kakaoApi: KakaoApi = kakaoRetrofit.create(KakaoApi::class.java)
}
