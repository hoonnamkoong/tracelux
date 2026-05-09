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
fun SkySearchBar(onClick: () -> Unit) {
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
        Text("주소 또는 장소 검색", color = Color.Gray, fontSize = 14.sp)
    }
}

@Composable
fun SkyConditionsHeader(apiSource: String, onSourceChange: (String) -> Unit, weatherDesc: String) {
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
                SourceButton("기상청", apiSource == "KMA", Modifier.weight(1f)) { onSourceChange("KMA") }
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
    temp: String, humidity: String, windDir: String,
    pty: String, pop: String, lgt: String,
    wav: String, windSpeed: String, cloudBase: String
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            WeatherTile("기온", temp, "℃", Modifier.weight(1f))
            Spacer(modifier = Modifier.width(10.dp))
            WeatherTile("습도", humidity, "%", Modifier.weight(1f))
            Spacer(modifier = Modifier.width(10.dp))
            WeatherTile("풍향", windDir, "", Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            WeatherTile("강수 형태", pty, "", Modifier.weight(1f))
            Spacer(modifier = Modifier.width(10.dp))
            WeatherTile("강수 확률", pop, "%", Modifier.weight(1f))
            Spacer(modifier = Modifier.width(10.dp))
            WeatherTile("낙뢰", lgt, "", Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            WeatherTile("파고", wav, "M", Modifier.weight(1f))
            Spacer(modifier = Modifier.width(10.dp))
            WeatherTile("풍속", windSpeed, "m/s", Modifier.weight(1f))
            Spacer(modifier = Modifier.width(10.dp))
            WeatherTile("운저고도 계산", cloudBase, "km", Modifier.weight(1f))
        }
    }
}

@Composable
fun OpenMeteoWeatherGrid(
    temp: String, humidity: String, dewPoint: String,
    pop: String, windDir: String, wavePeriod: String,
    cloudCover: String, cloudBase: String, visibility: String
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            WeatherTile("기온", temp, "℃", Modifier.weight(1f))
            Spacer(modifier = Modifier.width(10.dp))
            WeatherTile("습도", humidity, "%", Modifier.weight(1f))
            Spacer(modifier = Modifier.width(10.dp))
            WeatherTile("이슬점", dewPoint, "℃", Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            WeatherTile("강수 확률", pop, "%", Modifier.weight(1f))
            Spacer(modifier = Modifier.width(10.dp))
            WeatherTile("풍향", windDir, "", Modifier.weight(1f))
            Spacer(modifier = Modifier.width(10.dp))
            WeatherTile("파도 주기", wavePeriod, "", Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            WeatherTile("구름양", cloudCover, "%", Modifier.weight(1f))
            Spacer(modifier = Modifier.width(10.dp))
            WeatherTile("운저고도 계산", cloudBase, "km", Modifier.weight(1f))
            Spacer(modifier = Modifier.width(10.dp))
            WeatherTile("가시거리", visibility, "km", Modifier.weight(1f))
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
