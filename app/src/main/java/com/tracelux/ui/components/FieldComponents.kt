package com.tracelux.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracelux.ui.theme.*
import kotlin.math.pow

/**
 * 섹션 컨테이너 (번호 포함: 01 ACTIVE GEAR 등)
 */
@Composable
fun FieldSection(
    title: String,
    modifier: Modifier = Modifier,
    isEmphasized: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val borderColor = if (isEmphasized) Orange else TextDim.copy(alpha = 0.3f)
    val bgColor = if (isEmphasized) Orange.copy(alpha = 0.05f) else CardBg

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .background(color = bgColor, shape = RoundedCornerShape(20.dp))
            .border(if (isEmphasized) 2.dp else 1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            color = if (isEmphasized) Orange else TextDim,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

/**
 * 장비 카드 (카메라/렌즈 상단 표시용)
 */
@Composable
fun GearCard(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(70.dp)
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

/**
 * 설정 아이템 카드 (짧게 클릭: 선택 시트 / 길게 클릭: 직접 입력)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingCard(
    label: String,
    value: String,
    onSelect: () -> Unit,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 64.dp
) {
    var isEditing by remember { mutableStateOf(false) }
    var editValue by remember { mutableStateOf(value.replace("f/", "").replace("mm", "")) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isEditing) {
        if (isEditing) {
            editValue = value.replace("f/", "").replace("mm", "")
            focusRequester.requestFocus()
        }
    }

    Column(
        modifier = modifier
            .height(height)
            .background(color = Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp))
            .border(1.dp, TextDim.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = { if (!isEditing) onSelect() },
                onLongClick = { isEditing = true }
            )
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = TextDim,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        
        if (isEditing) {
            BasicTextField(
                value = editValue,
                onValueChange = { editValue = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                textStyle = TextStyle(
                    color = Orange,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(Orange),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        isEditing = false
                        onValueChange(editValue)
                        focusManager.clearFocus()
                    }
                ),
                singleLine = true
            )
        } else {
            Text(
                text = value,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 결과 표시 아이템 (NPF, Hyperfocal 등)
 */
@Composable
fun ResultItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isTinted: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = TextDim,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = value,
            color = if (isTinted) AccentBlue else Orange,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black
        )
    }
}

/**
 * 선택된 ND 필터 칩
 */
@Composable
fun NdFilterChip(
    stop: Int,
    onRemove: () -> Unit
) {
    val ndValue = 2.0.pow(stop.toDouble()).toInt()
    Surface(
        onClick = onRemove,
        shape = RoundedCornerShape(16.dp),
        color = Orange.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Orange)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ND$ndValue ✕",
                color = Orange,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

private fun Double.pow(n: Double): Double = Math.pow(this, n)
