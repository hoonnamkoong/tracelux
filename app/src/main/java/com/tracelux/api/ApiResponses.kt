package com.tracelux.api

import com.google.gson.annotations.SerializedName

/**
 * 기상청 (KMA) API 응답 모델
 */
data class KmaResponse(
    @SerializedName("response") val response: KmaResponseBodyWrapper
)

data class KmaResponseBodyWrapper(
    @SerializedName("header") val header: KmaHeader,
    @SerializedName("body") val body: KmaResponseBody?
)

data class KmaHeader(
    @SerializedName("resultCode") val resultCode: String,
    @SerializedName("resultMsg") val resultMsg: String
)

data class KmaResponseBody(
    @SerializedName("dataType") val dataType: String,
    @SerializedName("items") val items: KmaItems?,
    @SerializedName("numOfRows") val numOfRows: Int,
    @SerializedName("pageNo") val pageNo: Int,
    @SerializedName("totalCount") val totalCount: Int
)

data class KmaItems(
    @SerializedName("item") val item: List<KmaItem>
)

data class KmaItem(
    @SerializedName("baseDate") val baseDate: String,
    @SerializedName("baseTime") val baseTime: String,
    @SerializedName("category") val category: String,
    @SerializedName("fcstDate") val fcstDate: String,
    @SerializedName("fcstTime") val fcstTime: String,
    @SerializedName("fcstValue") val fcstValue: String,
    @SerializedName("nx") val nx: Int,
    @SerializedName("ny") val ny: Int
)

/**
 * Open-Meteo API 응답 모델
 */
data class OpenMeteoResponse(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("generationtime_ms") val generationtimeMs: Double,
    @SerializedName("utc_offset_seconds") val utcOffsetSeconds: Int,
    @SerializedName("timezone") val timezone: String,
    @SerializedName("timezone_abbreviation") val timezoneAbbreviation: String,
    @SerializedName("elevation") val elevation: Double,
    @SerializedName("hourly_units") val hourlyUnits: Map<String, String>,
    @SerializedName("hourly") val hourly: OpenMeteoHourly
)

data class OpenMeteoHourly(
    @SerializedName("time") val time: List<String>,
    @SerializedName("temperature_2m") val temperature2m: List<Double>,
    @SerializedName("relative_humidity_2m") val relativeHumidity2m: List<Double>,
    @SerializedName("dew_point_2m") val dewPoint2m: List<Double>,
    @SerializedName("cloud_cover") val cloudCover: List<Double>,
    @SerializedName("wind_speed_10m") val windSpeed10m: List<Double>,
    @SerializedName("wind_direction_10m") val windDirection10m: List<Double>,
    @SerializedName("wind_gusts_10m") val windGusts10m: List<Double>,
    @SerializedName("uv_index") val uvIndex: List<Double>,
    @SerializedName("visibility") val visibility: List<Double>,
    @SerializedName("precipitation_probability") val precipitationProbability: List<Double>,
    @SerializedName("cloud_base") val cloudBase: List<Double?>,
    @SerializedName("weather_code") val weatherCode: List<Int>
)

/**
 * Kakao Local API 응답 모델
 */
data class KakaoSearchResponse(
    @SerializedName("meta") val meta: KakaoMeta,
    @SerializedName("documents") val documents: List<KakaoDocumentResponse>
)

data class KakaoMeta(
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("pageable_count") val pageableCount: Int,
    @SerializedName("is_end") val isEnd: Boolean
)

data class KakaoDocumentResponse(
    @SerializedName("place_name") val placeName: String?,
    @SerializedName("address_name") val addressName: String,
    @SerializedName("road_address_name") val roadAddressName: String,
    @SerializedName("x") val x: String,
    @SerializedName("y") val y: String
)
