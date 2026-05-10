package com.tracelux.utils

import android.content.Context
import android.content.SharedPreferences
import com.tracelux.models.AppOptions
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 앱 옵션 설정 데이터 저장소
 */
class OptionsStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("options_prefs", Context.MODE_PRIVATE)
    private val STORAGE_KEY = "app_options"
    private val json = Json { ignoreUnknownKeys = true }

    fun saveOptions(options: AppOptions) {
        try {
            val jsonString = json.encodeToString(options)
            prefs.edit().putString(STORAGE_KEY, jsonString).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadOptions(): AppOptions {
        val jsonString = prefs.getString(STORAGE_KEY, null) ?: return AppOptions()
        return try {
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            AppOptions()
        }
    }

    fun clearAllData(context: Context) {
        // 모든 SharedPreferences 초기화 (비동기 apply 대신 동기식 commit 사용으로 강제 종료 전 기록 보장)
        val prefNames = listOf("options_prefs", "field_prefs", "inventory_prefs", "sky_prefs", "main_prefs")
        for (name in prefNames) {
            context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear().commit()
        }
    }
}
