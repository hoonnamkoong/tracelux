package com.tracelux

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.tracelux.ui.navigation.MainAppStructure
import com.tracelux.ui.theme.TraceluxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TraceluxTheme {
                val navController = rememberNavController()
                
                // 위치 및 카메라 권한 요청 핸들러
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    // 권한 결과 처리 (필요시 상세 로직 추가 가능)
                }

                // 앱 구동 시 초기 권한 체크
                LaunchedEffect(Unit) {
                    val permissions = arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.CAMERA
                    )
                    if (permissions.any { ContextCompat.checkSelfPermission(this@MainActivity, it) != PackageManager.PERMISSION_GRANTED }) {
                        permissionLauncher.launch(permissions)
                    }
                }

                // 모듈화된 메인 앱 구조 호출
                MainAppStructure(navController = navController)
            }
        }
    }
}
