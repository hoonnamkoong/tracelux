package com.tracelux.utils

import android.content.Context
import android.content.SharedPreferences
import com.tracelux.models.InventoryItem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 인벤토리 데이터 저장소
 * SharedPreferences와 JSON 직렬화를 사용하여 장비 목록을 관리합니다.
 */
class InventoryStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("inventory_prefs", Context.MODE_PRIVATE)
    private val STORAGE_KEY = "@inventory_data"

    /**
     * 장비 목록 저장
     */
    fun saveInventory(items: List<InventoryItem>) {
        try {
            val jsonString = Json.encodeToString(items)
            prefs.edit().putString(STORAGE_KEY, jsonString).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 장비 목록 불러오기
     */
    fun loadInventory(): List<InventoryItem> {
        val jsonString = prefs.getString(STORAGE_KEY, null) ?: return emptyList()
        return try {
            Json.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
