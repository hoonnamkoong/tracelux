package com.tracelux.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tl.R
import com.tracelux.ui.components.*
import com.tracelux.ui.theme.*
import com.tracelux.utils.LocationProvider
import com.tracelux.utils.WeatherUtils
import com.tracelux.utils.SolarCalculator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import com.tracelux.models.KakaoDocument
import com.tracelux.api.RetrofitClient

@Composable
fun SkyScreen(viewModel: SkyViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    // GPS 위치 기본값 (서울 시청)
    var lat by remember { mutableDoubleStateOf(37.5665) }
    var lon by remember { mutableDoubleStateOf(126.9780) }
    
    // 시간 슬라이더 
    var timeIndex by remember { mutableIntStateOf(0) }
    
    // 검색 모달
    var isSearchModalVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<KakaoDocument>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    
    // 초기 날씨 데이터 로드
    LaunchedEffect(Unit) {
        val isKoreaDefault = lat in 33.0..39.0 && lon in 124.0..132.0
        if (isKoreaDefault) {
            viewModel.setApiSource("KMA")
        }
        viewModel.fetchWeatherData(lat, lon, if(isKoreaDefault) "KMA" else uiState.apiSource, WeatherUtils.KMA_AUTH_KEY)
        
        try {
            val locationProvider = LocationProvider(context)
            locationProvider.getCurrentLocation { lLat, lLon ->
                lat = lLat
                lon = lLon
                val isKorea = lat in 33.0..39.0 && lon in 124.0..132.0
                if (isKorea) {
                    viewModel.setApiSource("KMA")
                } else {
                    viewModel.setApiSource("Open-Meteo")
                }
                viewModel.fetchWeatherData(lat, lon, if(isKorea) "KMA" else "Open-Meteo", WeatherUtils.KMA_AUTH_KEY)
            }
        } catch (e: Exception) {
            // 위치 권한 없거나 실패해도 기본값 유지
        }
    }
    
    // apiSource 변경시 데이터 재로드
    LaunchedEffect(uiState.apiSource) {
        viewModel.fetchWeatherData(lat, lon, uiState.apiSource, WeatherUtils.KMA_AUTH_KEY)
    }

    val maxIndex = if (uiState.hourlyWeather.isNotEmpty()) minOf(12, uiState.hourlyWeather.size - 1) else 0
    val weather = if (uiState.hourlyWeather.isNotEmpty()) uiState.hourlyWeather[timeIndex] else null
    
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val viewTime = weather?.time?.let { WeatherUtils.parseWeatherDate(it) } ?: Date()
    val timeText = SimpleDateFormat("HH:00", Locale.getDefault()).format(viewTime)

    // 천체 계산
    val sunTimes = SolarCalculator.getDetailedSunTimes(viewTime, lat, lon)
    val moonTimes = SolarCalculator.getMoonTimes(viewTime, lat, lon)
    val mwResult = SolarCalculator.calculateMilkyWayBest(viewTime, lat, lon)
    val moonIllum = SolarCalculator.getMoonIllumination(viewTime, lat, lon)

    // 운저고도 계산 등
    val t = weather?.temp ?: 0.0
    val h = weather?.humidity ?: 0.0
    val dp = weather?.dewPoint ?: WeatherUtils.calculateDewPoint(t, h)
    val cloudBase = ((t - dp) * 125.0).coerceAtLeast(0.0) / 1000.0 // km

    // KMA는 SKY+PTY 코드 기반, Open-Meteo는 WMO weatherCode 기반으로 날씨 설명 표시
    val weatherDesc = if (uiState.apiSource == "KMA") {
        WeatherUtils.getKmaSkyDesc(weather?.sky, weather?.pty)
    } else {
        WeatherUtils.getWeatherDesc(weather?.weatherCode ?: 0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(scrollState)
            .padding(bottom = 30.dp)
    ) {
        if (uiState.isLoading && uiState.hourlyWeather.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().height(400.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator(color = Orange)
            }
        } else {
            SkyHeader(
                locationName = uiState.locationName ?: "서울특별시 송파구",
                onGpsClick = {
                    try {
                        LocationProvider(context).getCurrentLocation { lLat, lLon ->
                            lat = lLat
                            lon = lLon
                            viewModel.fetchWeatherData(lat, lon, uiState.apiSource, WeatherUtils.KMA_AUTH_KEY)
                            Toast.makeText(context, "위치가 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "위치 정보를 가져올 수 없습니다. 권한을 확인해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            SkySearchBar(onClick = { isSearchModalVisible = true })
            Spacer(modifier = Modifier.height(15.dp))

            if (isSearchModalVisible) {
                AlertDialog(
                    onDismissRequest = { isSearchModalVisible = false },
                    title = { Text("주소 또는 장소 검색", color = Color.White) },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { q -> 
                                    searchQuery = q 
                                    coroutineScope.launch {
                                        try {
                                            val res = RetrofitClient.kakaoApi.searchKeyword("KakaoAK d5dc43418db84bd4c079afe1e3577db4", q)
                                            searchResults = res.documents
                                        } catch (e: Exception) {
                                            // Handle error if needed
                                        }
                                    }
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                placeholder = { Text("검색어 입력") },
                                singleLine = true
                            )
                            LazyColumn(modifier = Modifier.height(200.dp).padding(top = 8.dp)) {
                                items(searchResults) { doc ->
                                    Column(modifier = Modifier.fillMaxWidth().clickable {
                                        isSearchModalVisible = false
                                        lat = doc.y.toDoubleOrNull() ?: lat
                                        lon = doc.x.toDoubleOrNull() ?: lon
                                        viewModel.setLocationName(doc.place_name ?: doc.address_name)
                                        viewModel.fetchWeatherData(lat, lon, uiState.apiSource, WeatherUtils.KMA_AUTH_KEY)
                                    }.padding(8.dp)) {
                                        Text(doc.place_name ?: doc.address_name, color = Color.White)
                                        if (doc.place_name != null) {
                                            Text(doc.address_name, color = Color.Gray, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { isSearchModalVisible = false }) {
                            Text("닫기", color = Orange)
                        }
                    },
                    containerColor = Color(0xFF1E293B)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .background(Color(0xFF151E32), RoundedCornerShape(16.dp))
            ) {
                Column {
                    SkyConditionsHeader(
                        apiSource = uiState.apiSource,
                        onSourceChange = { viewModel.setApiSource(it) },
                        weatherDesc = weatherDesc
                    )

                    if (uiState.apiSource == "Open-Meteo") {
                        val visKm = (weather?.visibility ?: 0.0) / 1000.0
                        OpenMeteoWeatherGrid(
                            temp = WeatherUtils.formatVal(weather?.temp),
                            humidity = WeatherUtils.formatIntVal(weather?.humidity),
                            dewPoint = WeatherUtils.formatVal(weather?.dewPoint ?: WeatherUtils.calculateDewPoint(t, h)),
                            pop = WeatherUtils.formatIntVal(weather?.pop),
                            windDir = WeatherUtils.getWindDirectionText(weather?.windDirection, weather?.windSpeed),
                            wavePeriod = WeatherUtils.formatVal(weather?.wavePeriod),
                            cloudCover = WeatherUtils.formatIntVal(weather?.cloudCover),
                            cloudBase = WeatherUtils.formatVal(cloudBase),
                            visibility = WeatherUtils.formatVal(visKm)
                        )
                    } else {
                        WeatherGrid(
                            temp = WeatherUtils.formatVal(weather?.temp),
                            humidity = WeatherUtils.formatIntVal(weather?.humidity),
                            windDir = WeatherUtils.getWindDirectionText(weather?.windDirection, weather?.windSpeed),
                            pty = WeatherUtils.getPtyDesc(weather?.pty),
                            pop = WeatherUtils.formatIntVal(weather?.pop ?: weather?.prec),
                            lgt = WeatherUtils.formatIntVal(weather?.lgt?.toDouble()),
                            wav = WeatherUtils.formatVal(weather?.wav),
                            windSpeed = WeatherUtils.formatVal(weather?.windSpeed),
                            cloudBase = WeatherUtils.formatVal(cloudBase)
                        )
                    }

                    ForecastTimeSlider(
                        timeIndex = timeIndex,
                        maxIndex = maxIndex,
                        timeText = timeText,
                        onIndexChange = { timeIndex = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            SunMoonPathCard(
                title = "SUN PATH",
                riseTime = "-",
                setTime = "-",
                arcCanvas = {
                    SkyArcCanvas(
                        type = "SUN",
                        lat = lat,
                        lon = lon,
                        currentTime = viewTime
                    )
                }
            ) { /* Golden Hour/Blue Hour는 캔버스 내부에서 렌더링 */ }

            Spacer(modifier = Modifier.height(15.dp))

            SunMoonPathCard(
                title = "MOON PATH",
                riseTime = "-",
                setTime = "-",
                arcCanvas = {
                    val mwImg = ImageBitmap.imageResource(id = R.drawable.milky_way)
                    SkyArcCanvas(
                        type = "MOON",
                        lat = lat,
                        lon = lon,
                        currentTime = viewTime,
                        mwResult = SolarCalculator.calculateMilkyWayBest(viewTime, lat, lon),
                        milkyWayImage = mwImg
                    )
                }
            ) { /* Milky Way 텍스트는 캔버스 내부에서 렌더링 */ }
        }
    }
}
