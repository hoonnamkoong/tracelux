package com.tracelux.ui.utils

import java.util.*

/**
 * 기상 데이터의 단위 변환 및 문자열 포맷팅을 담당하는 유틸리티
 */
object WeatherFormatter {

    fun getTemp(isImp: Boolean, v: Double?): Pair<String, String> {
        if (v == null) return "-" to ""
        var temp = v
        if (isImp) temp = (temp * 1.8) + 32
        return String.format("%.1f", temp) to (if (isImp) "°F" else "°C")
    }

    fun getWind(isImp: Boolean, v: Double?): Pair<String, String> {
        if (v == null) return "-" to ""
        var speed = v
        if (isImp) speed *= 2.23694
        return String.format("%.1f", speed) to (if (isImp) "mph" else "m/s")
    }

    fun getDist(isImp: Boolean, v: Double?): Pair<String, String> {
        if (v == null) return "-" to ""
        var dist = v
        if (isImp) dist *= 0.621371
        return String.format("%.1f", dist) to (if (isImp) "mi" else "km")
    }

    fun getAlt(isImp: Boolean, v: Double?): Pair<String, String> {
        if (v == null) return "-" to ""
        var alt = v
        if (isImp) alt *= 3.28084
        return String.format("%.1f", alt / 1000.0) to (if (isImp) "k ft" else "km")
    }

    fun getWave(isImp: Boolean, v: Double?): Pair<String, String> {
        if (v == null) return "-" to ""
        var wave = v
        if (isImp) wave *= 3.28084
        return String.format("%.1f", wave) to (if (isImp) "ft" else "M")
    }
}
