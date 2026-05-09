package com.tracelux.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tracelux.api.RetrofitClient
import com.tracelux.models.HourlyWeather
import com.tracelux.utils.WeatherUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*

/**
 * Sky 화면용 뷰모델 - 날씨 및 일출/일몰 데이터 관리
 */
class SkyViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SkyUiState())
    val uiState: StateFlow<SkyUiState> = _uiState.asStateFlow()

    fun fetchWeatherData(lat: Double, lon: Double, source: String, kmaKey: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val weatherList = WeatherUtils.fetchWeather(
                    source = source,
                    omApi = RetrofitClient.openMeteoApi,
                    kmaApi = RetrofitClient.kmaApi,
                    lat = lat,
                    lon = lon,
                    authKey = kmaKey
                )
                _uiState.update { 
                    it.copy(
                        hourlyWeather = weatherList,
                        isLoading = false,
                        lastUpdated = Date()
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun setApiSource(source: String) {
        _uiState.update { it.copy(apiSource = source) }
    }

    fun setImperial(isImperial: Boolean) {
        _uiState.update { it.copy(isImperial = isImperial) }
    }

    fun setLocationName(name: String) {
        _uiState.update { it.copy(locationName = name) }
    }
}

data class SkyUiState(
    val hourlyWeather: List<HourlyWeather> = emptyList(),
    val apiSource: String = "Open-Meteo",
    val isImperial: Boolean = false,
    val locationName: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastUpdated: Date? = null
)
