package com.tracelux.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.tracelux.ui.screens.SkyScreen
import com.tracelux.ui.screens.FieldScreen

@Composable
fun MainAppStructure(navController: NavHostController) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                bottomNavItems.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { 
                            selectedTab = index
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        },
                        label = { Text(screen.title) },
                        icon = { /* TODO: 아이콘 추가 */ }
                    )
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
