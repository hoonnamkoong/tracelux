package com.tracelux.models

import kotlinx.serialization.Serializable

/**
 * 인벤토리 카테고리 (카메라, 렌즈)
 */
@Serializable
enum class InventoryCategory {
    CAMERA, LENS
}

/**
 * 센서 크기 열거형
 */
@Serializable
enum class SensorSize(val displayName: String) {
    FULL_FRAME("Full Frame"),
    APS_C("APS-C"),
    M43("M4/3"),
    MEDIUM("Medium")
}

/**
 * 렌즈 타입 열거형
 */
@Serializable
enum class LensType(val displayName: String) {
    PRIME("Prime"),
    ZOOM("Zoom")
}

/**
 * 인벤토리 아이템 데이터 클래스
 * APK 원본의 13개 필드 구조를 그대로 복원합니다.
 */
@Serializable
data class InventoryItem(
    val id: String,
    val category: InventoryCategory,
    val brand: String,
    val model: String? = null,       // 카메라 전용
    val sensor: SensorSize? = null,  // 카메라 전용
    val megapixels: String? = null,  // 카메라 전용
    val lensType: LensType? = null,  // 렌즈 전용
    val focalLength: String? = null, // 단렌즈 전용
    val aperture: String? = null,    // 단렌즈 전용
    val minFocal: String? = null,    // 줌렌즈 전용
    val maxFocal: String? = null,    // 줌렌즈 전용
    val minAperture: String? = null, // 줌렌즈 전용
    val maxAperture: String? = null  // 줌렌즈 전용
)
