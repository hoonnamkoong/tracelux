package com.tracelux.utils

import android.content.Context
import android.content.SharedPreferences
import com.tracelux.models.InventoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 인벤토리 데이터 저장소
 * SharedPreferences와 JSON 직렬화를 사용하여 장비 목록을 관리합니다.
 */
/**
 * 인벤토리 데이터 저장소
 * SharedPreferences와 JSON 직렬화를 사용하여 장비 목록을 관리합니다.
 * 싱글톤 패턴과 StateFlow를 사용하여 여러 화면에서 실시간으로 데이터를 공유합니다.
 */
class InventoryStorage private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("inventory_prefs", Context.MODE_PRIVATE)
    private val STORAGE_KEY = "@inventory_data"
    
    private val _items = MutableStateFlow<List<InventoryItem>>(loadInventoryFromPrefs())
    val items: StateFlow<List<InventoryItem>> = _items.asStateFlow()

    companion object {
        @Volatile
        private var INSTANCE: InventoryStorage? = null

        fun getInstance(context: Context): InventoryStorage {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: InventoryStorage(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * 장비 목록 저장
     */
    fun saveInventory(items: List<InventoryItem>) {
        try {
            val jsonString = Json.encodeToString(items)
            prefs.edit().putString(STORAGE_KEY, jsonString).apply()
            _items.value = items // Flow 업데이트
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 내부 프리퍼런스에서 로드
     */
    private fun loadInventoryFromPrefs(): List<InventoryItem> {
        val jsonString = prefs.getString(STORAGE_KEY, null) ?: return emptyList()
        return try {
            Json.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 현재 메모리의 장비 목록 반환 (Flow를 사용하지 않을 경우 대비)
     */
    fun loadInventory(): List<InventoryItem> = _items.value
}

