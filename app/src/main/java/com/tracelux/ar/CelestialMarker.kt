package com.tracelux.ar

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * AR 화면에 표시될 천체(해, 달 등) 마커 데이터 클래스
 */
data class CelestialMarker(
    val name: String,
    val azimuth: Float,
    val altitude: Float,
    val icon: ImageVector,
    val color: Color,
    val phase: Double? = null
)
