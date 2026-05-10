package com.tracelux.ar

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import java.util.Random

/**
 * 천체 마커 오버레이 - 원본 APK의 회전 행렬 기반 3D 투영 방식
 * @param markers 표시할 천체 마커 목록
 * @param rotationMatrix 센서에서 추출한 3x3 회전 행렬 (float[9], row-major)
 * @param magneticDeclination 자기 편각 (도 단위)
 * @param hFov 기기 수평 화각 (도 단위)
 * @param vFov 기기 수직 화각 (도 단위)
 */
@Composable
fun ArCelestialOverlay(
    markers: List<CelestialMarker>,
    rotationMatrix: FloatArray,
    magneticDeclination: Float,
    hFov: Float,
    vFov: Float
) {
    val density = LocalDensity.current
    // 원본 APK: labelPaint를 remember로 캐싱
    val labelPaint = remember(density) {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = with(density) { 10.sp.toPx() }
            isFakeBoldText = true
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f

        // 회전 행렬이 초기화되지 않은 경우(모두 0) 스킵
        val matrixNorm = rotationMatrix.fold(0f) { acc, v -> acc + v * v }
        if (matrixNorm < 0.5f) return@Canvas

        val hFovRad = Math.toRadians(hFov.toDouble())
        val vFovRad = Math.toRadians(vFov.toDouble())
        val hTan = Math.tan(hFovRad / 2.0)
        val vTan = Math.tan(vFovRad / 2.0)
        val R = rotationMatrix
        val magDecRad = Math.toRadians(magneticDeclination.toDouble())

        markers.forEach { marker ->
            if (marker.altitude < 0f) return@forEach // 지평선 아래 객체 숨김

            val azRad = Math.toRadians(marker.azimuth.toDouble()) + magDecRad
            val altRad = Math.toRadians(marker.altitude.toDouble())

            // 천체의 세계 좌표 (ENU: x=East, y=North, z=Up)
            val wE = (Math.cos(altRad) * Math.sin(azRad)).toFloat()
            val wN = (Math.cos(altRad) * Math.cos(azRad)).toFloat()
            val wU = Math.sin(altRad).toFloat()

            // 회전 행렬 전치(Transpose)로 세계→기기 좌표 변환
            val dx = R[0] * wE + R[3] * wN + R[6] * wU
            val dy = R[1] * wE + R[4] * wN + R[7] * wU
            val dz = R[2] * wE + R[5] * wN + R[8] * wU

            // 카메라는 기기 -Z 방향 (depth = -dz)
            val depth = -dz
            val x = (centerX + centerX.toDouble() * dx.toDouble() / (depth.toDouble() * hTan)).toFloat()
            val y = (centerY - centerY.toDouble() * dy.toDouble() / (depth.toDouble() * vTan)).toFloat()

            if (depth > 0.01f && x >= -60f && x <= size.width + 60f && y >= -60f && y <= size.height + 60f) {
                drawMarker(marker, x, y, labelPaint, density)
            } else {
                // 오프스크린 화살표 표시
                val vx = (dx / hTan).toFloat()
                val vy = (-dy / vTan).toFloat()
                val angle = Math.atan2(vy.toDouble(), vx.toDouble()).toFloat()

                val rX = centerX - 32f * density.density
                val rY = centerY - 32f * density.density
                
                // vx, vy가 0인 경우 방어 로직
                if (Math.abs(vx.toDouble()) > 0.001 || Math.abs(vy.toDouble()) > 0.001) {
                    val scaleX = Math.abs(vx.toDouble() / rX.toDouble())
                    val scaleY = Math.abs(vy.toDouble() / rY.toDouble())
                    val scale = if (scaleX > scaleY) scaleX else scaleY
                    val arrowX = (centerX + vx.toDouble() / scale).toFloat()
                    val arrowY = (centerY + vy.toDouble() / scale).toFloat()
                    drawOffscreenArrow(marker.color, arrowX, arrowY, angle, density)
                }
            }
        }
    }
}

/** 천체 종류별 마커 그리기 - DrawScope 확장 함수 */
private fun DrawScope.drawMarker(
    marker: CelestialMarker,
    x: Float,
    y: Float,
    labelPaint: Paint,
    density: Density
) {
    // 아이콘 크기 1.5배 상향 (16f -> 24f)
    val sizePx = with(density) { 24f * density.density }

    when (marker.name) {
        "SUN" -> {
            drawCircle(color = marker.color, radius = sizePx / 2f, center = Offset(x, y))
            drawCircle(color = marker.color.copy(alpha = 0.3f), radius = sizePx * 0.8f, center = Offset(x, y))
        }
        "MOON" -> drawMoonIcon(x, y, marker.phase ?: 0.5, marker.color, density)
        "POLARIS" -> drawPolarisIcon(x, y, sizePx / 2f)
        "MILKY WAY" -> drawMilkyWaySparkle(x, y, sizePx * 3f, density)
    }
    val labelStrokePx = with(density) { 1f * density.density }
    // 텍스트를 아이콘 하단에 배치 (밀키웨이는 스파클 범위 때문에 더 밑으로 이동)
    val labelYOffset = if (marker.name == "MILKY WAY") sizePx * 5f else sizePx + labelStrokePx * 4f
    drawContext.canvas.nativeCanvas.drawText(marker.name, x, y + labelYOffset, labelPaint)
}

/** 원본 APK: 달 위상 아이콘 */
private fun DrawScope.drawMoonIcon(x: Float, y: Float, phase: Double, color: Color, density: Density) {
    val radius = with(density) { 8f * density.density }
    // 배경 원 (알파 0.8) - 스카이 페이지와 동일
    drawCircle(color.copy(alpha = 0.8f), radius, Offset(x, y))

    val path = Path()
    // cos 값: radius * cos(2π * phase)
    val cos = radius * Math.cos(2.0 * phase * Math.PI).toFloat()

    if (phase < 0.5) {
        // 초승달 ~ 반달 (오른쪽이 빛남)
        path.moveTo(x, y - radius)
        for (i in -90..90) {
            val rad = (i * Math.PI) / 180.0
            path.lineTo(x + (Math.cos(rad).toFloat() * radius), y + (Math.sin(rad).toFloat() * radius))
        }
        for (i in 90 downTo -90) {
            val rad = (i * Math.PI) / 180.0
            path.lineTo(x + (Math.cos(rad).toFloat() * cos), y + (Math.sin(rad).toFloat() * radius))
        }
    } else {
        // 반달 ~ 그믐달 (왼쪽이 빛남)
        path.moveTo(x, y - radius)
        for (i in 90..270) {
            val rad = (i * Math.PI) / 180.0
            path.lineTo(x + (Math.cos(rad).toFloat() * radius), y + (Math.sin(rad).toFloat() * radius))
        }
        for (i in 270 downTo 90) {
            val rad = (i * Math.PI) / 180.0
            path.lineTo(x + (Math.cos(rad).toFloat() * cos), y + (Math.sin(rad).toFloat() * radius))
        }
    }
    path.close()
    // 위상이 빈 부분(어두운 부분)을 검정색으로 칠함
    drawPath(path, Color(0xFF000000L))
}

/** 원본 APK: 북극성 아이콘 */
private fun DrawScope.drawPolarisIcon(x: Float, y: Float, size: Float) {
    val color = Color.White
    val stroke = size * 0.08f
    drawCircle(color = color, radius = size * 0.8f, center = Offset(x, y), style = Stroke(stroke * 0.5f))
    val p = Path()
    p.moveTo(x, y - size); p.lineTo(x + size * 0.2f, y); p.lineTo(x, y + size); p.lineTo(x - size * 0.2f, y); p.close()
    p.moveTo(x - size, y); p.lineTo(x, y - size * 0.2f); p.lineTo(x + size, y); p.lineTo(x, y + size * 0.2f); p.close()
    drawPath(path = p, color = color)
    drawCircle(color = color, radius = size * 0.15f, center = Offset(x, y))
    // 45도 회전 십자 (Canvas save/restore)
    val nativeCanvas = drawContext.canvas.nativeCanvas
    nativeCanvas.save()
    nativeCanvas.rotate(45f, x, y)
    val p2 = Path()
    val ss = size * 0.65f; val sb = ss * 0.2f
    p2.moveTo(x, y - ss); p2.lineTo(x + sb, y); p2.lineTo(x, y + ss); p2.lineTo(x - sb, y); p2.close()
    p2.moveTo(x - ss, y); p2.lineTo(x, y - sb); p2.lineTo(x + ss, y); p2.lineTo(x, y + sb); p2.close()
    drawPath(path = p2, color = color)
    nativeCanvas.restore()
}

/** 원본 APK: 은하수 스파클 효과 */
private fun DrawScope.drawMilkyWaySparkle(x: Float, y: Float, size: Float, density: Density) {
    val colors = listOf(Color.White, Color(0xFFD9EFFF), Color(0xFFF0F8FF))
    val random = Random(1234L)
    val strokePx = with(density) { 1f * density.density }
    repeat(35) {
        val angleDeg = (it * 4.0f - 160.0f) + (random.nextFloat() * 2.0f)
        val angleRad = Math.toRadians(angleDeg.toDouble())
        val radius = size * (random.nextFloat() * 0.4f + 0.8f)
        val ox = x + Math.cos(angleRad).toFloat() * radius
        val oy = y + Math.sin(angleRad).toFloat() * radius + (0.5f * size)
        val s = 0.05f * size + random.nextFloat() * size * 0.15f
        val alpha = random.nextFloat() * 0.6f + 0.4f
        val color = colors[random.nextInt(colors.size)].copy(alpha = alpha)
        drawLine(color = color, start = Offset(ox - s, oy), end = Offset(ox + s, oy), strokeWidth = strokePx)
        drawLine(color = color, start = Offset(ox, oy - s), end = Offset(ox, oy + s), strokeWidth = strokePx)
        drawCircle(color = color.copy(alpha = alpha * 0.8f), radius = s * 0.3f, center = Offset(ox, oy))
    }
}

/** 화면 밖 천체를 가리키는 화살표 그리기 */
private fun DrawScope.drawOffscreenArrow(color: Color, x: Float, y: Float, angle: Float, density: Density) {
    val sizePx = with(density) { 12f * density.density }
    val nativeCanvas = drawContext.canvas.nativeCanvas
    nativeCanvas.save()
    nativeCanvas.translate(x, y)
    nativeCanvas.rotate(Math.toDegrees(angle.toDouble()).toFloat())

    val path = Path()
    path.moveTo(sizePx, 0f)
    path.lineTo(-sizePx, -sizePx)
    path.lineTo(-sizePx / 2f, 0f)
    path.lineTo(-sizePx, sizePx)
    path.close()

    drawPath(path = path, color = color.copy(alpha = 0.8f))
    nativeCanvas.restore()
}
