package com.tracelux.ar

import android.content.Context
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.view.Surface
import kotlin.math.*

/**
 * AR 및 센서 계산을 위한 정밀 수학 유틸리티 (원본 APK 로직 기반)
 */
object ArMathUtils {

    private val lastRotationMatrix = FloatArray(9)

    fun getLastRotationMatrix(): FloatArray = lastRotationMatrix

    /**
     * 원본 APK의 정밀 오리엔테이션 추출 수식
     */
    fun extractOrientationAngles(
        rotationVector: FloatArray,
        displayRotation: Int
    ): FloatArray {
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector)

        val remappedMatrix = FloatArray(9)
        when (displayRotation) {
            Surface.ROTATION_90 -> SensorManager.remapCoordinateSystem(rotationMatrix, 2, 129, remappedMatrix)
            Surface.ROTATION_180 -> SensorManager.remapCoordinateSystem(rotationMatrix, 129, 130, remappedMatrix)
            Surface.ROTATION_270 -> SensorManager.remapCoordinateSystem(rotationMatrix, 130, 1, remappedMatrix)
            else -> System.arraycopy(rotationMatrix, 0, remappedMatrix, 0, 9)
        }

        System.arraycopy(remappedMatrix, 0, lastRotationMatrix, 0, 9)

        // 원본 커스텀 수식 적용
        val cx = -remappedMatrix[2]
        val cy = -remappedMatrix[5]
        val cz = -remappedMatrix[8]
        
        val pitchDeg = Math.toDegrees(asin(cz.coerceIn(-1f, 1f).toDouble())).toFloat()
        val azimuthDeg = (Math.toDegrees(atan2(cx.toDouble(), cy.toDouble())).toFloat() + 360.0f) % 360.0f
        
        // Roll은 표준 getOrientation 사용 (원본과 동일)
        val orientation = FloatArray(3)
        SensorManager.getOrientation(remappedMatrix, orientation)
        val rollDeg = Math.toDegrees(orientation[2].toDouble()).toFloat()

        return floatArrayOf(azimuthDeg, pitchDeg, rollDeg)
    }

    /**
     * Camera2 API를 활용한 실제 기기 수평 화각(FOV) 계산
     */
    fun calculateDeviceHorizontalFov(context: Context): Float {
        return try {
            val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val id = manager.cameraIdList.firstOrNull { 
                manager.getCameraCharacteristics(it).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK 
            } ?: manager.cameraIdList[0]
            
            val characteristics = manager.getCameraCharacteristics(id)
            val sensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)!!
            val focalLength = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)!![0]
            
            val fov = 2 * atan(sensorSize.width / (2 * focalLength))
            Math.toDegrees(fov.toDouble()).toFloat()
        } catch (e: Exception) {
            60f // 실패 시 기본값
        }
    }

    fun calculateDeviceVerticalFov(context: Context): Float {
        return try {
            val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val id = manager.cameraIdList.firstOrNull { 
                manager.getCameraCharacteristics(it).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK 
            } ?: manager.cameraIdList[0]
            
            val characteristics = manager.getCameraCharacteristics(id)
            val sensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)!!
            val focalLength = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)!![0]
            
            val fov = 2 * atan(sensorSize.height / (2 * focalLength))
            Math.toDegrees(fov.toDouble()).toFloat()
        } catch (e: Exception) {
            40f
        }
    }

    /**
     * 구면 좌표를 3D 카테시안 좌표로 변환 (원본 로직)
     */
    fun sphericalToCartesian(azimuth: Double, altitude: Double, radius: Float): FloatArray {
        val azRad = Math.toRadians(azimuth)
        val altRad = Math.toRadians(altitude)
        val x = radius.toDouble() * cos(altRad) * sin(azRad)
        val y = radius.toDouble() * sin(altRad)
        val z = (-radius).toDouble() * cos(altRad) * cos(azRad)
        return floatArrayOf(x.toFloat(), y.toFloat(), z.toFloat())
    }

    /**
     * FOV 비율 계산 (타겟 초점거리에 맞는 프레임 크기 비율 반환)
     */
    fun calculateFovFrameRatio(targetFocalMm: Float, deviceFov: Float): Float {
        if (targetFocalMm <= 0f) return 1f
        // 35mm 풀프레임 기준 (36mm 너비) 수평 화각
        val targetHovRad = 2.0 * atan(36.0 / (targetFocalMm * 2.0))
        val deviceFovRad = Math.toRadians(deviceFov.toDouble())
        
        val targetTan = tan(targetHovRad / 2.0)
        val deviceTan = tan(deviceFovRad / 2.0)
        
        return (targetTan / deviceTan).toFloat().coerceIn(0.1f, 3.0f)
    }

    /**
     * 행렬을 Z축(수직축) 기준으로 회전 (자기 편각 보정용)
     */
    fun rotateMatrixZ(matrix: FloatArray, degrees: Float): FloatArray {
        val result = FloatArray(9)
        val angleRad = Math.toRadians(degrees.toDouble())
        val cosA = cos(angleRad).toFloat()
        val sinA = sin(angleRad).toFloat()

        // ENU 좌표계에서 Z축 회전 (X, Y 평면 회전)
        // x' = x*cos - y*sin
        // y' = x*sin + y*cos
        // 행렬 곱셈: R_z * Matrix
        result[0] = matrix[0] * cosA - matrix[3] * sinA
        result[1] = matrix[1] * cosA - matrix[4] * sinA
        result[2] = matrix[2] * cosA - matrix[5] * sinA

        result[3] = matrix[0] * sinA + matrix[3] * cosA
        result[4] = matrix[1] * sinA + matrix[4] * cosA
        result[5] = matrix[2] * sinA + matrix[5] * cosA

        result[6] = matrix[6]
        result[7] = matrix[7]
        result[8] = matrix[8]

        return result
    }
}


