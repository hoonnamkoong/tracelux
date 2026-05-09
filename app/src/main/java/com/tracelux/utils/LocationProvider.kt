package com.tracelux.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

/**
 * 위치 정보 제공 클래스
 * APK 원본의 LocationProvider를 Kotlin으로 복원
 */
class LocationProvider(context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(onSuccess: (Double, Double) -> Unit) {
        val cts = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cts.token
        ).addOnSuccessListener { location: Location? ->
            if (location != null) {
                onSuccess(location.latitude, location.longitude)
            }
        }
    }
}
