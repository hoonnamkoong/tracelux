package com.tracelux.api

import com.tracelux.models.KmaResponse
import com.tracelux.models.OpenMeteoResponse
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 날씨 API 인터페이스 (기상청 및 Open-Meteo)
 * APK 원본의 com.hoonnk.landscapephotoassistant.api.WeatherApi와 1:1 대응
 */
interface WeatherApi {
    @GET("https://api.open-meteo.com/v1/forecast")
    suspend fun getOpenMeteoForecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("hourly") hourly: String = "temperature_2m,relative_humidity_2m,dew_point_2m,apparent_temperature,precipitation_probability,precipitation,weather_code,pressure_msl,cloud_cover,visibility,wind_speed_10m,wind_direction_10m,uv_index,is_day",
        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoResponse

    @GET("api/typ01/cgi-bin/url/nph-dfs_xy_lonlat")
    suspend fun getKmaCoordinate(
        @Query("lon") lon: Double,
        @Query("lat") lat: Double,
        @Query("help") help: Int = 1,
        @Query("authKey") authKey: String
    ): ResponseBody

    @GET("1360000/VilageFcstInfoService_2.0/getUltraSrtFcst")
    suspend fun getKmaUltraShortForecast(
        @Query("serviceKey") authKey: String,
        @Query("numOfRows") numOfRows: Int = 1000,
        @Query("pageNo") pageNo: Int = 1,
        @Query("dataType") dataType: String = "JSON",
        @Query("base_date") baseDate: String,
        @Query("base_time") baseTime: String,
        @Query("nx") nx: Int,
        @Query("ny") ny: Int
    ): KmaResponse

    @GET("1360000/VilageFcstInfoService_2.0/getVilageFcst")
    suspend fun getKmaVilageForecast(
        @Query("serviceKey") authKey: String,
        @Query("numOfRows") numOfRows: Int = 1000,
        @Query("pageNo") pageNo: Int = 1,
        @Query("dataType") dataType: String = "JSON",
        @Query("base_date") baseDate: String,
        @Query("base_time") baseTime: String,
        @Query("nx") nx: Int,
        @Query("ny") ny: Int
    ): KmaResponse
}
