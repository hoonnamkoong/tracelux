package com.tracelux.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tracelux.models.InventoryCategory
import com.tracelux.models.InventoryItem
import com.tracelux.utils.InventoryStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 인벤토리 화면 ViewModel
 */
class InventoryViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = InventoryStorage(application)
    
    private val _inventoryList = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventoryList: StateFlow<List<InventoryItem>> = _inventoryList.asStateFlow()

    init {
        loadItems()
    }

    /**
     * 저장된 아이템 로드 및 정렬 (카메라 우선 -> ID순)
     */
    fun loadItems() {
        viewModelScope.launch {
            val items = storage.loadInventory()
            val sortedItems = items.sortedWith(
                compareBy<InventoryItem> { it.category != InventoryCategory.CAMERA }
                    .thenBy { it.id }
            )
            _inventoryList.value = sortedItems
        }
    }

    /**
     * 아이템 저장 또는 수정
     */
    fun saveItem(item: InventoryItem) {
        viewModelScope.launch {
            val currentList = _inventoryList.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == item.id }
            
            if (index != -1) {
                currentList[index] = item
            } else {
                currentList.add(0, item)
            }
            
            storage.saveInventory(currentList)
            loadItems()
        }
    }

    /**
     * 아이템 삭제
     */
    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            val currentList = _inventoryList.value.filter { it.id != itemId }
            storage.saveInventory(currentList)
            loadItems()
        }
    }
}
