package com.tracelux.utils

import android.content.Context
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.SizeF
import kotlin.math.*

/**
 * 원본 APK의 ArMathUtils.java를 1:1로 복원한 Kotlin 클래스입니다.
 * 구형 좌표계를 직교 좌표계로 변환하고, 기기 화각 및 센서 데이터를 처리합니다.
 */
object ArMathUtils {
    private val lastRotationMatrix = FloatArray(9)

    fun getLastRotationMatrix(): FloatArray = lastRotationMatrix

    /**
     * 구형 좌표(방위각, 고도)를 AR 공간의 직교 좌표(X, Y, Z)로 변환합니다.
     */
    fun sphericalToCartesian(azimuth: Double, altitude: Double, radius: Float = 50.0f): FloatArray {
        val azRad = Math.toRadians(azimuth)
        val altRad = Math.toRadians(altitude)
        
        val x = radius.toDouble() * cos(altRad) * sin(azRad)
        val y = radius.toDouble() * sin(altRad)
        val z = (-radius).toDouble() * cos(altRad) * cos(azRad)
        
        return floatArrayOf(x.toFloat(), y.toFloat(), z.toFloat())
    }

    /**
     * 기기의 실제 카메라 하드웨어 정보를 바탕으로 수평 화각(Horizontal FOV)을 계산합니다.
     */
    fun calculateDeviceHorizontalFov(context: Context): Float {
        return try {
            val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val backCameraId = manager.cameraIdList.firstOrNull { id ->
                manager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            } ?: manager.cameraIdList[0]
            
            val characteristics = manager.getCameraCharacteristics(backCameraId)
            val sensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE) ?: SizeF(3.6f, 2.4f)
            val focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
            val focalLength = focalLengths?.get(0) ?: 4.0f
            
            // FOV = 2 * atan(sensorSize / (2 * focalLength))
            val hFov = 2 * atan(sensorSize.width / (2 * focalLength))
            Math.toDegrees(hFov.toDouble()).toFloat()
        } catch (e: Exception) {
            60.0f // 폴백 값
        }
    }

    /**
     * 기기의 실제 카메라 하드웨어 정보를 바탕으로 수직 화각(Vertical FOV)을 계산합니다.
     */
    fun calculateDeviceVerticalFov(context: Context): Float {
        return try {
            val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val backCameraId = manager.cameraIdList.firstOrNull { id ->
                manager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            } ?: manager.cameraIdList[0]
            
            val characteristics = manager.getCameraCharacteristics(backCameraId)
            val sensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE) ?: SizeF(3.6f, 2.4f)
            val focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
            val focalLength = focalLengths?.get(0) ?: 4.0f
            
            val vFov = 2 * atan(sensorSize.height / (2 * focalLength))
            Math.toDegrees(vFov.toDouble()).toFloat()
        } catch (e: Exception) {
            45.0f // 폴백 값
        }
    }

    /**
     * 타겟 초점 거리와 기기 화각 사이의 비율을 계산하여 AR 프레임 크기를 조정합니다.
     */
    fun calculateFovFrameRatio(targetFocalMm: Float, deviceFov: Float): Float {
        if (targetFocalMm <= 0.0f) return 1.0f
        
        // Full Frame(36mm width) 기준 타겟 화각 계산
        val targetHovRad = 2 * atan(36.0 / (targetFocalMm * 2.0))
        val deviceFovRad = Math.toRadians(deviceFov.toDouble())
        
        val targetTan = tan(targetHovRad / 2.0)
        val deviceTan = tan(deviceFovRad / 2.0)
        
        return (targetTan / deviceTan).toFloat().coerceIn(0.1f, 3.0f)
    }

    /**
     * 방위각을 8방위 텍스트(N, NE, E...)로 변환합니다.
     */
    fun getCompassDirection(azimuth: Float): String {
        val directions = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW", "N")
        val normalizedAzimuth = ((azimuth % 360) + 360) % 360
        val index = (normalizedAzimuth / 45.0).roundToInt()
        return directions[index]
    }

    /**
     * 회전 벡터 센서 데이터로부터 방위각, 피치, 롤을 추출합니다.
     */
    fun extractOrientationAngles(rotationVector: FloatArray, displayRotation: Int = 0): FloatArray {
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector)
        
        val displayMappedMatrix = FloatArray(9)
        when (displayRotation) {
            0 -> System.arraycopy(rotationMatrix, 0, displayMappedMatrix, 0, 9)
            1 -> SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, displayMappedMatrix)
            2 -> SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y, displayMappedMatrix)
            3 -> SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X, displayMappedMatrix)
            else -> System.arraycopy(rotationMatrix, 0, displayMappedMatrix, 0, 9)
        }
        
        System.arraycopy(displayMappedMatrix, 0, lastRotationMatrix, 0, 9)
        
        // 커스텀 방향 계산 (원본 로직 유지)
        val cx = -rotationMatrix[2]
        val cy = -rotationMatrix[5]
        val cz = -rotationMatrix[8]
        
        val pitchDeg = Math.toDegrees(asin(cz.toDouble().coerceIn(-1.0, 1.0))).toFloat()
        val azimuthDeg = (Math.toDegrees(atan2(cx.toDouble(), cy.toDouble())).toFloat() + 360.0f) % 360.0f
        
        val orientation = FloatArray(3)
        SensorManager.getOrientation(displayMappedMatrix, orientation)
        val rollDeg = Math.toDegrees(orientation[2].toDouble()).toFloat()
        
        return floatArrayOf(azimuthDeg, pitchDeg, rollDeg)
    }
}
