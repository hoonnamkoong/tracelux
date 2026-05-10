package com.tracelux.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tracelux.models.*
import com.tracelux.utils.FieldStorage
import com.tracelux.utils.InventoryStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * 필드 설정 및 계산 ViewModel
 */
class FieldViewModel(application: Application) : AndroidViewModel(application) {
    private val fieldStorage = FieldStorage(application)
    private val inventoryStorage = InventoryStorage(application)

    private val _uiState = MutableStateFlow(fieldStorage.loadFieldState())
    val uiState: StateFlow<FieldState> = _uiState.asStateFlow()

    private val _inventoryList = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventoryList: StateFlow<List<InventoryItem>> = _inventoryList.asStateFlow()

    init {
        refreshInventory()
    }

    fun refreshInventory() {
        viewModelScope.launch {
            val items = inventoryStorage.loadInventory()
            _inventoryList.value = items
            
            // 자동 선택 로직 (선택된 장비가 없고 인벤토리에 장비가 있을 경우)
            val current = _uiState.value
            var updated = current
            
            if (current.selectedCameraId == null) {
                items.firstOrNull { it.category == InventoryCategory.CAMERA }?.let {
                    updated = updated.copy(selectedCameraId = it.id)
                }
            }
            if (current.selectedLensId == null) {
                items.firstOrNull { it.category == InventoryCategory.LENS }?.let {
                    updated = updated.copy(selectedLensId = it.id)
                }
            }
            
            if (updated != current) {
                _uiState.value = updated
                fieldStorage.saveFieldState(updated)
            }
        }
    }

    /**
     * 상태 업데이트 및 자동 저장
     */
    fun updateState(update: (FieldState) -> FieldState) {
        val newState = update(_uiState.value)
        _uiState.value = newState
        fieldStorage.saveFieldState(newState)
    }

    /**
     * 장비 선택 처리
     */
    fun selectGear(item: InventoryItem) {
        updateState { currentState ->
            if (item.category == InventoryCategory.CAMERA) {
                currentState.copy(selectedCameraId = item.id)
            } else {
                val initialAperture = if (item.lensType == LensType.PRIME) item.aperture else item.minAperture
                val initialFocal = if (item.lensType == LensType.PRIME) item.focalLength else item.minFocal
                currentState.copy(
                    selectedLensId = item.id,
                    aperture = initialAperture ?: currentState.aperture,
                    currentFocal = initialFocal ?: currentState.currentFocal
                )
            }
        }
    }

    /**
     * ND 필터 토글
     */
    fun toggleNdFilter(stop: Int) {
        updateState { currentState ->
            val newList = if (currentState.ndFilters.contains(stop)) {
                currentState.ndFilters.filter { it != stop }
            } else {
                currentState.ndFilters + stop
            }
            currentState.copy(ndFilters = newList)
        }
    }

    /**
     * NPF Rule 계산 (별 사진 궤적 방지 노출 시간)
     */
    fun calculateNPF(): String {
        val state = _uiState.value
        val camera = _inventoryList.value.find { it.id == state.selectedCameraId } ?: return "--"
        val focal = state.currentFocal.toDoubleOrNull() ?: return "--"
        val aperture = state.aperture.toDoubleOrNull() ?: 2.8

        val sensorWidth = when (camera.sensor) {
            SensorSize.APS_C -> 23.5
            SensorSize.M43 -> 17.3
            else -> 36.0 // FULL_FRAME or MEDIUM (Simplified)
        }

        val megapixels = camera.megapixels?.toDoubleOrNull() ?: 24.0
        val imageWidth = Math.sqrt((1000000.0 * megapixels * 3.0) / 2.0)
        val pixelPitch = (sensorWidth / imageWidth) * 1000.0
        
        val npf = ((35.0 * aperture) + (30.0 * pixelPitch)) / focal
        return String.format("%.1fs", npf)
    }

    /**
     * 과초점거리 계산
     */
    fun calculateHyperfocal(): String {
        val state = _uiState.value
        val camera = _inventoryList.value.find { it.id == state.selectedCameraId } ?: return "--"
        val focal = state.currentFocal.toDoubleOrNull() ?: return "--"
        val aperture = state.aperture.toDoubleOrNull() ?: 2.8

        val coc = when (camera.sensor) {
            SensorSize.APS_C -> 0.02
            SensorSize.M43 -> 0.015
            SensorSize.MEDIUM -> 0.045
            else -> 0.03 // FULL_FRAME
        }

        val hyperfocalM = ((focal * focal) / (aperture * coc)) / 1000.0
        return String.format("%.2fm", hyperfocalM)
    }

    /**
     * ND 필터 적용 후 최종 노출 시간 계산
     */
    fun calculateNdExposure(): String {
        val state = _uiState.value
        val baseVal = state.shutterValue.toDoubleOrNull() ?: return "--"
        
        val baseSeconds = when (state.shutterMode) {
            ShutterMode.FRACTION -> 1.0 / baseVal
            ShutterMode.SECONDS -> baseVal
            ShutterMode.MINUTES -> baseVal * 60.0
        }

        val totalStops = state.ndFilters.sum()
        if (totalStops == 0) return "NO ND"

        val finalSeconds = 2.0.pow(totalStops.toDouble()) * baseSeconds
        val roundedSeconds = findClosestShutter(finalSeconds)

        return when {
            roundedSeconds < 0.25 -> {
                val fraction = (1.0 / roundedSeconds).roundToInt()
                "1/$fraction"
            }
            roundedSeconds < 60.0 -> String.format("%.1fs", roundedSeconds)
            roundedSeconds < 3600.0 -> {
                val m = (roundedSeconds / 60.0).toInt()
                val s = (roundedSeconds % 60.0).roundToInt()
                if (s == 0 || s == 60) "${if (s == 60) m + 1 else m}m" else "${m}m ${s}s"
            }
            else -> {
                val h = (roundedSeconds / 3600.0).toInt()
                val m = ((roundedSeconds % 3600.0) / 60.0).roundToInt()
                if (m == 0 || m == 60) "${if (m == 60) h + 1 else h}h" else "${h}h ${m}m"
            }
        }
    }

    private fun findClosestShutter(seconds: Double): Double {
        if (seconds > 30.0) {
            return if (seconds > 60.0) Math.rint(seconds / 5.0) * 5.0 else Math.rint(seconds)
        }
        return FieldConstants.STANDARD_SHUTTER_SPEEDS.minByOrNull { 
            Math.abs(log2(it) - log2(seconds)) 
        } ?: seconds
    }
}
