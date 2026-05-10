package com.tracelux.utils

import android.content.Context
import android.content.SharedPreferences
import com.tracelux.models.FieldState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 필드 설정 데이터 저장소
 */
class FieldStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("field_prefs", Context.MODE_PRIVATE)
    private val STORAGE_KEY = "last_field_state"
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * 마지막 설정 상태 저장
     */
    fun saveFieldState(state: FieldState) {
        try {
            val jsonString = json.encodeToString(state)
            prefs.edit().putString(STORAGE_KEY, jsonString).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 마지막 설정 상태 로드 (없으면 기본값)
     */
    fun loadFieldState(): FieldState {
        val jsonString = prefs.getString(STORAGE_KEY, null) ?: return FieldState()
        return try {
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            FieldState()
        }
    }
}
