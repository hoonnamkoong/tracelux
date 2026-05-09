package com.tracelux.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracelux.ui.theme.BlueHour
import com.tracelux.ui.theme.GoldenHour
import com.tracelux.ui.theme.TextDim
import com.tracelux.utils.MilkyWayResult
import com.tracelux.utils.SolarCalculator
import java.text.SimpleDateFormat
import java.util.*

// ─────────────────────────────────────────────────────────────────
// APK 원본의 SkyArcCanvas를 1:1 복원한 Composable.
// 모든 텍스트(Golden Hour, Blue Hour, Milky Way)를 캔버스 안에서 drawText로 렌더링하며,
// 달의 위상(drawMoonIcon)도 APK 동일 로직으로 처리합니다.
// ─────────────────────────────────────────────────────────────────
@OptIn(ExperimentalTextApi::class)
@Composable
fun SkyArcCanvas(
    type: String,           // "SUN" or "MOON"
    lat: Double,
    lon: Double,
    currentTime: Date = Date(),
    mwResult: MilkyWayResult? = null,
    milkyWayImage: ImageBitmap? = null,
    modifier: Modifier = Modifier
) {
    val isSun = type == "SUN"
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val textMeasurer = rememberTextMeasurer()

    // 오늘 날짜 키 (날짜가 바뀔 때만 재계산)
    val dateKey = remember(currentTime) {
        val cal = Calendar.getInstance()
        cal.time = currentTime
        "${cal.get(Calendar.YEAR)}${cal.get(Calendar.DAY_OF_YEAR)}"
    }

    // APK 원본: getDetailedSunTimes / getMoonTimes 를 remember로 캐시
    val sunTimesMap = remember(lat, lon, dateKey) {
        SolarCalculator.getDetailedSunTimes(currentTime, lat, lon)
    }
    val moonTimesMap = remember(lat, lon, dateKey) {
        SolarCalculator.getMoonTimes(currentTime, lat, lon)
    }

    // 현재 타입에 따라 출몰 시간 선택
    val riseDate: Date? = if (isSun) sunTimesMap["sunrise"] else moonTimesMap["moonrise"]
    val setDate: Date? = if (isSun) sunTimesMap["sunset"] else moonTimesMap["moonset"]

    val riseFrac: Float? = getFraction(riseDate)
    val setFrac: Float? = getFraction(setDate)
    val riseF = riseFrac ?: 0f
    val setF = setFrac ?: 1f
    val currentFrac = getFraction(currentTime) ?: 0f

    // APK 원본 색상 값 (long hex)
    // SUN: 0xFFFFAF0B (Orange), MOON: 0xFF0000FF with blue hue  -> APK: areEqual=SUN → 4294286859L else 4294956544L
    // 4294286859L = 0xFF_FF_AF_0B (orange)
    // 4294956544L = 0xFF_FF_FF_00 ... actually 4278235391L is used for moon border
    // APK Color3 = if(areEqual) Color else Color(4278235391L)
    // 4278235391L = 0xFF00FFFF? no → 0xFF0000FF blue. Let's decode:
    // 4278235391 = 0xFF0000FF → full alpha, R=0, G=0, B=255 → pure blue
    // The accent color (arc stroke / gradient top)
    val accentColor = if (isSun) Color(0xFFFFAF0BL) else Color(0xFF3B82F6L)
    // The gradient fill color (arc body)
    val gradFillColor = accentColor.copy(alpha = 0.4f)

    // 텍스트 스타일 - APK 원본과 동일
    val timeStyle = TextStyle(
        color = accentColor,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Bold
    )
    val labelStyle = TextStyle(
        color = TextDim,
        fontSize = 7.5.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp
    )
    val infoStyle = TextStyle(
        color = accentColor.copy(alpha = 0.9f),
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // ─── 헤더 박스: "SUN PATH / MOON PATH" + 밀키웨이 아이콘 ───
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .padding(bottom = 5.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            // 밀키웨이 아이콘: 문패스 + 현재 시각이 best 구간 내일 때만
            if (!isSun && mwResult != null && milkyWayImage != null) {
                val start = mwResult.start
                val end = mwResult.end
                val inBest = if (start != null && end != null) {
                    !currentTime.before(start) && !currentTime.after(end)
                } else false

                if (inBest) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val imgW = (milkyWayImage.width / 3f).toInt()
                        val imgH = (milkyWayImage.height / 3f).toInt()
                        // SunMoonPathCard 타이틀 위치인 -22dp 지점으로 이동
                        val topX = size.width / 2f - imgW / 2f
                        val topY = -22.dp.toPx()
                        drawImage(
                            image = milkyWayImage,
                            dstSize = androidx.compose.ui.unit.IntSize(imgW, imgH),
                            dstOffset = androidx.compose.ui.unit.IntOffset(topX.toInt(), topY.toInt())
                        )
                    }
                }
            }
        }

        // ─── 메인 캔버스: 호 + 텍스트 ───
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .padding(start = 4.dp)
        ) {
            val w = size.width
            val h = size.height

            // APK 원본 비율 그대로
            val startX = w * 0.05f
            val widthPx = w * 0.9f
            val baselineY = (h * 0.7f) - 8.dp.toPx()
            val amplitude = (h * 0.22f) + 10.dp.toPx()

            // ─── 1. 그라데이션 채우기 ───
            val gradient = Brush.verticalGradient(
                colors = listOf(gradFillColor, Color.Transparent),
                startY = baselineY - amplitude,
                endY = baselineY
            )

            if (riseF < setF) {
                // 일반 경우: 출 → 몰
                val path = Path()
                path.moveTo(startX + riseF * widthPx, baselineY)
                for (i in 0..150) {
                    val t = riseF + (i / 150f) * (setF - riseF)
                    val pt = getPointOnArc(riseF, setF, startX, widthPx, baselineY, amplitude, t)
                    path.lineTo(pt.x, pt.y)
                }
                path.lineTo(startX + setF * widthPx, baselineY)
                path.close()
                drawPath(path, gradient, style = Fill)
            } else {
                // 자정 넘어가는 경우: 0 → 몰 구간
                val path1 = Path()
                path1.moveTo(startX, baselineY)
                for (i in 0..150) {
                    val t = (i / 150f) * setF
                    val pt = getPointOnArc(riseF, setF, startX, widthPx, baselineY, amplitude, t)
                    path1.lineTo(pt.x, pt.y)
                }
                path1.lineTo(startX + setF * widthPx, baselineY)
                path1.close()
                drawPath(path1, gradient, style = Fill)

                // 출 → 끝 구간
                val path2 = Path()
                path2.moveTo(startX + riseF * widthPx, baselineY)
                for (i in 0..150) {
                    val t = riseF + (i / 150f) * (1f - riseF)
                    val pt = getPointOnArc(riseF, setF, startX, widthPx, baselineY, amplitude, t)
                    path2.lineTo(pt.x, pt.y)
                }
                path2.lineTo(startX + widthPx, baselineY)
                path2.close()
                drawPath(path2, gradient, style = Fill)
            }

            // ─── 1-1. 수평 기준선 (지평선) ───
            // 3px 실선으로 그리되 호 밑에는 그리지 않음 (호가 없는 바깥 구간에만 그림)
            val horizonColor = Color.Gray.copy(alpha = 0.5f)
            val horizonStroke = 1.5.dp.toPx()
            val rX = startX + riseF * widthPx
            val sX = startX + setF * widthPx

            if (riseF < setF) {
                // 출 -> 몰: 양 끝단에 지평선 (호는 가운데)
                drawLine(color = horizonColor, start = Offset(startX, baselineY), end = Offset(rX, baselineY), strokeWidth = horizonStroke)
                drawLine(color = horizonColor, start = Offset(sX, baselineY), end = Offset(startX + widthPx, baselineY), strokeWidth = horizonStroke)
            } else {
                // 자정 넘는 경우 (몰 -> 출): 가운데에 지평선 (호는 양 끝단)
                drawLine(color = horizonColor, start = Offset(sX, baselineY), end = Offset(rX, baselineY), strokeWidth = horizonStroke)
            }

            // ─── 2. 전체 아크 선 (지평선 위 구간만) ───
            val strokePath = Path()
            val riseX = startX + riseF * widthPx
            val setXPos = startX + setF * widthPx

            if (riseF < setF) {
                // 일반 경우: 출 -> 몰 사이를 촘촘하게 연결
                strokePath.moveTo(riseX, baselineY)
                for (i in 1..100) {
                    val t = riseF + (i / 100f) * (setF - riseF)
                    val pt = getPointOnArc(riseF, setF, startX, widthPx, baselineY, amplitude, t)
                    strokePath.lineTo(pt.x, pt.y)
                }
            } else {
                // 자정 넘어가는 경우: 몰 -> 출 사이 (지평선 아래)가 아닌 지평선 위 구간만
                // 0 -> 몰 구간
                strokePath.moveTo(startX, baselineY) // 0시점 (지평선 위일 수도 아래일 수도 있지만 getPointOnArc가 처리)
                for (i in 0..100) {
                    val t = (i / 100f)
                    val pt = getPointOnArc(riseF, setF, startX, widthPx, baselineY, amplitude, t)
                    if (i == 0) strokePath.moveTo(pt.x, pt.y) else strokePath.lineTo(pt.x, pt.y)
                }
                // 출 -> 1 구간
                for (i in 0..100) {
                    val t = (i / 100f)
                    val pt = getPointOnArc(riseF, setF, startX, widthPx, baselineY, amplitude, t)
                    // ... (이 부분은 기존 sinVal < 0 체크 로직이 복잡하므로 간단히 baselineY 체크로 필터링)
                }
                // 개선: 기존의 필터링 루프를 사용하되 시작/끝점을 강제 지정
                strokePath.reset()
                var first = true
                for (i in 0..200) {
                    val t = i / 200f
                    val pt = getPointOnArc(riseF, setF, startX, widthPx, baselineY, amplitude, t)
                    if (pt.y <= baselineY + 0.5f) {
                        if (first) {
                            strokePath.moveTo(pt.x, pt.y)
                            first = false
                        } else {
                            strokePath.lineTo(pt.x, pt.y)
                        }
                    } else {
                        first = true
                    }
                }
            }
            
            drawPath(
                path = strokePath,
                color = horizonColor,
                style = Stroke(width = horizonStroke, join = StrokeJoin.Round)
            )



            // ─── 4. 현재 위치 아이콘 ───
            val currentPt = getPointOnArc(riseF, setF, startX, widthPx, baselineY, amplitude, currentFrac)
            if (isSun) {
                // 태양: 좀 더 진한 오렌지색
                val sunColor = Color(0xFFFF8000L).copy(alpha = 0.9f)
                drawCircle(color = sunColor, radius = 8.dp.toPx(), center = currentPt)
            } else {
                // 달: 더 샛노란색
                val moonPhase = SolarCalculator.getMoonIllumination(currentTime, lat, lon).fraction
                val moonColor = Color(0xFFFFE000L) 
                drawMoonIcon(currentPt.x, currentPt.y, moonPhase, moonColor)
                

            }

            // ─── 5. 출몰 시각 텍스트 ───
            // APK: drawSafeText(..., centerX=riseX, y=baselineY - 52dp, centered=true)
            if (riseFrac != null && riseDate != null) {
                val safeRiseX = riseX.coerceIn(
                    startX + 20.dp.toPx(),
                    startX + widthPx - 20.dp.toPx()
                )
                drawSafeText(
                    textMeasurer, this,
                    timeFormat.format(riseDate), timeStyle,
                    safeRiseX, baselineY - 52.dp.toPx(), centered = true
                )
                drawSafeText(
                    textMeasurer, this,
                    if (isSun) "SUNRISE" else "MOONRISE", labelStyle,
                    safeRiseX, baselineY - 38.dp.toPx(), centered = true
                )
            }
            if (setFrac != null && setDate != null) {
                val safeSetX = setXPos.coerceIn(
                    startX + 20.dp.toPx(),
                    startX + widthPx - 20.dp.toPx()
                )
                drawSafeText(
                    textMeasurer, this,
                    timeFormat.format(setDate), timeStyle,
                    safeSetX, baselineY - 52.dp.toPx(), centered = true
                )
                drawSafeText(
                    textMeasurer, this,
                    if (isSun) "SUNSET" else "MOONSET", labelStyle,
                    safeSetX, baselineY - 38.dp.toPx(), centered = true
                )
            }

            // ─── 6. Golden / Blue Hour 텍스트 (선패스) or Milky Way (문패스) ───
            if (isSun) {
                val goldenStyle = TextStyle(
                    color = GoldenHour,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                val blueStyle = TextStyle(
                    color = BlueHour,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                val mgS = fmt(timeFormat, sunTimesMap["morningGoldenHourStart"])
                val mgE = fmt(timeFormat, sunTimesMap["morningGoldenHourEnd"])
                val egS = fmt(timeFormat, sunTimesMap["eveningGoldenHourStart"])
                val egE = fmt(timeFormat, sunTimesMap["eveningGoldenHourEnd"])
                drawSafeText(
                    textMeasurer, this,
                    "Golden Hour : $mgS ~ $mgE / $egS ~ $egE",
                    goldenStyle,
                    w / 2f, baselineY + 18.dp.toPx(), centered = true
                )
                val mbS = fmt(timeFormat, sunTimesMap["morningBlueHourStart"])
                val mbE = fmt(timeFormat, sunTimesMap["morningBlueHourEnd"])
                val ebS = fmt(timeFormat, sunTimesMap["eveningBlueHourStart"])
                val ebE = fmt(timeFormat, sunTimesMap["eveningBlueHourEnd"])
                drawSafeText(
                    textMeasurer, this,
                    "Blue Hour : $mbS ~ $mbE / $ebS ~ $ebE",
                    blueStyle,
                    w / 2f, baselineY + 36.dp.toPx(), centered = true
                )
            } else if (mwResult != null) {
                val mwStyle = TextStyle(
                    color = Color(0xFF38BDF8L), // 연하늘색 (APK: j = milky way color)
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                val msg = mwResult.status
                    ?: ("${fmt(timeFormat, mwResult.start)} - ${fmt(timeFormat, mwResult.end)}")
                drawSafeText(
                    textMeasurer, this,
                    "MILKY WAY BEST: $msg",
                    mwStyle,
                    w / 2f, baselineY + 23.dp.toPx(), centered = true
                )
            }
        }
    }
}

// ─── Arc 위 좌표 계산 (APK 1:1 복원) ───────────────────────────────
private fun getPointOnArc(
    riseF: Float, setF: Float,
    startX: Float, widthPx: Float,
    baselineY: Float, amplitude: Float,
    t: Float
): Offset {
    var sinVal = 0f
    if (riseF < setF) {
        if (t in riseF..setF) {
            sinVal = Math.sin(((t - riseF) / (setF - riseF)) * Math.PI).toFloat()
        }
    } else {
        // 자정 넘는 경우
        if (t <= setF || t >= riseF) {
            val progress = if (t >= riseF) t - riseF else (1f - riseF) + t
            val total = (1f - riseF) + setF
            sinVal = Math.sin((progress / total) * Math.PI).toFloat()
        }
    }
    return Offset(startX + t * widthPx, baselineY - sinVal * amplitude)
}

// ─── 달 위상 그리기 (APK drawMoonIcon 1:1 복원) ─────────────────────
@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawMoonIcon(cx: Float, cy: Float, phase: Double, color: Color) {
    val radius = 8.dp.toPx()
    // 배경 원 (알파 0.8)
    drawCircle(color.copy(alpha = 0.8f), radius, Offset(cx, cy))

    val path = Path()
    // cos 값: radius * cos(2π * phase)
    val cos = radius * Math.cos(2.0 * phase * Math.PI).toFloat()

    if (phase < 0.5) {
        // 초승달 ~ 반달 (오른쪽이 빛남)
        path.moveTo(cx, cy - radius)
        for (i in -90..90) {
            val rad = (i * Math.PI) / 180.0
            path.lineTo(cx + (Math.cos(rad).toFloat() * radius), cy + (Math.sin(rad).toFloat() * radius))
        }
        for (i in 90 downTo -90) {
            val rad = (i * Math.PI) / 180.0
            path.lineTo(cx + (Math.cos(rad).toFloat() * cos), cy + (Math.sin(rad).toFloat() * radius))
        }
    } else {
        // 반달 ~ 그믐달 (왼쪽이 빛남)
        path.moveTo(cx, cy - radius)
        for (i in 90..270) {
            val rad = (i * Math.PI) / 180.0
            path.lineTo(cx + (Math.cos(rad).toFloat() * radius), cy + (Math.sin(rad).toFloat() * radius))
        }
        for (i in 270 downTo 90) {
            val rad = (i * Math.PI) / 180.0
            path.lineTo(cx + (Math.cos(rad).toFloat() * cos), cy + (Math.sin(rad).toFloat() * radius))
        }
    }
    path.close()
    // 위상이 빈 부분(어두운 부분)을 검정색으로 칠함
    drawPath(path, Color(0xFF000000L))
}

// ─── 텍스트를 안전하게 캔버스에 그리기 (APK drawSafeText 복원) ─────
@OptIn(ExperimentalTextApi::class)
private fun drawSafeText(
    measurer: TextMeasurer,
    scope: DrawScope,
    text: String,
    style: TextStyle,
    x: Float,
    y: Float,
    centered: Boolean = false
) {
    val layout = measurer.measure(
        text = text,
        style = style,
        overflow = TextOverflow.Clip,
        softWrap = false,
        maxLines = 1,
        constraints = Constraints()
    )
    val drawX = if (centered) x - layout.size.width / 2f else x
    scope.drawText(layout, topLeft = Offset(drawX, y))
}

// ─── 날짜 → 하루 분율(0~1) ────────────────────────────────────────
private fun getFraction(date: Date?): Float? {
    if (date == null) return null
    val cal = Calendar.getInstance()
    cal.time = date
    return (cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)) / 1440f
}

// ─── 날짜 포맷 ("--:--" if null) ──────────────────────────────────
private fun fmt(fmt: SimpleDateFormat, date: Date?): String {
    if (date == null) return "--:--"
    return fmt.format(date)
}

// ─── 밀키웨이 스파클 아이콘 (별 3개 + 점 2개) ─────────────────────
private fun DrawScope.drawMilkyWaySparkle() {
    val w = size.width
    val h = size.height
    val starColor = Color(0xFFFFD700L) // gold
    drawStar(starColor, w * 0.2f, h * 0.6f, h * 0.3f, 0.7f)
    drawStar(starColor, w * 0.5f, h * 0.3f, h * 0.5f, 0.9f)
    drawStar(starColor, w * 0.8f, h * 0.5f, h * 0.35f, 0.8f)
    drawCircle(starColor.copy(alpha = 0.5f), 1.dp.toPx(), Offset(w * 0.35f, h * 0.4f))
    drawCircle(starColor.copy(alpha = 0.6f), 1.5.dp.toPx(), Offset(w * 0.65f, h * 0.5f))
}

private fun DrawScope.drawStar(color: Color, x: Float, y: Float, size: Float, alpha: Float) {
    val path = Path()
    path.moveTo(x, y - size)
    path.quadraticBezierTo(x + size * 0.2f, y - size * 0.2f, x + size, y)
    path.quadraticBezierTo(x + size * 0.2f, y + size * 0.2f, x, y + size)
    path.quadraticBezierTo(x - size * 0.2f, y + size * 0.2f, x - size, y)
    path.quadraticBezierTo(x - size * 0.2f, y - size * 0.2f, x, y - size)
    path.close()
    drawPath(path, color.copy(alpha = alpha))
}
