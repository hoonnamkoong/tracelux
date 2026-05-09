package com.tracelux.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracelux.models.HourlyWeather
import com.tracelux.ui.theme.*
import com.tracelux.ui.utils.WeatherFormatter
import com.tracelux.utils.SolarCalculator
import com.tracelux.utils.WeatherUtils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LocationHeader(locationName: String, onRefreshLocation: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, contentDescription = "Search", tint = TextDim)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = locationName,
            color = TextWhite,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Default.LocationOn,
            contentDescription = "My Location",
            tint = Accent,
            modifier = Modifier.clickable { onRefreshLocation() }
        )
    }
}

@Composable
fun ApiSourceToggle(currentSource: String, onSourceSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardBg)
            .padding(4.dp)
    ) {
        listOf("Open-Meteo", "KMA").forEach { source ->
            val isActive = currentSource == source
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isActive) Accent else Color.Transparent)
                    .clickable { onSourceSelected(source) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = source,
                    color = if (isActive) DarkBg else TextDim,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun WeatherCard(weather: HourlyWeather?, isImperial: Boolean) {
    val temp = WeatherFormatter.getTemp(isImperial, weather?.temp)
    val wind = WeatherFormatter.getWind(isImperial, weather?.windSpeed)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${temp.first}${temp.second}",
            color = TextWhite,
            fontSize = 56.sp,
            fontWeight = FontWeight.Light
        )
        Text(
            text = WeatherUtils.getWeatherDesc(weather?.weatherCode ?: 0),
            color = TextDim,
            fontSize = 16.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            WeatherDetailItem("Wind", "${wind.first} ${wind.second} ${WeatherUtils.getWindDirectionText(weather?.windDirection, weather?.windSpeed)}")
            WeatherDetailItem("Humidity", "${weather?.humidity?.toInt() ?: "--"} %")
            WeatherDetailItem("UV", "${weather?.uvIndex?.toInt() ?: "--"}")
        }

        if (weather?.source == "KMA" && (weather.lgt ?: 0) > 0) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "⚡ Lightning Activity Detected",
                color = Color.Yellow,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SolarLunarGrid(weather: HourlyWeather?, lat: Double, lon: Double, isImperial: Boolean) {
    val date = remember { Date() }
    val sunTimes = remember(lat, lon) { SolarCalculator.getDetailedSunTimes(date, lat, lon) }
    val moonData = remember(lat, lon) { SolarCalculator.getMoonIllumination(date, lat, lon) }
    
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val sunriseStr = sunTimes["sunrise"]?.let { timeFormat.format(it) } ?: "--:--"
    val sunsetStr = sunTimes["sunset"]?.let { timeFormat.format(it) } ?: "--:--"
    
    val wave = WeatherFormatter.getWave(isImperial, weather?.wav)
    val visibility = WeatherFormatter.getDist(isImperial, weather?.visibility?.toDouble())
    
    val cloudBaseValue = if (weather?.temp != null && weather.humidity != null) {
        WeatherUtils.calculateCloudBase(weather.temp, weather.humidity)
    } else null
    val cloudBase = WeatherFormatter.getAlt(isImperial, cloudBaseValue)

    val moonPhase = when {
        moonData.fraction < 0.1 -> "New Moon"
        moonData.fraction < 0.4 -> "Waxing"
        moonData.fraction < 0.6 -> "Full Moon"
        moonData.fraction < 0.9 -> "Waning"
        else -> "New Moon"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).background(CardBg).padding(12.dp)) {
            WeatherDetailItem("Sunrise", sunriseStr)
        }
        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).background(CardBg).padding(12.dp)) {
            WeatherDetailItem("Sunset", sunsetStr)
        }
        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).background(CardBg).padding(12.dp)) {
            WeatherDetailItem("Wave", "${wave.first}${wave.second}")
        }
    }
    
    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).background(CardBg).padding(12.dp)) {
            WeatherDetailItem("Visibility", "${visibility.first}${visibility.second}")
        }
        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).background(CardBg).padding(12.dp)) {
            WeatherDetailItem("Moon", moonPhase)
        }
        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).background(CardBg).padding(12.dp)) {
            WeatherDetailItem("Cloud Base", "${cloudBase.first}${cloudBase.second}")
        }
    }
}

@Composable
fun WeatherDetailItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = TextDim, fontSize = 12.sp)
        Text(text = value, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HourlyForecastList(weatherList: List<HourlyWeather>, isImperial: Boolean) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(weatherList) { item ->
            val time = WeatherUtils.parseWeatherDate(item.time)
            val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(time)
            val temp = WeatherFormatter.getTemp(isImperial, item.temp)
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceDark)
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Text(text = timeStr, color = TextDim, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "${temp.first}°", color = TextWhite, fontWeight = FontWeight.Bold)
            }
        }
    }
}
