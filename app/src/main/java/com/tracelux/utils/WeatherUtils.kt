package com.tracelux.utils

import com.tracelux.api.WeatherApi
import com.tracelux.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

object WeatherUtils {
    const val KMA_AUTH_KEY = "58e0775c428b98d105d19fa0327bfd15772bb789ebeae55305678eedf8d90aff"

    /**
     * 기상 코드를 기반으로 날씨 설명을 반환합니다.
     * 원본 WeatherUtils.java의 getWeatherDesc와 1:1 대응
     */
    fun getWeatherDesc(code: Int): String {
        return when {
            code == 0 -> "Clear Sky"
            code in 1..3 -> "Partly Cloudy"
            code == 45 || code == 48 -> "Foggy"
            code in 51..67 -> "Rainy"
            code in 71..86 -> "Snowy"
            code in 95..100 -> "Thunderstorm"
            else -> "Cloudy"
        }
    }

    /** KMA SKY 코드(1=맑음,3=구름많음,4=흐림)로 날씨 설명 반환 */
    fun getKmaSkyDesc(sky: Int?, pty: Int?): String {
        if (pty != null && pty > 0) return when (pty) {
            1 -> "RAINY"
            2 -> "RAIN/SNOW"
            3 -> "SNOWY"
            4 -> "SHOWERY"
            else -> "RAINY"
        }
        return when (sky) {
            1 -> "CLEAR SKY"
            3 -> "PARTLY CLOUDY"
            4 -> "CLOUDY"
            else -> "CLOUDY"
        }
    }

    /**
     * 하늘 상태 코드를 설명으로 변환합니다.
     */
    fun getSkyDesc(code: Int?): String {
        return when (code) {
            1 -> "Sunny"
            3 -> "Partly"
            4 -> "Cloudy"
            else -> "-"
        }
    }

    /**
     * 강수 형태 코드를 설명으로 변환합니다.
     */
    fun getPtyDesc(code: Int?): String {
        return when (code) {
            0 -> "None"
            1 -> "Rain"
            2 -> "Rain/Snow"
            3 -> "Snow"
            4 -> "Shower"
            else -> "-"
        }
    }

    /**
     * 풍향 각도를 텍스트로 변환합니다.
     */
    fun getWindDirectionText(degree: Double?, windSpeed: Double? = null, isKo: Boolean = true): String {
        if (degree == null) return "-"
        val directionsEn = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW", "N")
        val index = (((degree + 22.5) % 360) / 45).toInt()
        return directionsEn[index]
    }

    /**
     * 이슬점 온도를 계산합니다.
     */
    fun calculateDewPoint(temp: Double, humidity: Double): Double {
        val safeHumidity = if (humidity <= 0.0) 0.1 else humidity
        val alpha = ((17.27 * temp) / (237.7 + temp)) + ln(safeHumidity / 100.0)
        return (237.7 * alpha) / (17.27 - alpha)
    }

    /**
     * 운고(구름 높이)를 계산합니다.
     */
    fun calculateCloudBase(temp: Double, humidity: Double): Double {
        val cloudBase = (temp - calculateDewPoint(temp, humidity)) * 125.0
        return if (cloudBase < 0.0) 0.0 else cloudBase
    }


    fun convertToKmaGrid(lat: Double, lon: Double): Pair<Int, Int> {
        val RE = 6371.00877
        val GRID = 5.0
        val SLAT1 = 30.0
        val SLAT2 = 60.0
        val OLON = 126.0
        val OLAT = 38.0
        val XO = 43.0
        val YO = 136.0

        val DEGRAD = Math.PI / 180.0
        val re = RE / GRID
        val slat1 = SLAT1 * DEGRAD
        val slat2 = SLAT2 * DEGRAD
        val olon = OLON * DEGRAD
        val olat = OLAT * DEGRAD

        var sn = tan(Math.PI * 0.25 + slat2 * 0.5) / tan(Math.PI * 0.25 + slat1 * 0.5)
        sn = ln(cos(slat1) / cos(slat2)) / ln(sn)
        var sf = tan(Math.PI * 0.25 + slat1 * 0.5)
        sf = (sf.pow(sn) * cos(slat1)) / sn
        var ro = tan(Math.PI * 0.25 + olat * 0.5)
        ro = (re * sf) / ro.pow(sn)

        var ra = tan(Math.PI * 0.25 + lat * DEGRAD * 0.5)
        ra = (re * sf) / ra.pow(sn)
        var theta = lon * DEGRAD - olon
        if (theta > Math.PI) theta -= 2.0 * Math.PI
        if (theta < -Math.PI) theta += 2.0 * Math.PI
        theta *= sn

        val nx = (ra * sin(theta) + XO + 0.5).toInt()
        val ny = (ro - ra * cos(theta) + YO + 0.5).toInt()

        return Pair(nx, ny)
    }

    fun getVilageBase(): Pair<String, String> {
        val d = Calendar.getInstance()
        val hour = d.get(Calendar.HOUR_OF_DAY)
        val minute = d.get(Calendar.MINUTE)
        val times = listOf(2, 5, 8, 11, 14, 17, 20, 23)
        var baseH = 23
        val baseD = d.clone() as Calendar
        
        for (i in times.indices.reversed()) {
            if (hour > times[i] || (hour == times[i] && minute >= 20)) {
                baseH = times[i]
                break
            }
        }
        
        if (hour < 2 || (hour == 2 && minute < 20)) {
            baseD.add(Calendar.DAY_OF_MONTH, -1)
            baseH = 23
        }
        
        val df = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return Pair(df.format(baseD.time), String.format("%02d00", baseH))
    }

    fun getUltraBase(): Pair<String, String> {
        val d = Calendar.getInstance()
        val minute = d.get(Calendar.MINUTE)
        if (minute < 45) {
            d.add(Calendar.HOUR_OF_DAY, -1)
        }
        val df = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val hourDf = SimpleDateFormat("HH30", Locale.getDefault())
        return Pair(df.format(d.time), hourDf.format(d.time))
    }

    fun parseWeatherDate(timeStr: String): Date {
        return try {
            val trimmed = timeStr.trim()
            if (trimmed.contains("-") || trimmed.contains("T")) {
                val pattern = if (trimmed.length > 16) "yyyy-MM-dd'T'HH:mm:ss" else "yyyy-MM-dd'T'HH:mm"
                SimpleDateFormat(pattern, Locale.US).parse(trimmed) ?: Date()
            } else {
                val pattern = when (trimmed.length) {
                    8 -> "yyyyMMdd"
                    10 -> "yyyyMMddHH"
                    else -> "yyyyMMddHHmm"
                }
                SimpleDateFormat(pattern, Locale.US).parse(trimmed) ?: Date()
            }
        } catch (e: Exception) { Date() }
    }

    fun formatVal(v: Double?): String = if (v == null) "-" else String.format("%.1f", v)
    fun formatIntVal(v: Double?): String = if (v == null) "-" else String.format("%.0f", v)

    suspend fun fetchWeather(
        source: String,
        omApi: WeatherApi,
        kmaApi: WeatherApi,
        lat: Double,
        lon: Double,
        authKey: String = KMA_AUTH_KEY
    ): List<HourlyWeather> = withContext(Dispatchers.IO) {
        val result = mutableListOf<HourlyWeather>()
        try {
            if (source == "KMA") {
                val grid = convertToKmaGrid(lat, lon)
                val uBase = getUltraBase()
                val vBase = getVilageBase()
                
                val uResp = kmaApi.getKmaUltraShortForecast(authKey, baseDate = uBase.first, baseTime = uBase.second, nx = grid.first, ny = grid.second)
                val vResp = kmaApi.getKmaVilageForecast(authKey, baseDate = vBase.first, baseTime = vBase.second, nx = grid.first, ny = grid.second)
                
                val kmaMap = mutableMapOf<String, MutableMap<String, String>>()
                fun processItems(items: List<KmaItem>?) {
                    items?.forEach { item ->
                        val key = "${item.fcstDate}${item.fcstTime}"
                        kmaMap.getOrPut(key) { mutableMapOf() }[item.category] = item.fcstValue
                    }
                }
                // 단기 예보를 먼저 처리한 뒤, 정확도가 더 높은 초단기 예보로 덮어쓰기 (실제 기상청 방식)
                processItems(vResp.response.body?.items?.item)
                processItems(uResp.response.body?.items?.item)

                kmaMap.keys.sorted().forEach { key ->
                    val vals = kmaMap[key]!!
                    val timeDate = SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault()).parse(key) ?: Date()
                    val timeStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault()).format(timeDate)
                    
                    result.add(HourlyWeather(
                        time = timeStr,
                        temp = vals["T1H"]?.toDoubleOrNull() ?: vals["TMP"]?.toDoubleOrNull(),
                        humidity = vals["REH"]?.toDoubleOrNull(),
                        weatherCode = vals["SKY"]?.toIntOrNull() ?: 0,
                        pop = vals["POP"]?.toDoubleOrNull(),
                        sky = vals["SKY"]?.toIntOrNull(),
                        pty = vals["PTY"]?.toIntOrNull(),
                        windSpeed = vals["WSD"]?.toDoubleOrNull(),
                        windDirection = vals["VEC"]?.toDoubleOrNull(),
                        precipitation = (vals["RN1"] ?: vals["PCP"])?.let { if (it == "강수없음") 0.0 else it.replace("mm", "").toDoubleOrNull() },
                        lgt = vals["LGT"]?.toIntOrNull(),
                        wav = vals["WAV"]?.toDoubleOrNull(),
                        source = "KMA"
                    ))
                }
            } else {
                val omResp = omApi.getOpenMeteoForecast(lat, lon)
                omResp.hourly.let { h ->
                    for (i in h.time.indices) {
                        result.add(HourlyWeather(
                            time = h.time[i],
                            temp = h.temperature2m[i],
                            humidity = h.relativeHumidity2m[i],
                            dewPoint = h.dewPoint2m[i],
                            feelsLike = h.feelsLike?.get(i),
                            pop = h.precipitationProbability[i],
                            prec = h.precipitation?.get(i),
                            weatherCode = h.weatherCode[i],
                            pressure = h.pressure?.get(i),
                            cloudCover = h.cloudCover[i],
                            visibility = h.visibility[i],
                            windSpeed = h.windSpeed10m[i],
                            windDirection = h.windDirection10m[i],
                            uvIndex = h.uvIndex[i],
                            source = "Open-Meteo"
                        ))
                    }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        
        // 현재 시간 다음 정각 시간부터 표기되도록 필터링
        val cal = Calendar.getInstance()
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.HOUR_OF_DAY, 1)
        val nextHour = cal.time

        result.filter { 
            val d = parseWeatherDate(it.time)
            !d.before(nextHour)
        }
    }
}
