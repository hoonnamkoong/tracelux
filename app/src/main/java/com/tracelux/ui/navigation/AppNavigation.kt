package com.tracelux.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.navigation.compose.*
import com.tracelux.ui.screens.SkyScreen
import com.tracelux.ui.screens.FieldScreen
import com.tracelux.ui.theme.Orange
import com.tracelux.ui.theme.DarkBg

@Composable
fun MainAppStructure(navController: NavHostController) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBg)
                    .padding(bottom = 20.dp, top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomNavItems.forEachIndexed { index, screen ->
                    val isSelected = selectedTab == index
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { 
                                selectedTab = index
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                    ) {
                        // 상단 인디케이터
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isSelected) Orange else Color.Transparent)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = screen.title,
                            color = if (isSelected) Orange else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Sky.route,
        modifier = modifier
    ) {
        composable(Screen.Sky.route) { SkyScreen() }
        composable(Screen.Field.route) { FieldScreen() }
        composable(Screen.Inventory.route) { Text("Inventory Screen") }
        composable(Screen.Options.route) { Text("Options Screen") }
    }
}
