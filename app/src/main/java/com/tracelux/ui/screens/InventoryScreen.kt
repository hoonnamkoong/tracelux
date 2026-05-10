package com.tracelux.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tracelux.models.*
import com.tracelux.ui.components.InventoryCard
import com.tracelux.ui.theme.*
import com.tracelux.ui.viewmodel.InventoryViewModel

/**
 * 인벤토리 메인 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(viewModel: InventoryViewModel = viewModel()) {
    val inventoryList by viewModel.inventoryList.collectAsState()
    var isSheetVisible by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<InventoryItem?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // 장비 목록
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 40.dp, bottom = 100.dp)
        ) {
            items(inventoryList, key = { it.id }) { item ->
                InventoryCard(item = item) {
                    editingItem = item
                    isSheetVisible = true
                }
            }
        }

        // 추가 버튼
        Button(
            onClick = {
                editingItem = null
                isSheetVisible = true
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(20.dp)
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Orange),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("+ Add", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        // 편집용 하단 시트
        if (isSheetVisible) {
            ModalBottomSheet(
                onDismissRequest = { isSheetVisible = false },
                sheetState = sheetState,
                containerColor = CardBg,
                dragHandle = { BottomSheetDefaults.DragHandle(color = TextDim) }
            ) {
                InventoryEditor(
                    initialItem = editingItem,
                    onSave = {
                        viewModel.saveItem(it)
                        isSheetVisible = false
                    },
                    onDelete = {
                        viewModel.deleteItem(it)
                        isSheetVisible = false
                    },
                    onCancel = { isSheetVisible = false }
                )
            }
        }
    }
}

/**
 * 장비 편집 에디터 (BottomSheet 내부용)
 */
@Composable
fun InventoryEditor(
    initialItem: InventoryItem?,
    onSave: (InventoryItem) -> Unit,
    onDelete: (String) -> Unit,
    onCancel: () -> Unit
) {
    var category by remember { mutableStateOf(initialItem?.category ?: InventoryCategory.CAMERA) }
    var brand by remember { mutableStateOf(initialItem?.brand ?: "") }
    var model by remember { mutableStateOf(initialItem?.model ?: "") }
    var sensor by remember { mutableStateOf(initialItem?.sensor ?: SensorSize.FULL_FRAME) }
    var megapixels by remember { mutableStateOf(initialItem?.megapixels ?: "") }
    var lensType by remember { mutableStateOf(initialItem?.lensType ?: LensType.PRIME) }
    var focalLength by remember { mutableStateOf(initialItem?.focalLength ?: "") }
    var aperture by remember { mutableStateOf(initialItem?.aperture ?: "") }
    var minFocal by remember { mutableStateOf(initialItem?.minFocal ?: "") }
    var maxFocal by remember { mutableStateOf(initialItem?.maxFocal ?: "") }
    var minAperture by remember { mutableStateOf(initialItem?.minAperture ?: "") }
    var maxAperture by remember { mutableStateOf(initialItem?.maxAperture ?: "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .navigationBarsPadding()
    ) {
        Text(
            text = if (initialItem == null) "새 장비 추가" else "장비 수정",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // 카테고리 선택
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            ToggleOption("CAMERA", category == InventoryCategory.CAMERA, { category = InventoryCategory.CAMERA }, Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            ToggleOption("LENS", category == InventoryCategory.LENS, { category = InventoryCategory.LENS }, Modifier.weight(1f))
        }

        // 입력 필드들
        EditorInput("브랜드명", brand, { brand = it }, "예: Sony, Canon")

        if (category == InventoryCategory.CAMERA) {
            EditorInput("모델명", model, { model = it }, "예: A7R IV")
            
            Text("센서 크기", color = TextDim, fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp, bottom = 8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                SensorSize.values().forEach { size ->
                    SubToggleOption(size.displayName, sensor == size, { sensor = size }, Modifier.weight(1f))
                }
            }
            
            EditorInput("해상도 (MP)", megapixels, { megapixels = it }, "예: 61", isNumeric = true)
        } else {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 12.dp)) {
                SubToggleOption("PRIME (단렌즈)", lensType == LensType.PRIME, { lensType = LensType.PRIME }, Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                SubToggleOption("ZOOM (줌렌즈)", lensType == LensType.ZOOM, { lensType = LensType.ZOOM }, Modifier.weight(1f))
            }

            if (lensType == LensType.PRIME) {
                Row {
                    Box(modifier = Modifier.weight(1f)) { EditorInput("초점거리 (mm)", focalLength, { focalLength = it }, "예: 35", true) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) { EditorInput("조리개 (f/)", aperture, { aperture = it }, "예: 1.4", true) }
                }
            } else {
                Row {
                    Box(modifier = Modifier.weight(1f)) { EditorInput("최소 초점거리", minFocal, { minFocal = it }, "예: 16", true) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) { EditorInput("최대 초점거리", maxFocal, { maxFocal = it }, "예: 35", true) }
                }
                Row {
                    Box(modifier = Modifier.weight(1f)) { EditorInput("최소 조리개", minAperture, { minAperture = it }, "예: 2.8", true) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) { EditorInput("최대 조리개", maxAperture, { maxAperture = it }, "예: 2.8", true) }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // 버튼 영역
        Button(
            onClick = {
                val finalItem = InventoryItem(
                    id = initialItem?.id ?: System.currentTimeMillis().toString(),
                    category = category,
                    brand = brand,
                    model = if (category == InventoryCategory.CAMERA) model else null,
                    sensor = if (category == InventoryCategory.CAMERA) sensor else null,
                    megapixels = if (category == InventoryCategory.CAMERA) megapixels else null,
                    lensType = if (category == InventoryCategory.LENS) lensType else null,
                    focalLength = if (category == InventoryCategory.LENS && lensType == LensType.PRIME) focalLength else null,
                    aperture = if (category == InventoryCategory.LENS && lensType == LensType.PRIME) aperture else null,
                    minFocal = if (category == InventoryCategory.LENS && lensType == LensType.ZOOM) minFocal else null,
                    maxFocal = if (category == InventoryCategory.LENS && lensType == LensType.ZOOM) maxFocal else null,
                    minAperture = if (category == InventoryCategory.LENS && lensType == LensType.ZOOM) minAperture else null,
                    maxAperture = if (category == InventoryCategory.LENS && lensType == LensType.ZOOM) maxAperture else null
                )
                onSave(finalItem)
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Orange),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("저장하기", fontWeight = FontWeight.Bold)
        }

        if (initialItem != null) {
            TextButton(
                onClick = { onDelete(initialItem.id) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text("삭제하기", color = ErrorRed)
            }
        }
    }
}

@Composable
fun EditorInput(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String, isNumeric: Boolean = false) {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Text(label, color = TextDim, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextDim.copy(alpha = 0.5f), fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Orange,
                unfocusedIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(keyboardType = if (isNumeric) KeyboardType.Decimal else KeyboardType.Text),
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
fun ToggleOption(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .height(40.dp)
            .background(if (isSelected) Orange else Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (isSelected) DarkBg else Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun SubToggleOption(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .height(32.dp)
            .background(if (isSelected) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (isSelected) Color.White else TextDim, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}
