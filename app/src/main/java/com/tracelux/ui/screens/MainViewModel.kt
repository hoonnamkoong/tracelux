package com.tracelux.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.tracelux.utils.LocationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 메인 뷰모델 - 위치 및 전역 상태 관리
 * APK 원본의 MainViewModel 1:1 복원
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val locationProvider = LocationProvider(application)
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun refreshLocation() {
        locationProvider.getCurrentLocation { lat, lng ->
            _uiState.update { it.copy(lat = lat, lon = lng) }
        }
    }

    fun setArViewerVisible(visible: Boolean) {
        _uiState.update { it.copy(isArViewerVisible = visible) }
    }

    fun updateFocalLength(focal: Float) {
        _uiState.update { it.copy(focalLength = focal) }
    }
}

data class MainUiState(
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val isArViewerVisible: Boolean = false,
    val focalLength: Float = 50f
)
