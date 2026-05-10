package com.tracelux.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracelux.ui.theme.*
import com.tracelux.models.AppUnit

@Composable
fun SkyHeader(locationName: String, onGpsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = locationName,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Button(
            onClick = onGpsClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Text("GPS", color = Orange, fontSize = 12.sp)
        }
    }
}

@Composable
fun SkySearchBar(isKo: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(50.dp)
            .background(Color(0xFF151E32), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(if (isKo) "주소 또는 장소 검색" else "Search Address or Place", color = Color.Gray, fontSize = 14.sp)
    }
}

@Composable
fun SkyConditionsHeader(isKo: Boolean, apiSource: String, onSourceChange: (String) -> Unit, weatherDesc: String) {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("SKY CONDITIONS", color = Color.Gray, fontSize = 12.sp)
            Row(
                modifier = Modifier
                    .width(180.dp)
                    .background(Color(0xFF1E293B), RoundedCornerShape(20.dp))
                    .padding(2.dp)
            ) {
                SourceButton(if (isKo) "기상청" else "KMA", apiSource == "KMA", Modifier.weight(1f)) { onSourceChange("KMA") }
                SourceButton("OPEN METEO", apiSource == "Open-Meteo", Modifier.weight(1f)) { onSourceChange("Open-Meteo") }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("WEATHER STATUS", color = Color.Gray, fontSize = 10.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(weatherDesc.uppercase(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SourceButton(label: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) Orange else Color.Transparent,
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                color = if (isSelected) Color.Black else Color.Gray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun WeatherGrid(
    isKo: Boolean, appUnit: AppUnit,
    temp: String, humidity: String, windDir: String,
    pty: String, pop: String, lgt: String,
    wav: String, windSpeed: String, cloudBase: String
) {
    val tUnit = if (appUnit == AppUnit.METRIC) "℃" else "℉"
    val sUnit = if (appUnit == AppUnit.METRIC) "m/s" else "mph"
    val dUnit = if (appUnit == AppUnit.METRIC) "km" else "mi"
    val wUnit = if (appUnit == AppUnit.METRIC) "M" else "ft"

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            WeatherTile(if (isKo) "기온" else "Temp", temp, tUnit, Modifier.weight(1f))
            Spacer(modifier = Modifier.width(10.dp))
            WeatherTile(if (isKo) "습도" else "Humidity", humidity, "%", Modifier.weight(1f))
            Spacer(modifier = Modifier.width(10.dp))
            WeatherTile(if (isKo) "풍향" else "Wind Dir", windDir, "", Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            WeatherTile(if (isKo) "강수 형태" else "Precip Type", pty, "", Modifier.weight(1f))
            Spacer(modifier = Modifier.width(10.dp))
            WeatherTile(if (isKo) "강수 확률" else "PoP", pop, "%", Modifier.weight(1f))
            Spacer(modifier = Modifier.width(10.dp))
            WeatherTile(if (isKo) "낙뢰" else "Lightning", lgt, "", Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            WeatherTile(if (isKo) "파고" else "Wave", wav, wUnit, Modifier.weight(1f))
            Spacer(modifier = Modifier.width(10.dp))
            WeatherTile(if (isKo) "풍속" else "Wind Spd", windSpeed, sUnit, Modifier.weight(1f))
            Spacer(modifier = Modifier.width(10.dp))
            WeatherTile(if (isKo) "운저고도 계산" else "Cloud Base", cloudBase, dUnit, Modifier.weight(1f))
        }
    }
}

@Composable
fun OpenMeteoWeatherGrid(
    isKo: Boolean, appUnit: AppUnit,
    temp: String, humidity: String, dewPoint: String,
    pop: String, windDir: String, wavePeriod: String,
    cloudCover: String, cloudBase: String, visibility: String
) {
    val tUnit = if (appUnit == AppUnit.METRIC) "℃" else "℉"
    val dUnit = if (appUnit == AppUnit.METRIC) "km" else "mi"

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        // Row 1: 기온, 습도, 풍향
        Row(modifier = Modifier.fillMaxWidth()) {
            WeatherTile(if (isKo) "기온" else "Temp", temp, tUnit, Modifier.weight(1f))
            Spacer(modifier = Modifier.width(10.dp))
            WeatherTile(if (isKo) "습도" else "Humidity", humidity, "%", Modifier.weight(1f))
            Spacer(modifier = Modifier.width(10.dp))
            WeatherTile(if (isKo) "풍향" else "Wind Dir", windDir, "", Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(10.dp))
        // Row 2: 이슬점, 강수 확률, 구름양
        Row(modifier = Modifier.fillMaxWidth()) {
            WeatherTile(if (isKo) "이슬점" else "Dew Point", dewPoint, tUnit, Modifier.weight(1f))
            Spacer(modifier = Modifier.width(10.dp))
            WeatherTile(if (isKo) "강수 확률" else "PoP", pop, "%", Modifier.weight(1f))
            Spacer(modifier = Modifier.width(10.dp))
            WeatherTile(if (isKo) "구름양" else "Cloud Cover", cloudCover, "%", Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(10.dp))
        // Row 3: 가시거리, 파도 주기, 운저고도 계산
        Row(modifier = Modifier.fillMaxWidth()) {
            WeatherTile(if (isKo) "가시거리" else "Visibility", visibility, dUnit, Modifier.weight(1f))
            Spacer(modifier = Modifier.width(10.dp))
            WeatherTile(if (isKo) "파도 주기" else "Wave Period", wavePeriod, "", Modifier.weight(1f))
            Spacer(modifier = Modifier.width(10.dp))
            WeatherTile(if (isKo) "운저고도 계산" else "Cloud Base", cloudBase, dUnit, Modifier.weight(1f))
        }
    }
}

@Composable
fun WeatherTile(label: String, value: String, unit: String, modifier: Modifier) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(label, color = Color.Gray, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            if (unit.isNotEmpty()) {
                Text(unit, color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(bottom = 2.dp, start = 2.dp))
            }
        }
    }
}

@Composable
fun ForecastTimeSlider(
    timeIndex: Int,
    maxIndex: Int,
    timeText: String,
    onIndexChange: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("FORECAST TIME", color = Color.Gray, fontSize = 10.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "-", 
                    color = Orange, 
                    fontSize = 20.sp,
                    modifier = Modifier
                        .clickable { if(timeIndex > 0) onIndexChange(timeIndex - 1) }
                        .padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.width(10.dp)) // 버튼 간격 조절로 '-'를 왼쪽으로 이동
                Text(
                    text = "+", 
                    color = Orange, 
                    fontSize = 20.sp,
                    modifier = Modifier
                        .clickable { if(timeIndex < maxIndex) onIndexChange(timeIndex + 1) }
                        .padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.width(15.dp))
                Text(timeText, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        // thumb dot 없는 커스텀 슬라이더
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .pointerInput(maxIndex) {
                    detectHorizontalDragGestures { change, _ ->
                        change.consume()
                        val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                        onIndexChange((fraction * maxIndex).toInt())
                    }
                }
        ) {
            val fraction = if (maxIndex > 0) timeIndex.toFloat() / maxIndex else 0f
            // 배경 트랙 (더 얇고 어둡게)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.CenterStart)
                    .background(Color(0xFF0F172A), RoundedCornerShape(1.dp))
            )
            
            // 썸 (Thumb) - 오렌지색 원형
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .offset(x = (maxWidth - 14.dp) * fraction)
                    .clip(CircleShape)
                    .background(Orange)
                    .align(Alignment.CenterStart)
            )
        }
    }
}

@Composable
fun SunMoonPathCard(title: String, riseTime: String, setTime: String, arcCanvas: @Composable () -> Unit, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .background(Color(0xFF1E293B), RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Text(title, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        arcCanvas()
        content()
    }
}
