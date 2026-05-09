package com.tracelux.utils

import org.shredzone.commons.suncalc.*
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

/**
 * 원본 APK의 SolarCalculator.java를 1:1로 복원한 Kotlin 클래스입니다.
 * 천체(해, 달, 은하수)의 위치 및 출몰 시간을 계산합니다.
 */
object SolarCalculator {

    private fun Date.toZonedDateTime(): ZonedDateTime {
        return this.toInstant().atZone(ZoneId.systemDefault())
    }

    private fun ZonedDateTime.toDate(): Date {
        return Date.from(this.toInstant())
    }

    /**
     * 태양의 위치(방위각, 고도)를 계산합니다.
     */
    fun getSunPosition(date: Date, lat: Double, lng: Double): SunPositionData {
        val pos = SunPosition.compute()
            .on(date.toZonedDateTime())
            .at(lat, lng)
            .execute()
        return SunPositionData(pos.azimuth, pos.altitude)
    }

    /**
     * 달의 위치(방위각, 고도, 거리)를 계산합니다.
     */
    fun getMoonPosition(date: Date, lat: Double, lng: Double): MoonPositionData {
        val pos = MoonPosition.compute()
            .on(date.toZonedDateTime())
            .at(lat, lng)
            .execute()
        return MoonPositionData(pos.azimuth, pos.altitude, pos.distance)
    }

    /**
     * 달의 위상 및 연령을 계산합니다.
     */
    fun getMoonIllumination(date: Date, lat: Double, lon: Double): MoonIlluminationData {
        val illumination = MoonIllumination.compute()
            .on(date.toZonedDateTime())
            .at(lat, lon)
            .execute()
        
        // commons-suncalc getPhase() 반환 범위: -180 ~ +180
        //   0도   = 망 (Full Moon)
        //  ±180도 = 삭 (New Moon)
        //  -90도  = 하현 (Last Quarter, 주는 중)
        //  +90도  = 상현 (First Quarter, 차오르는 중)
        // KASI 기준 정규화 (0=삭, 0.25=상현, 0.5=망, 0.75=하현, 1=삭):
        val phaseAngle = illumination.phase
        val normalizedPhase = (phaseAngle + 180.0) / 360.0 // 0=New(-180°), 0.5=Full(0°), 1=New(+180°)
        val age = 29.53059 * normalizedPhase
        return MoonIlluminationData(normalizedPhase, age)
    }

    /**
     * 상세한 태양 관련 시간(일출, 일몰, 골든아워 등)을 계산합니다.
     */
    fun getDetailedSunTimes(date: Date, lat: Double, lon: Double): Map<String, Date?> {
        // 당일 자정부터 시작해야 오전 출몰 시간을 정확히 찾음 (정오 기준이면 다음날 시간 반환)
        val startOfDay = date.toZonedDateTime().withHour(0).withMinute(0).withSecond(0).withNano(0)
        
        val visual = SunTimes.compute().on(startOfDay).at(lat, lon).execute()
        val golden = SunTimes.compute().on(startOfDay).at(lat, lon).twilight(SunTimes.Twilight.GOLDEN_HOUR).execute()
        val blue = SunTimes.compute().on(startOfDay).at(lat, lon).twilight(SunTimes.Twilight.BLUE_HOUR).execute()
        val civil = SunTimes.compute().on(startOfDay).at(lat, lon).twilight(SunTimes.Twilight.CIVIL).execute()

        // commons-suncalc Twilight 기준:
        // CIVIL = 태양고도 -6도 (Blue Hour 시작)
        // BLUE_HOUR = 태양고도 -4도 (Blue↔Golden 경계)
        // GOLDEN_HOUR = 태양고도 +6도 (Golden Hour 종료)
        return mapOf(
            "sunrise" to visual.rise?.toDate(),
            "sunset" to visual.set?.toDate(),
            "morningBlueHourStart" to civil.rise?.toDate(),
            "morningBlueHourEnd" to blue.rise?.toDate(),
            "morningGoldenHourStart" to blue.rise?.toDate(),
            "morningGoldenHourEnd" to golden.rise?.toDate(),
            "eveningGoldenHourStart" to golden.set?.toDate(),
            "eveningGoldenHourEnd" to blue.set?.toDate(),
            "eveningBlueHourStart" to blue.set?.toDate(),
            "eveningBlueHourEnd" to civil.set?.toDate()
        )
    }

    /**
     * 월출 및 월몰 시간을 계산합니다.
     */
    fun getMoonTimes(date: Date, lat: Double, lon: Double): Map<String, Date?> {
        val startOfDay = date.toZonedDateTime().withHour(0).withMinute(0).withSecond(0).withNano(0)
        val times = MoonTimes.compute().on(startOfDay).at(lat, lon).execute()
        
        return mapOf(
            "moonrise" to times.rise?.toDate(),
            "moonset" to times.set?.toDate()
        )
    }

    /**
     * 은하수 관측 최적 시간을 계산합니다.
     * (태양 고도 < -18도, 달 고도 < 0도, 은하 중심 고도 > 0도 조건)
     */
    fun calculateMilkyWayBest(date: Date, lat: Double, lon: Double): MilkyWayResult {
        val calendar = Calendar.getInstance()
        calendar.time = date
        
        var it = date.toZonedDateTime().withHour(12).withMinute(0).withSecond(0)
        if (calendar.get(Calendar.HOUR_OF_DAY) < 12) {
            it = it.minusDays(1)
        }

        var bestStart: Date? = null
        var bestEnd: Date? = null

        for (i in 0 until 289) { // 5분 간격으로 24시간 체크
            val checkTime = it.plusMinutes(i.toLong() * 5)
            val d = Date.from(checkTime.toInstant())
            
            val sunAlt = SunPosition.compute().on(checkTime).at(lat, lon).execute().altitude
            val moonAlt = MoonPosition.compute().on(checkTime).at(lat, lon).execute().altitude
            val gcAlt = getGalacticCenterAlt(d, lat, lon)

            if (sunAlt < -18.0 && moonAlt < 0.0 && gcAlt > 0.0) {
                if (bestStart == null) bestStart = d
                bestEnd = d
            }
        }

        return MilkyWayResult(
            start = bestStart,
            end = bestEnd,
            status = if (bestStart == null) "관측 불가" else null
        )
    }

    fun getGalacticCenterAlt(date: Date, lat: Double, lon: Double): Double {
        return getCelestialPosition(266.4, -29.0, date, lat, lon).altitude
    }

    /**
     * 특정 적경(RA), 적위(Dec)를 가진 천체의 지평 좌표를 계산합니다.
     */
    fun getCelestialPosition(raDeg: Double, decDeg: Double, date: Date, lat: Double, lon: Double): SunPositionData {
        val decRad = Math.toRadians(decDeg)
        val latRad = Math.toRadians(lat)
        
        val d = (date.time / 8.64E7) - 10957.5
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.time = date
        
        val utHours = calendar.get(Calendar.HOUR_OF_DAY) + 
                     (calendar.get(Calendar.MINUTE) / 60.0) + 
                     (calendar.get(Calendar.SECOND) / 3600.0)
        
        var lst = ((0.985647 * d) + 100.46 + lon + (15.0 * utHours)) % 360.0
        if (lst < 0.0) lst += 360.0
        
        val haRad = Math.toRadians(lst - raDeg)
        val alt = Math.asin(Math.sin(latRad) * Math.sin(decRad) + 
                           Math.cos(latRad) * Math.cos(decRad) * Math.cos(haRad))
        
        val az = Math.atan2(-Math.cos(decRad) * Math.sin(haRad),
                           Math.cos(latRad) * Math.sin(decRad) - 
                           Math.sin(latRad) * Math.cos(decRad) * Math.cos(haRad))
        
        var azDeg = Math.toDegrees(az)
        if (azDeg < 0.0) azDeg += 360.0
        
        return SunPositionData(azDeg, Math.toDegrees(alt))
    }

    fun getPolarisPosition(date: Date, lat: Double, lon: Double): SunPositionData {
        return getCelestialPosition(37.95, 89.26, date, lat, lon)
    }

    fun getGalacticCenterPosition(date: Date, lat: Double, lon: Double): SunPositionData {
        return getCelestialPosition(266.4, -29.0, date, lat, lon)
    }
}

// 데이터 모델들
data class SunPositionData(val azimuth: Double, val altitude: Double)
data class MoonPositionData(val azimuth: Double, val altitude: Double, val distance: Double)
data class MoonIlluminationData(val fraction: Double, val age: Double)
data class MilkyWayResult(val start: Date?, val end: Date?, val status: String?)
