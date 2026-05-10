package com.tracelux.models

import kotlinx.serialization.Serializable

/**
 * 셔터 속도 모드
 */
@Serializable
enum class ShutterMode {
    FRACTION, // 분수 형태 (1/250 등)
    SECONDS,  // 초 단위 (5s 등)
    MINUTES   // 분 단위 (2m 등)
}

/**
 * 필드 화면의 설정 상태를 저장하는 데이터 클래스
 */
@Serializable
data class FieldState(
    val selectedCameraId: String? = null,
    val selectedLensId: String? = null,
    val aperture: String = "2.8",
    val iso: String = "100",
    val shutterValue: String = "250",
    val shutterMode: ShutterMode = ShutterMode.FRACTION,
    val currentFocal: String = "35",
    val ndFilters: List<Int> = emptyList() // 선택된 ND 필터들의 stop 값 목록
)

/**
 * 카메라 설정을 위한 표준 상수 목록
 */
object FieldConstants {
    val APERTURE_STOPS = listOf(
        "1.4", "1.8", "2.0", "2.4", "2.8", "3.2", "3.5", "4.0", "4.5", "5.0",
        "5.6", "6.3", "7.1", "8.0", "9.0", "10", "11", "13", "14", "16", "18", "20", "22"
    )

    val ISO_STOPS = listOf(
        "50", "100", "125", "200", "250", "400", "500", "800", "1000", "1600",
        "2000", "3200", "4000", "6400", "8000", "12800", "16000", "25600",
        "32000", "51200", "64000", "102400"
    ).sortedBy { it.toIntOrNull() ?: 0 }

    val SHUTTER_FRACTION_STOPS = listOf(
        "30", "40", "50", "60", "80", "100", "125", "160", "200", "250",
        "320", "400", "500", "640", "800", "1000", "1250", "1600", "2000", "2500", "3200", "4000"
    )

    val SHUTTER_TIME_STOPS = (1..30).map { it.toString() }

    val FOCAL_STOPS = listOf(
        "12", "14", "16", "18", "20", "24", "28", "35", "50", "70", "85", "105", "135", "200"
    )

    // ND 노출 보정 계산 시 가장 가까운 셔터 속도를 찾기 위한 표준 값 목록
    val STANDARD_SHUTTER_SPEEDS = listOf(
        1.25E-4, 1.5625E-4, 2.0E-4, 2.5E-4, 3.125E-4, 4.0E-4, 5.0E-4, 6.25E-4, 8.0E-4,
        0.001, 0.00125, 0.0015625, 0.002, 0.0025, 0.003125, 0.004, 0.005, 0.00625, 0.008,
        0.01, 0.0125, 0.016666666666666666, 0.02, 0.025, 0.03333333333333333, 0.04, 0.05,
        0.06666666666666667, 0.07692307692307693, 0.1, 0.125, 0.16666666666666666, 0.2,
        0.25, 0.3, 0.4, 0.5, 0.6, 0.8, 1.0, 1.3, 1.6, 2.0, 2.5, 3.2, 4.0, 5.0, 6.0, 8.0,
        10.0, 13.0, 15.0, 20.0, 25.0, 30.0
    )
}
