package com.tracelux.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.List

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tracelux.models.AppLanguage
import com.tracelux.models.AppUnit
import com.tracelux.ui.theme.DarkBg
import com.tracelux.ui.theme.Orange
import com.tracelux.ui.theme.TextDim
import com.tracelux.ui.viewmodel.OptionsViewModel

@Composable
fun OptionsScreen(
    viewModel: OptionsViewModel = viewModel()
) {
    val options by viewModel.options.collectAsState()
    val isKo = options.language == AppLanguage.KO
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "OPTIONS",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 40.dp, bottom = 30.dp)
            )

            // Language Section
            OptionSection("LANGUAGE", Icons.Default.Settings) {
                ToggleRow {
                    ToggleItem(
                        text = "한국어",
                        isSelected = options.language == AppLanguage.KO,
                        onClick = { viewModel.setLanguage(AppLanguage.KO) },
                        modifier = Modifier.weight(1f)
                    )
                    ToggleItem(
                        text = "English",
                        isSelected = options.language == AppLanguage.EN,
                        onClick = { viewModel.setLanguage(AppLanguage.EN) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Units Section
            OptionSection("UNITS", Icons.Default.List) {
                ToggleRow {
                    ToggleItem(
                        text = "Metric (m)",
                        isSelected = options.unit == AppUnit.METRIC,
                        onClick = { viewModel.setUnit(AppUnit.METRIC) },
                        modifier = Modifier.weight(1f)
                    )
                    ToggleItem(
                        text = "Imperial (ft)",
                        isSelected = options.unit == AppUnit.IMPERIAL,
                        onClick = { viewModel.setUnit(AppUnit.IMPERIAL) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Data Management Section
            OptionSection("DATA Management", Icons.Default.Delete) {
                Button(
                    onClick = { showResetDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("RESET ALL DATA", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }

            // Info Section
            OptionSection("SUPPORT & INFO", Icons.Default.Info) {
                InfoRow(
                    label = if (options.language == AppLanguage.KO) "애플리케이션 버전" else "Application Version",
                    value = "v${viewModel.getAppVersion()}"
                )
            }

            Text(
                text = "© 2026 Tracelux.\nDesigned for creators who chase the light.",
                color = TextDim,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            )

            Spacer(modifier = Modifier.height(100.dp))
        }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text(if (isKo) "데이터 초기화" else "Reset All Data", color = Color.White) },
                text = { Text(if (isKo) "모든 데이터를 정말로 초기화하시겠습니까? 인벤토리와 모든 설정이 삭제됩니다." else "Are you sure you want to reset all data? This will clear your inventory and all settings.", color = TextDim) },
                confirmButton = {
                    TextButton(onClick = {
                        showResetDialog = false
                        viewModel.resetAllData(context)
                    }) {
                        Text(if (isKo) "초기화" else "RESET", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text(if (isKo) "취소" else "CANCEL", color = Color.White)
                    }
                },
                containerColor = Color(0xFF1E293B)
            )
        }
    }
}

@Composable
fun OptionSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 30.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Orange,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = Orange,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
        content()
    }
}

@Composable
fun ToggleRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top,
        content = content
    )
}

@Composable
fun ToggleItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) Orange.copy(alpha = 0.1f) else Color.Transparent
    val borderColor = if (isSelected) Orange else Color.Transparent
    val textColor = if (isSelected) Orange else Color.White

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(42.dp)
            .background(bgColor, RoundedCornerShape(10.dp))
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextDim, fontSize = 14.sp)
        Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
