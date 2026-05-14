package com.tracelux.ui.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tracelux.ui.screens.SkyScreen
import com.tracelux.ui.screens.FieldScreen
import com.tracelux.ui.screens.InventoryScreen
import com.tracelux.ui.theme.Orange
import com.tracelux.ui.theme.DarkBg
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainAppStructure(navController: NavHostController) {
    // Screen.kt에 정의된 bottomNavItems 사용
    val pagerState = rememberPagerState(pageCount = { bottomNavItems.size })
    val scope = rememberCoroutineScope()
    
    Scaffold(
        containerColor = DarkBg,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBg)
                    .windowInsetsPadding(WindowInsets.navigationBars) // 시스템 네비게이션 바 영역 확보
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomNavItems.forEachIndexed { index, screen ->
                    val isSelected = pagerState.currentPage == index
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { 
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                    ) {
                        Text(
                            text = screen.title,
                            color = if (isSelected) Orange else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        // 하단 인디케이터 (텍스트 아래로 이동)
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isSelected) Orange else Color.Transparent)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) { page ->
            when (page) {
                0 -> SkyScreen()
                1 -> FieldScreen()
                2 -> InventoryScreen()
                3 -> com.tracelux.ui.screens.OptionsScreen()
            }
        }
    }
}
