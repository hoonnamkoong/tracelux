package com.tracelux.data.network

import com.google.gson.annotations.SerializedName

/**
 * Open-Meteo API 응답 데이터 모델
 */
data class OpenMeteoResponse(
    @SerializedName("hourly") val hourly: HourlyData?
)

data class HourlyData(
    @SerializedName("time") val time: List<String>,
    @SerializedName("temperature_2m") val temperatures: List<Double>,
    @SerializedName("relative_humidity_2m") val humidities: List<Double>,
    @SerializedName("precipitation") val precipitations: List<Double>,
    @SerializedName("cloud_cover") val cloudCovers: List<Double>,
    @SerializedName("wind_speed_10m") val windSpeeds: List<Double>,
    @SerializedName("wind_direction_10m") val windDirections: List<Double>,
    @SerializedName("uv_index") val uvIndexes: List<Double>,
    @SerializedName("visibility") val visibilities: List<Double>
)

/**
 * 기상청 API 응답 데이터 모델 (JSON)
 */
data class KmaResponse(
    @SerializedName("response") val response: KmaResponseBody
)

data class KmaResponseBody(
    @SerializedName("body") val body: KmaItems?
)

data class KmaItems(
    @SerializedName("items") val items: KmaItemContainer?
)

data class KmaItemContainer(
    @SerializedName("item") val item: List<KmaItem>
)

data class KmaItem(
    @SerializedName("category") val category: String,
    @SerializedName("fcstDate") val fcstDate: String,
    @SerializedName("fcstTime") val fcstTime: String,
    @SerializedName("fcstValue") val fcstValue: String
)
