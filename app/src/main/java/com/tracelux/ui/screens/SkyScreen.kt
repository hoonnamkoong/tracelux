package com.tracelux.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tracelux.ui.components.*
import com.tracelux.ui.theme.*
import com.tracelux.utils.WeatherUtils

@Composable
fun SkyScreen(
    mainViewModel: MainViewModel = viewModel(),
    skyViewModel: SkyViewModel = viewModel()
) {
    val mainUiState by mainViewModel.uiState.collectAsState()
    val skyUiState by skyViewModel.uiState.collectAsState()

    // 위치 변경 시 날씨 데이터 자동 갱신
    LaunchedEffect(mainUiState.lat, mainUiState.lon, skyUiState.apiSource) {
        if (mainUiState.lat != 0.0 && mainUiState.lon != 0.0) {
            skyViewModel.fetchWeatherData(
                lat = mainUiState.lat,
                lon = mainUiState.lon,
                source = skyUiState.apiSource,
                kmaKey = WeatherUtils.KMA_AUTH_KEY
            )
        }
    }

    // 초기 위치 요청
    LaunchedEffect(Unit) {
        if (mainUiState.lat == 0.0) {
            mainViewModel.refreshLocation()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 상단 위치 정보 및 검색 바
            LocationHeader(
                locationName = skyUiState.locationName ?: "Seoul, South Korea",
                onRefreshLocation = { mainViewModel.refreshLocation() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // API 소스 선택 및 단위 변환 로직
            ApiSourceToggle(
                currentSource = skyUiState.apiSource,
                onSourceSelected = { skyViewModel.setApiSource(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (skyUiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent)
                }
            } else {
                val currentHourWeather = skyUiState.hourlyWeather.firstOrNull()
                
                // 메인 날씨 카드
                WeatherCard(
                    weather = currentHourWeather,
                    isImperial = skyUiState.isImperial
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 일출/일몰 및 월령 그리드
                SolarLunarGrid(
                    weather = currentHourWeather,
                    lat = mainUiState.lat,
                    lon = mainUiState.lon,
                    isImperial = skyUiState.isImperial
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 시간별 예보 리스트
                Text(
                    text = "Hourly Forecast",
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                HourlyForecastList(
                    weatherList = skyUiState.hourlyWeather,
                    isImperial = skyUiState.isImperial
                )
            }
        }
    }
}
