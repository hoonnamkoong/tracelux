package com.tracelux.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 천체 및 기상 데이터 모델 클래스
 * APK 원본의 HourlyWeather 구조와 1:1 대응
 */

@Serializable
data class HourlyWeather(
    val time: String,
    val temp: Double? = null,
    val dewPoint: Double? = null,
    val humidity: Double? = null,
    val cloudCover: Double? = null,
    val cloudBase: Double? = null,
    val windSpeed: Double? = null,
    val windDirection: Double? = null,
    val windGust: Double? = null,
    val uvIndex: Double? = null,
    val visibility: Double? = null,
    val precipitation: Double? = null,
    val prec: Double? = null,
    val pop: Double? = null,
    val feelsLike: Double? = null,
    val pressure: Double? = null,
    val snow: Double? = null,
    val sky: Int? = null,
    val pty: Int? = null,
    val wav: Double? = null,
    val wavePeriod: Double? = null,
    val lgt: Int? = null,
    val weatherCode: Int,
    val source: String
)

@Serializable
data class WeatherData(
    val location: String,
    val currentTemp: Double,
    val condition: String,
    val windSpeed: Double,
    val windDirection: Int,
    val humidity: Int,
    val precipitation: Int,
    val cloudCover: Int,
    val uvIndex: Int,
    val feelsLike: Double,
    val visibility: Double,
    val hourly: List<HourlyWeather>
)

enum class AppUnit(val label: String, val description: String) {
    METRIC("METRIC", "°C, km, m"),
    IMPERIAL("IMPERIAL", "°F, mi, ft")
}

@Serializable
data class KakaoDocument(
    val place_name: String? = null,
    val address_name: String,
    val x: String,
    val y: String
)

@Serializable
data class KakaoSearchResponse(
    val documents: List<KakaoDocument>
)

// --- KMA Response Models ---

@Serializable
data class KmaResponse(
    val response: KmaResponseBodyWrapper
)

@Serializable
data class KmaResponseBodyWrapper(
    val body: KmaResponseBody? = null
)

@Serializable
data class KmaResponseBody(
    val items: KmaItems? = null
)

@Serializable
data class KmaItems(
    val item: List<KmaItem>
)

@Serializable
data class KmaItem(
    val baseDate: String,
    val baseTime: String,
    val category: String,
    val fcstDate: String,
    val fcstTime: String,
    val fcstValue: String,
    val nx: Int,
    val ny: Int
)

// --- Open-Meteo Response Models ---

@Serializable
data class OpenMeteoResponse(
    val hourly: OpenMeteoHourly
)

@Serializable
data class OpenMeteoHourly(
    val time: List<String>,
    @SerialName("temperature_2m") val temperature2m: List<Double>,
    @SerialName("relative_humidity_2m") val relativeHumidity2m: List<Double>,
    @SerialName("dew_point_2m") val dewPoint2m: List<Double>,
    @SerialName("apparent_temperature") val feelsLike: List<Double>? = null,
    @SerialName("precipitation_probability") val precipitationProbability: List<Double>,
    @SerialName("precipitation") val precipitation: List<Double>? = null,
    @SerialName("weather_code") val weatherCode: List<Int>,
    @SerialName("pressure_msl") val pressure: List<Double>? = null,
    @SerialName("cloud_cover") val cloudCover: List<Double>,
    @SerialName("visibility") val visibility: List<Double>,
    @SerialName("wind_speed_10m") val windSpeed10m: List<Double>,
    @SerialName("wind_direction_10m") val windDirection10m: List<Double>,
    @SerialName("uv_index") val uvIndex: List<Double>,
    @SerialName("is_day") val isDay: List<Int>? = null
)
