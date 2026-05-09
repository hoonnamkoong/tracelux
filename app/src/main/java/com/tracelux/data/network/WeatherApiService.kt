package com.tracelux.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    /**
     * Open-Meteo를 통한 시간별 기상 예보 조회
     */
    @GET("https://api.open-meteo.com/v1/forecast")
    suspend fun getOpenMeteoWeather(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("hourly") hourly: String = "temperature_2m,relative_humidity_2m,precipitation,cloud_cover,wind_speed_10m,wind_direction_10m,uv_index,visibility",
        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoResponse

    /**
     * 기상청 단기예보 조회 (한국 지역 정밀 날씨)
     */
    @GET("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst")
    suspend fun getKmaWeather(
        @Query("serviceKey") apiKey: String,
        @Query("numOfRows") rows: Int = 1000,
        @Query("dataType") type: String = "JSON",
        @Query("base_date") date: String,
        @Query("base_time") time: String,
        @Query("nx") nx: Int,
        @Query("ny") ny: Int
    ): KmaResponse
}
