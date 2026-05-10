package com.tracelux.ar

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

@Composable
fun ArHorizonLine(azimuth: Float, direction: String, pitch: Float, fovY: Float) {
    val density = LocalDensity.current
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val centerX = w / 2f
        val centerY = h / 2f

        val yOffset = (pitch / (fovY / 2f)) * (h / 2f)
        val horizonY = centerY + yOffset

        if (horizonY > 0f && horizonY < h) {
            drawRect(
                color = Color.White.copy(alpha = 0.15f),
                topLeft = Offset(0f, horizonY - 12.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(w, 24.dp.toPx())
            )
            drawLine(
                color = Color.White.copy(alpha = 0.4f),
                start = Offset(0f, horizonY),
                end = Offset(w, horizonY),
                strokeWidth = 1.dp.toPx()
            )
            val pxPerDeg = w / 50f
            for (i in -180..180 step 10) {
                val deg = ((azimuth.toInt() + i) + 360) % 360
                val x = centerX + i * pxPerDeg
                if (x < 0f || x > w) continue
                val tickH = if (deg % 30 == 0) 10.dp.toPx() else 5.dp.toPx()
                drawLine(
                    color = Color.White.copy(alpha = 0.5f),
                    start = Offset(x, horizonY - tickH),
                    end = Offset(x, horizonY + tickH),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // Azimuth Text fixed on Horizon Line
            drawContext.canvas.nativeCanvas.drawText(
                "${azimuth.toInt()}° $direction",
                centerX,
                horizonY - 8.dp.toPx(),
                Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 20.sp.toPx()
                    textAlign = Paint.Align.CENTER
                    isFakeBoldText = true
                    setShadowLayer(8f, 2f, 2f, android.graphics.Color.BLACK)
                }
            )

            // Downward arrow fixed on Horizon Line
            val path = Path()
            path.moveTo(centerX - 6.dp.toPx(), horizonY)
            path.lineTo(centerX + 6.dp.toPx(), horizonY)
            path.lineTo(centerX, horizonY + 8.dp.toPx())
            path.close()
            drawPath(path = path, color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun ArHorizonCompass(azimuth: Float, direction: String, pitch: Float, fovY: Float) {
    Box(modifier = Modifier.fillMaxSize()) {
        ArHorizonLine(azimuth = azimuth, direction = direction, pitch = pitch, fovY = fovY)
    }
}

@Composable
fun ArTiltScale(pitch: Float) {
    Box(modifier = Modifier.fillMaxHeight().width(80.dp).padding(start = 16.dp)) {
        Text(
            text = "TILT",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopStart).padding(top = 100.dp)
        )
        Canvas(modifier = Modifier.fillMaxHeight(0.6f).width(40.dp).align(Alignment.CenterStart)) {
            val h = size.height
            val w = size.width
            val step = h / 40f // -20 ~ 20 degree range for scale
            for (i in -20..20 step 2) {
                val y = (h / 2f) - (i * step)
                val isMajor = i % 10 == 0
                val lineW = if (isMajor) w * 0.5f else w * 0.25f
                drawLine(
                    color = Color.White.copy(alpha = 0.5f),
                    start = Offset(0f, y),
                    end = Offset(lineW, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
            
            // Pitch text on the RIGHT of the scale lines
            drawContext.canvas.nativeCanvas.drawText(
                "${pitch.toInt()}°",
                w * 0.5f + 4.dp.toPx(),
                h / 2f + 4.dp.toPx(),
                Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 14.sp.toPx()
                    textAlign = Paint.Align.LEFT
                    isFakeBoldText = true
                    setShadowLayer(4f, 2f, 2f, android.graphics.Color.BLACK)
                }
            )
        }
    }
}

@Composable
fun ArTimeSimulationScale(timeOffset: Float, onTimeChange: (Float) -> Unit) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxHeight()
            .width(130.dp)
            .padding(end = 16.dp)
    ) {
        // constraints.maxHeight는 픽셀 단위의 높이입니다.
        // Canvas가 fillMaxHeight(0.6f)이므로 실제 레일의 높이도 0.6배입니다.
        val totalH = constraints.maxHeight.toFloat() * 0.6f
        val dragState = rememberDraggableState { delta ->
            // delta(픽셀) / totalH(픽셀) * 24시간 = 시간 변화량
            // 감도를 2배로 높여 더 빠르게 반응하도록 수정
            val deltaHours = (delta * 2.0f / totalH) * 24f
            val newTime = (timeOffset + deltaHours).coerceIn(0f, 24f)
            onTimeChange(newTime)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .draggable(state = dragState, orientation = Orientation.Vertical)
        ) {
        Text(
            text = "TIME SIMULATION",
            color = Color(0xFFFFC300),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 100.dp)
        )
        Canvas(modifier = Modifier.fillMaxHeight(0.6f).fillMaxWidth().align(Alignment.CenterEnd)) {
            val totalH = size.height
            val pxPerHour = totalH / 24f
            val railX = size.width - 2.dp.toPx()

            drawLine(color = Color.White.copy(alpha = 0.3f), start = Offset(railX, 0f), end = Offset(railX, totalH), strokeWidth = 1.dp.toPx())

            for (h in 0..24 step 3) {
                val y = h * pxPerHour
                val isMajor = h % 6 == 0
                val tickLen = if (isMajor) 12.dp.toPx() else 6.dp.toPx()
                drawLine(
                    color = Color.White.copy(alpha = 0.6f),
                    start = Offset(railX, y),
                    end = Offset(railX - tickLen, y),
                    strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                )
                if (isMajor) {
                    drawContext.canvas.nativeCanvas.drawText(
                        String.format("%02d", h),
                        railX - tickLen - 6.dp.toPx(),
                        y + 4.dp.toPx(),
                        Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 10.sp.toPx()
                            textAlign = Paint.Align.RIGHT
                        }
                    )
                }
            }

            // Arrow pointing at selected time
            val arrowY = totalH * (timeOffset / 24f)
            val path = Path()
            path.moveTo(railX + 6.dp.toPx(), arrowY)
            path.lineTo(railX - 6.dp.toPx(), arrowY - 6.dp.toPx())
            path.lineTo(railX - 6.dp.toPx(), arrowY + 6.dp.toPx())
            path.close()
            drawPath(path = path, color = Color(0xFFFFC300))

            // Fixed Time text in the exact center of the scale
            val timeText = if (timeOffset % 1 == 0f) "${timeOffset.toInt()}" else String.format("%.1f", timeOffset)
            drawContext.canvas.nativeCanvas.drawText(
                timeText,
                railX - 24.dp.toPx(),
                totalH / 2f + 5.dp.toPx(),
                Paint().apply {
                    color = android.graphics.Color.parseColor("#FFC300")
                    textSize = 16.sp.toPx()
                    textAlign = Paint.Align.RIGHT
                    isFakeBoldText = true
                    setShadowLayer(4f, 2f, 2f, android.graphics.Color.BLACK)
                }
            )
        }
    }
}
}

@Composable
fun CustomArIcon(key: String, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val s = size.width
        val h = size.height
        val c = Offset(s / 2f, h / 2f)
        val stroke = Stroke(1.5.dp.toPx())
        
        when(key) {
            "MOON" -> {
                val p = Path()
                p.moveTo(s * 0.5f, h * 0.15f)
                p.arcTo(androidx.compose.ui.geometry.Rect(s * 0.2f, h * 0.15f, s * 0.8f, h * 0.85f), -90f, -180f, false)
                p.arcTo(androidx.compose.ui.geometry.Rect(s * 0.35f, h * 0.15f, s * 0.95f, h * 0.85f), 90f, 180f, false)
                drawPath(p, color)
            }
            "STAR" -> {
                val p = Path()
                for (i in 0..4) {
                    val angle = Math.toRadians((i * 144 - 90).toDouble())
                    val x = c.x + (s * 0.45f) * cos(angle).toFloat()
                    val y = c.y + (h * 0.45f) * sin(angle).toFloat()
                    if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
                }
                p.close()
                drawPath(p, color)
            }
            "SUN" -> {
                drawCircle(color, radius = s * 0.25f, center = c, style = stroke)
                for (i in 0..7) {
                    val angle = Math.toRadians((i * 45).toDouble())
                    val r1 = s * 0.3f
                    val r2 = s * 0.45f
                    drawLine(
                        color = color,
                        start = Offset(c.x + cos(angle).toFloat() * r1, c.y + sin(angle).toFloat() * r1),
                        end = Offset(c.x + cos(angle).toFloat() * r2, c.y + sin(angle).toFloat() * r2),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }
            }
            "MW" -> {
                // Sparkles (4-pointed stars)
                fun drawSparkle(center: Offset, rad: Float) {
                    val p = Path()
                    p.moveTo(center.x, center.y - rad)
                    p.quadraticBezierTo(center.x, center.y, center.x + rad, center.y)
                    p.quadraticBezierTo(center.x, center.y, center.x, center.y + rad)
                    p.quadraticBezierTo(center.x, center.y, center.x - rad, center.y)
                    p.quadraticBezierTo(center.x, center.y, center.x, center.y - rad)
                    p.close()
                    drawPath(p, color)
                }
                drawSparkle(Offset(s * 0.3f, h * 0.3f), s * 0.25f)
                drawSparkle(Offset(s * 0.7f, h * 0.4f), s * 0.15f)
                drawSparkle(Offset(s * 0.5f, h * 0.7f), s * 0.2f)
            }
            "FOV" -> {
                val l = s * 0.25f
                drawLine(color, Offset(0f, 0f), Offset(l, 0f), strokeWidth = 1.5.dp.toPx())
                drawLine(color, Offset(0f, 0f), Offset(0f, l), strokeWidth = 1.5.dp.toPx())
                
                drawLine(color, Offset(s, 0f), Offset(s - l, 0f), strokeWidth = 1.5.dp.toPx())
                drawLine(color, Offset(s, 0f), Offset(s, l), strokeWidth = 1.5.dp.toPx())
                
                drawLine(color, Offset(0f, s), Offset(l, s), strokeWidth = 1.5.dp.toPx())
                drawLine(color, Offset(0f, s), Offset(0f, s - l), strokeWidth = 1.5.dp.toPx())
                
                drawLine(color, Offset(s, s), Offset(s - l, s), strokeWidth = 1.5.dp.toPx())
                drawLine(color, Offset(s, s), Offset(s, s - l), strokeWidth = 1.5.dp.toPx())
            }
            "ROT" -> {
                val p = Path()
                p.moveTo(s * 0.15f, h * 0.5f)
                p.arcTo(androidx.compose.ui.geometry.Rect(s * 0.15f, h * 0.15f, s * 0.85f, h * 0.85f), 135f, 270f, false)
                drawPath(p, color, style = stroke)
                
                val arr = Path()
                arr.moveTo(s * 0.15f, h * 0.5f)
                arr.lineTo(s * 0.05f, h * 0.6f)
                arr.lineTo(s * 0.25f, h * 0.6f)
                arr.close()
                drawPath(arr, color)
            }
        }
    }
}

@Composable
fun ArObjectToggles(
    toggles: Map<String, Boolean>,
    onToggle: (String) -> Unit,
    onRotate: () -> Unit,
    modifier: Modifier = Modifier
) {
    data class ToggleItem(val key: String, val label: String)
    
    val items = listOf(
        ToggleItem("MOON", "MOON"),
        ToggleItem("STAR", "POLARIS"),
        ToggleItem("SUN", "SUN"),
        ToggleItem("MW", "MILKY"),
        ToggleItem("FOV", "FOV")
    )

    Row(
        modifier = modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            val isEnabled = toggles[item.key] ?: false
            Column(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        if (isEnabled) Color.White.copy(alpha = 0.2f) else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .border(1.dp, if (isEnabled) Color.White else Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .clickable { onToggle(item.key) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CustomArIcon(item.key, if (isEnabled) Color.White else Color.White.copy(alpha = 0.5f), Modifier.offset(y = 2.5.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(item.label, color = if (isEnabled) Color.White else Color.White.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        // ROTATE button
        Column(
            modifier = Modifier
                .size(56.dp)
                .background(Color.Transparent, RoundedCornerShape(8.dp))
                .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .clickable { onRotate() },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CustomArIcon("ROT", Color.White.copy(alpha = 0.7f), Modifier.offset(y = 2.5.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text("ROT", color = Color.White.copy(alpha = 0.7f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FovFrame(mm: Int, ratio: Float, isWarning: Boolean = false) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val baseWidth = if (maxWidth > maxHeight) maxWidth else maxHeight
            val boxW = baseWidth * ratio
            val boxH = boxW / 1.5f
            Box(
                modifier = Modifier
                    .size(width = boxW, height = boxH)
                    .border(
                        1.dp,
                        if (isWarning) Color.Red.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.3f),
                        RoundedCornerShape(2.dp)
                    ),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = "${mm}mm",
                    color = if (isWarning) Color.Red else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

fun getDirectionString(azimuth: Float): String {
    val dirs = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW", "N")
    val index = ((azimuth + 22.5) / 45).toInt() % 8
    return dirs[index]
}
