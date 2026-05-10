package com.tracelux.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tracelux.models.*
import com.tracelux.ui.components.*
import com.tracelux.ui.theme.*
import com.tracelux.ui.viewmodel.FieldViewModel

import com.tracelux.ui.viewmodel.OptionsViewModel
import com.tracelux.models.AppLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldScreen(
    viewModel: FieldViewModel = viewModel(),
    optionsViewModel: OptionsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val inventoryList by viewModel.inventoryList.collectAsState()
    val options by optionsViewModel.options.collectAsState()
    val isKo = options.language == AppLanguage.KO
    var showSheet by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val selectedCamera = inventoryList.find { it.id == uiState.selectedCameraId }
    val selectedLens = inventoryList.find { it.id == uiState.selectedLensId }

    Scaffold(
        containerColor = DarkBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp)
        ) {
            // 01 ACTIVE GEAR
            item {
                FieldSection(title = "01 ACTIVE GEAR") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GearCard(
                            label = selectedCamera?.let { "${it.brand} ${it.model ?: ""}" } ?: if(isKo) "카메라 선택" else "Select Camera",
                            onClick = { showSheet = "GEAR_CAM" },
                            modifier = Modifier.weight(1f)
                        )
                        GearCard(
                            label = selectedLens?.let { "${it.brand} ${it.model ?: ""}" } ?: if(isKo) "렌즈 선택" else "Select Lens",
                            onClick = { showSheet = "GEAR_LENS" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 02 CAMERA SETTING
            item {
                FieldSection(title = "02 CAMERA SETTING") {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        SettingCard(
                            label = "F NO.",
                            value = "f/${uiState.aperture}",
                            onSelect = { showSheet = "APERTURE" },
                            onValueChange = { viewModel.updateState { s -> s.copy(aperture = it) } },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        SettingCard(
                            label = "ISO",
                            value = uiState.iso,
                            onSelect = { showSheet = "ISO" },
                            onValueChange = { viewModel.updateState { s -> s.copy(iso = it) } },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        val shutterDisplay = when (uiState.shutterMode) {
                            ShutterMode.FRACTION -> "1/${uiState.shutterValue}"
                            ShutterMode.SECONDS -> "${uiState.shutterValue}"
                            ShutterMode.MINUTES -> "${uiState.shutterValue}"
                        }
                        val shutterLabel = when (uiState.shutterMode) {
                            ShutterMode.FRACTION -> "SHUTTER (1/x)"
                            ShutterMode.SECONDS -> "SHUTTER (sec)"
                            ShutterMode.MINUTES -> "SHUTTER (min)"
                        }
                        SettingCard(
                            label = shutterLabel,
                            value = shutterDisplay,
                            onSelect = { showSheet = "SHUTTER" },
                            onValueChange = { viewModel.updateState { s -> s.copy(shutterValue = it) } },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        SettingCard(
                            label = "FOCAL",
                            value = "${uiState.currentFocal}mm",
                            onSelect = { showSheet = "FOCAL" },
                            onValueChange = { viewModel.updateState { s -> s.copy(currentFocal = it) } },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 03 ND FILTER
            item {
                FieldSection(title = "03 ND FILTER") {
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        uiState.ndFilters.sorted().forEach { stop ->
                            NdFilterChip(stop = stop, onRemove = { viewModel.toggleNdFilter(stop) })
                        }
                        
                        // ADD ND Button
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(18.dp))
                                .border(1.dp, TextDim.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                                .clickable { showSheet = "ND" }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+ ADD ND", color = TextDim, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            // 04 OPTICAL RESULT (Orange Box)
            item {
                FieldSection(title = "04 OPTICAL RESULT", isEmphasized = true) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ResultItem(
                            label = "NPF TIME", 
                            value = viewModel.calculateNPF(), 
                            modifier = Modifier.weight(1f)
                        )
                        Box(modifier = Modifier.width(1.dp).height(30.dp).background(TextDim.copy(alpha = 0.3f)))
                        ResultItem(
                            label = "HYPERFOCAL", 
                            value = viewModel.calculateHyperfocal(), 
                            modifier = Modifier.weight(1f)
                        )
                        Box(modifier = Modifier.width(1.dp).height(30.dp).background(TextDim.copy(alpha = 0.3f)))
                        ResultItem(
                            label = "ND EXP", 
                            value = viewModel.calculateNdExposure(), 
                            isTinted = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Bottom Annotations
            item {
                Column(modifier = Modifier.padding(top = 10.dp, start = 4.dp)) {
                    val npfDesc = if (isKo) "별이 흐르지 않고 점으로 찍히는 최대 노출 시간" else "Max exposure time for pinpoint stars without trailing"
                    val hpfDesc = if (isKo) "어디에 초점을 맞춰도 배경까지 다 선명하게 나오는 거리" else "Focus distance that keeps the background perfectly sharp"
                    val ndDesc = if (isKo) "필터 사용 시 최종 노출 시간" else "Final exposure time with ND filter applied"

                    Text("• NPF TIME: $npfDesc", color = TextDim, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• HYPERFOCAL: $hpfDesc", color = TextDim, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• ND EXP: $ndDesc", color = TextDim, fontSize = 12.sp)
                }
            }
        }

        // BottomSheet Logic
        if (showSheet != null) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = null },
                sheetState = sheetState,
                containerColor = CardBg,
                dragHandle = { BottomSheetDefaults.DragHandle(color = TextDim.copy(alpha = 0.4f)) }
            ) {
                SheetContent(
                    type = showSheet!!,
                    uiState = uiState,
                    inventoryList = inventoryList,
                    onSelectGear = { item -> 
                        viewModel.selectGear(item)
                        showSheet = null
                    },
                    onToggleNd = { viewModel.toggleNdFilter(it) },
                    onUpdateState = { viewModel.updateState(it) },
                    onClose = { showSheet = null }
                )
            }
        }
    }
}

@Composable
fun SheetContent(
    type: String,
    uiState: FieldState,
    inventoryList: List<InventoryItem>,
    onSelectGear: (InventoryItem) -> Unit,
    onToggleNd: (Int) -> Unit,
    onUpdateState: ((FieldState) -> FieldState) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 40.dp)
    ) {
        val title = when (type) {
            "GEAR_CAM" -> "Select Camera"
            "GEAR_LENS" -> "Select Lens"
            "APERTURE" -> "Aperture"
            "ISO" -> "ISO"
            "SHUTTER" -> "Shutter Speed"
            "FOCAL" -> "Focal Length"
            "ND" -> "Add ND Filter"
            else -> ""
        }

        Text(
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        when (type) {
            "GEAR_CAM", "GEAR_LENS" -> {
                val category = if (type == "GEAR_CAM") InventoryCategory.CAMERA else InventoryCategory.LENS
                val items = inventoryList.filter { it.category == category }
                if (items.isEmpty()) {
                    Text("No items found in inventory.", color = TextDim, modifier = Modifier.padding(20.dp))
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp) // 높이 제한을 두어 스크롤 활성화
                    ) {
                        items(items) { item ->
                            InventoryItemRow(
                                item = item,
                                isSelected = (if (category == InventoryCategory.CAMERA) uiState.selectedCameraId else uiState.selectedLensId) == item.id,
                                onClick = { onSelectGear(item) }
                            )
                        }
                    }
                }
            }
            "APERTURE", "ISO", "FOCAL" -> {
                val options = when (type) {
                    "APERTURE" -> FieldConstants.APERTURE_STOPS
                    "ISO" -> FieldConstants.ISO_STOPS
                    else -> FieldConstants.FOCAL_STOPS
                }
                val currentVal = when (type) {
                    "APERTURE" -> uiState.aperture
                    "ISO" -> uiState.iso
                    else -> uiState.currentFocal
                }
                
                var inputText by remember { mutableStateOf("") } // 초기값 빈칸으로 변경

                Column {
                    // 상단 직접 입력 필드
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        textStyle = TextStyle(color = Orange, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        placeholder = { Text("Enter value (Current: $currentVal)", color = TextDim) }, // 현재값 힌트로 표시
                        trailingIcon = {
                            Text(
                                "APPLY", 
                                color = Orange, 
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.clickable {
                                    if (inputText.isNotEmpty()) {
                                        onUpdateState { s ->
                                            when (type) {
                                                "APERTURE" -> s.copy(aperture = inputText)
                                                "ISO" -> s.copy(iso = inputText)
                                                else -> s.copy(currentFocal = inputText)
                                            }
                                        }
                                        onClose()
                                    }
                                }.padding(8.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (inputText.isNotEmpty()) {
                                onUpdateState { s ->
                                    when (type) {
                                        "APERTURE" -> s.copy(aperture = inputText)
                                        "ISO" -> s.copy(iso = inputText)
                                        else -> s.copy(currentFocal = inputText)
                                    }
                                }
                                onClose()
                            }
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Orange,
                            unfocusedBorderColor = TextDim.copy(alpha = 0.3f),
                            cursorColor = Orange
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp)
                    ) {
                        items(options) { opt ->
                            GridItem(
                                text = opt,
                                isSelected = opt == currentVal,
                                onClick = {
                                    onUpdateState { s ->
                                        when (type) {
                                            "APERTURE" -> s.copy(aperture = opt)
                                            "ISO" -> s.copy(iso = opt)
                                            else -> s.copy(currentFocal = opt)
                                        }
                                    }
                                    onClose()
                                }
                            )
                        }
                    }
                }
            }
            "SHUTTER" -> {
                Column {
                    var inputVal by remember { mutableStateOf("") } // 초기값 빈칸
                    var currentMode by remember { mutableStateOf(uiState.shutterMode) } // 모드 상태를 상위로 올림
                    
                    OutlinedTextField(
                        value = inputVal,
                        onValueChange = { inputVal = it },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        textStyle = TextStyle(color = Orange, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        placeholder = { Text("Enter speed (Current: ${uiState.shutterValue})", color = TextDim) },
                        trailingIcon = {
                            Text(
                                "APPLY", 
                                color = Orange, 
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.clickable {
                                    if (inputVal.isNotEmpty()) {
                                        onUpdateState { it.copy(shutterValue = inputVal, shutterMode = currentMode) }
                                        onClose()
                                    }
                                }.padding(8.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (inputVal.isNotEmpty()) {
                                onUpdateState { it.copy(shutterValue = inputVal, shutterMode = currentMode) }
                                onClose()
                            }
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Orange,
                            unfocusedBorderColor = TextDim.copy(alpha = 0.3f),
                            cursorColor = Orange
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // ShutterEditor를 내부 탭과 그리드로 분리하여 상태 연동
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp))
                                .padding(4.dp)
                        ) {
                            ShutterModeTab(
                                text = "1/x",
                                isSelected = currentMode == ShutterMode.FRACTION,
                                onClick = { currentMode = ShutterMode.FRACTION },
                                modifier = Modifier.weight(1f)
                            )
                            ShutterModeTab(
                                text = "sec",
                                isSelected = currentMode == ShutterMode.SECONDS,
                                onClick = { currentMode = ShutterMode.SECONDS },
                                modifier = Modifier.weight(1f)
                            )
                            ShutterModeTab(
                                text = "min",
                                isSelected = currentMode == ShutterMode.MINUTES,
                                onClick = { currentMode = ShutterMode.MINUTES },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        val options = if (currentMode == ShutterMode.FRACTION) FieldConstants.SHUTTER_FRACTION_STOPS else FieldConstants.SHUTTER_TIME_STOPS
                        
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 300.dp)
                        ) {
                            items(options) { opt ->
                                GridItem(
                                    text = opt,
                                    isSelected = opt == uiState.shutterValue && currentMode == uiState.shutterMode,
                                    onClick = { 
                                        onUpdateState { it.copy(shutterValue = opt, shutterMode = currentMode) }
                                        onClose()
                                    }
                                )
                            }
                        }
                    }
                }
            }
            "ND" -> {
                NdGrid(
                    selectedFilters = uiState.ndFilters,
                    onToggle = onToggleNd,
                    onClose = onClose
                )
            }
        }
    }
}

@Composable
fun NdGrid(
    selectedFilters: List<Int>,
    onToggle: (Int) -> Unit,
    onClose: () -> Unit
) {
    Column {
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            (1..10).forEach { stop ->
                val isSelected = selectedFilters.contains(stop)
                val ndValue = 2.0.pow(stop.toDouble()).toInt()
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.48f)
                        .height(70.dp)
                        .background(
                            if (isSelected) AccentBlue else Color.White.copy(alpha = 0.05f), 
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp, 
                            if (isSelected) AccentBlue else TextDim.copy(alpha = 0.2f), 
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onToggle(stop) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$stop STOP", 
                            color = if (isSelected) DarkBg else TextDim, 
                            fontSize = 11.sp, 
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ND$ndValue", 
                            color = if (isSelected) DarkBg else Color.White, 
                            fontSize = 18.sp, 
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(30.dp))
        
        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
        ) {
            Text("APPLY", color = DarkBg, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun ShutterEditor(
    value: String,
    mode: ShutterMode,
    onApply: (String, ShutterMode) -> Unit
) {
    var selectedMode by remember { mutableStateOf(mode) }
    var selectedValue by remember { mutableStateOf(value) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            ShutterModeTab(
                text = "1/x",
                isSelected = selectedMode == ShutterMode.FRACTION,
                onClick = { selectedMode = ShutterMode.FRACTION; selectedValue = "250" },
                modifier = Modifier.weight(1f)
            )
            ShutterModeTab(
                text = "sec",
                isSelected = selectedMode == ShutterMode.SECONDS,
                onClick = { selectedMode = ShutterMode.SECONDS; selectedValue = "1" },
                modifier = Modifier.weight(1f)
            )
            ShutterModeTab(
                text = "min",
                isSelected = selectedMode == ShutterMode.MINUTES,
                onClick = { selectedMode = ShutterMode.MINUTES; selectedValue = "1" },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        val options = if (selectedMode == ShutterMode.FRACTION) FieldConstants.SHUTTER_FRACTION_STOPS else FieldConstants.SHUTTER_TIME_STOPS
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 300.dp)
        ) {
            items(options) { opt ->
                GridItem(
                    text = opt,
                    isSelected = opt == selectedValue,
                    onClick = { onApply(opt, selectedMode) }
                )
            }
        }
    }
}

@Composable
fun ShutterModeTab(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .height(36.dp)
            .background(color = if (isSelected) Orange else Color.Transparent, shape = RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = if (isSelected) DarkBg else Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GridItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(50.dp)
            .background(color = if (isSelected) Orange.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.03f), shape = RoundedCornerShape(12.dp))
            .border(1.dp, if (isSelected) Orange else TextDim.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = if (isSelected) Orange else Color.White, fontWeight = FontWeight.Black)
    }
}

@Composable
fun InventoryItemRow(item: InventoryItem, isSelected: Boolean, onClick: () -> Unit) {
    val accent = if (item.category == InventoryCategory.CAMERA) Orange else AccentBlue
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp))
            .border(1.dp, if (isSelected) accent else TextDim.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "${item.brand} ${item.model ?: ""}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
            Text(
                text = if (item.category == InventoryCategory.CAMERA) "${item.sensor?.displayName} · ${item.megapixels}MP" else "${item.lensType?.displayName} · ${item.minFocal}-${item.maxFocal}mm",
                color = TextDim,
                fontSize = 12.sp
            )
        }
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(accent, RoundedCornerShape(4.dp))
        )
    }
}

private fun Double.pow(n: Double): Double = Math.pow(this, n)
