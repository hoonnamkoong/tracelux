package com.tracelux.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracelux.models.InventoryCategory
import com.tracelux.models.InventoryItem
import com.tracelux.models.LensType
import com.tracelux.ui.theme.*

/**
 * 인벤토리 아이템 카드
 */
@Composable
fun InventoryCard(
    item: InventoryItem,
    onClick: () -> Unit
) {
    val isCamera = item.category == InventoryCategory.CAMERA
    val accentColor = if (isCamera) Orange else AccentBlue

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, accentColor, RoundedCornerShape(16.dp))
                .padding(16.dp, 14.dp)
        ) {
            // 카테고리 정보
            Text(
                text = "${item.category.name} ${if (item.lensType != null) "(${item.lensType.name})" else ""}",
                color = accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            
            // 브랜드 및 모델명
            Text(
                text = "${item.brand} ${item.model ?: ""}",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 스펙 정보 행
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isCamera) {
                    SpecBox("SENSOR", item.sensor?.displayName ?: "-")
                    SpecBox("RESOLUTION", if (item.megapixels != null) "${item.megapixels} MP" else "-")
                } else {
                    if (item.lensType == LensType.PRIME) {
                        SpecBox("FOCAL LENGTH", if (item.focalLength != null) "${item.focalLength}mm" else "-")
                        SpecBox("APERTURE", if (item.aperture != null) "f/${item.aperture}" else "-")
                    } else {
                        SpecBox("FOCAL RANGE", if (item.minFocal != null && item.maxFocal != null) "${item.minFocal}-${item.maxFocal}mm" else "-")
                        SpecBox("MAX APERTURE", if (item.minAperture != null) "f/${item.minAperture}${if (item.maxAperture != null && item.minAperture != item.maxAperture) "-${item.maxAperture}" else ""}" else "-")
                    }
                }
            }
        }
    }
}

/**
 * 스펙 정보 박스 (아이템 카드 내부용)
 */
@Composable
fun RowScope.SpecBox(label: String, value: String) {
    Box(
        modifier = Modifier
            .weight(1f)
            .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(10.dp))
            .padding(vertical = 6.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                color = TextDim,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
