package com.tracelux.ar

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import kotlin.math.*
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.ar.core.CameraConfig
import com.google.ar.core.CameraConfigFilter
import com.google.ar.core.Config
import com.tracelux.utils.SolarCalculator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import io.github.sceneview.ar.ArSceneView
import java.util.*

private const val TAG = "ArSimulatorScreen"

@Composable
fun ArSimulatorScreen(
    selectedFocalLength: Float = 50f,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val view = LocalView.current

    // ─── 상태 관리 ──────────────────────────────────────────────────────────
    var azimuth by remember { mutableFloatStateOf(0f) }
    var rawAzimuth by remember { mutableFloatStateOf(0f) }
    var pitch by remember { mutableFloatStateOf(0f) }
    var isEcoMode by remember { mutableStateOf(false) }
    var thermalStatus by remember { mutableIntStateOf(0) }
    // 원본 APK: 회전 행렬 상태 (센서 콜백에서 업데이트)
    var rotationMatrix by remember { mutableStateOf(FloatArray(9)) }
    // 자기 편각 (GeomagneticField로 계산)
    var magneticDeclination by remember { mutableFloatStateOf(0f) }
    // 원본 APK: 발열 상태에 따른 FPS (4 이상이면 15fps, 아니면 30fps)
    var currentFps by remember { mutableIntStateOf(30) }

    val fovX by remember { mutableFloatStateOf(ArMathUtils.calculateDeviceHorizontalFov(context)) }
    val fovY by remember { mutableFloatStateOf(ArMathUtils.calculateDeviceVerticalFov(context)) }

    val lat by remember { mutableDoubleStateOf(37.5665) }
    val lng by remember { mutableDoubleStateOf(126.9780) }

    var timeOffset by remember {
        mutableFloatStateOf(
            Calendar.getInstance().get(Calendar.HOUR_OF_DAY) +
            Calendar.getInstance().get(Calendar.MINUTE) / 60f
        )
    }

    LaunchedEffect(lat, lng) {
        try {
            val geoField = android.hardware.GeomagneticField(
                lat.toFloat(),
                lng.toFloat(),
                0f,
                System.currentTimeMillis()
            )
            // 지자기 편각 (Declination). 진북(True North)과 자북(Magnetic North)의 차이입니다.
            // 동쪽 편각은 양수, 서쪽 편각은 음수입니다. 한국은 보통 서편각(-8도 등)입니다.
            magneticDeclination = geoField.declination
        } catch (e: Exception) {
            Log.w(TAG, "Failed to calculate GeomagneticField: ${e.message}")
        }
    }

    // 수동 보정값 (사용자가 화면 드래그로 미세 조정)
    var manualAzimuthOffset by remember { mutableFloatStateOf(0f) }

    // 원본 APK: Map<String, Boolean>으로 토글 상태 관리 (ROTATE 포함)
    var toggles by remember {
        mutableStateOf(mapOf("SUN" to false, "MOON" to false, "STAR" to false, "MW" to false, "FOV" to false))
    }

    val filterAlpha = 0.12f
    var arSceneViewRef by remember { mutableStateOf<ArSceneView?>(null) }

    // ─── 네비게이션 바 숨김 ────────────────────────────────────────────────
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val window = activity?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, view)
            controller.hide(WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        onDispose {
            window?.let { WindowCompat.getInsetsController(it, view).show(WindowInsetsCompat.Type.navigationBars()) }
        }
    }

    // ─── 화면 종료 시 세로 모드 복구 ──────────────────────────────────────
    DisposableEffect(context) {
        onDispose {
            val activity = context as? Activity
            activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    // ─── 원본 APK: 발열 상태 리스너 + FPS 동적 변경 ──────────────────────
    DisposableEffect(Unit) {
        val pm = context.getSystemService("power") as? PowerManager
        val listener = if (Build.VERSION.SDK_INT >= 29) {
            PowerManager.OnThermalStatusChangedListener { status ->
                thermalStatus = status
                currentFps = if (status >= 4) 15 else 30
            }
        } else null
        if (Build.VERSION.SDK_INT >= 29 && pm != null && listener != null) {
            pm.addThermalStatusListener(listener)
        }
        onDispose {
            if (Build.VERSION.SDK_INT >= 29 && pm != null && listener != null) {
                pm.removeThermalStatusListener(listener)
            }
        }
    }

    // ─── 센서 리스너 (회전 행렬 상태 업데이트 포함) ───────────────────────
    DisposableEffect(Unit) {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    try {
                        @Suppress("DEPRECATION")
                        val displayRotation = windowManager.defaultDisplay.rotation
                        
                        // 1. 센서에서 원본 회전 행렬 추출
                        val rawMatrix = FloatArray(9)
                        SensorManager.getRotationMatrixFromVector(rawMatrix, event.values)
                        
                        // 2. 자기 편각 보정 (자북 -> 진북 회전)
                        // 행렬 자체를 진북 기준으로 회전시켜 모든 후속 계산이 진북 기반이 되게 함
                        val trueMatrix = ArMathUtils.rotateMatrixZ(rawMatrix, magneticDeclination)
                        
                        // 3. 화면 회전에 따른 좌표계 재매핑 (보정된 행렬 사용)
                        val remappedMatrix = FloatArray(9)
                        when (displayRotation) {
                            android.view.Surface.ROTATION_90 -> SensorManager.remapCoordinateSystem(trueMatrix, 2, 129, remappedMatrix)
                            android.view.Surface.ROTATION_180 -> SensorManager.remapCoordinateSystem(trueMatrix, 129, 130, remappedMatrix)
                            android.view.Surface.ROTATION_270 -> SensorManager.remapCoordinateSystem(trueMatrix, 130, 1, remappedMatrix)
                            else -> System.arraycopy(trueMatrix, 0, remappedMatrix, 0, 9)
                        }
                        
                        // 4. 오리엔테이션 각도 추출 (이미 진북 보정됨)
                        // ArMathUtils.extractOrientationAngles 내부 로직 중 remap 부분을 건너뛰고 각도만 계산하기 위해 
                        // 커스텀 수식 직접 적용 (또는 extractOrientationAngles를 수정해서 사용 가능하지만 여기선 직접 계산)
                        val cx = -remappedMatrix[2]
                        val cy = -remappedMatrix[5]
                        val cz = -remappedMatrix[8]
                        
                        val pitchDeg = Math.toDegrees(asin(cz.coerceIn(-1f, 1f).toDouble())).toFloat()
                        val newRawAzimuth = (Math.toDegrees(atan2(cx.toDouble(), cy.toDouble())).toFloat() + 360.0f) % 360.0f
                        
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(remappedMatrix, orientation)
                        // rollDeg는 orientation[2]

                        // Low-Pass Filter 적용
                        var diff = newRawAzimuth - rawAzimuth
                        if (diff > 180f) diff -= 360f
                        if (diff < -180f) diff += 360f
                        rawAzimuth = ((rawAzimuth + filterAlpha * diff) + 360f) % 360f
                        azimuth = (rawAzimuth + manualAzimuthOffset + 360f) % 360f // 수동 보정값 반영

                        pitch = pitch + filterAlpha * (pitchDeg - pitch)

                        rotationMatrix = remappedMatrix.copyOf()
                        isEcoMode = pitch < -70f
                    } catch (e: Exception) {
                        Log.w(TAG, "Sensor error: ${e.message}")
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(sensorListener, rotationSensor, SensorManager.SENSOR_DELAY_GAME)
        onDispose { sensorManager.unregisterListener(sensorListener) }
    }

    // ─── 생명주기 관찰자 ──────────────────────────────────────────────────
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            val arView = arSceneViewRef ?: return@LifecycleEventObserver
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (!isEcoMode) try { arView.arSession?.resume() } catch (e: Exception) {}
                }
                Lifecycle.Event.ON_PAUSE -> try { arView.arSession?.pause() } catch (e: Exception) {}
                Lifecycle.Event.ON_DESTROY -> try { arView.destroy() } catch (e: Exception) {}
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // ─── 천체 마커 계산 ───────────────────────────────────────────────────
    val activeMarkers = remember(timeOffset, lat, lng, toggles) {
        try {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, timeOffset.toInt())
            cal.set(Calendar.MINUTE, ((timeOffset % 1) * 60).toInt())
            val simDate = cal.time
            val moonIllum = SolarCalculator.getMoonIllumination(simDate, lat, lng)
            buildList {
                if (toggles["SUN"] == true) {
                    val pos = SolarCalculator.getSunPosition(simDate, lat, lng)
                add(CelestialMarker("SUN", pos.azimuth.toFloat(), pos.altitude.toFloat(), Icons.Filled.Star, Color(0xFFFF8000)))
                }
                if (toggles["MOON"] == true) {
                    val pos = SolarCalculator.getMoonPosition(simDate, lat, lng)
                add(CelestialMarker("MOON", pos.azimuth.toFloat(), pos.altitude.toFloat(), Icons.Filled.Star, Color(0xFFFFE000), phase = moonIllum.fraction))
                }
                if (toggles["STAR"] == true) {
                    val pos = SolarCalculator.getPolarisPosition(simDate, lat, lng)
                add(CelestialMarker("POLARIS", pos.azimuth.toFloat(), pos.altitude.toFloat(), Icons.Filled.Star, Color.White))
                }
                if (toggles["MW"] == true) {
                    val pos = SolarCalculator.getGalacticCenterPosition(simDate, lat, lng)
                add(CelestialMarker("MILKY WAY", pos.azimuth.toFloat(), pos.altitude.toFloat(), Icons.Filled.Star, Color.Cyan))
                }
            }
        } catch (e: Exception) { emptyList() }
    }

    // FOV 비율 계산 (FovFrame용, 원본 APK 수학 수식 적용)
    val fovRatio = ArMathUtils.calculateFovFrameRatio(selectedFocalLength, fovX)
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    // ─── UI 레이아웃 ──────────────────────────────────────────────────────
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val dialogView = LocalView.current
        DisposableEffect(dialogView) {
            val window = (dialogView.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window
                ?: (dialogView.context as? android.app.Activity)?.window
            if (window != null) {
                window.setLayout(
                    android.view.WindowManager.LayoutParams.MATCH_PARENT,
                    android.view.WindowManager.LayoutParams.MATCH_PARENT
                )
                window.addFlags(android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    window.attributes.layoutInDisplayCutoutMode = android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
                WindowCompat.setDecorFitsSystemWindows(window, false)
                WindowInsetsControllerCompat(window, dialogView).apply {
                    hide(WindowInsetsCompat.Type.systemBars())
                    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }
            onDispose {}
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        // 드래그로 방위각 미세 보정 (감도 조절)
                        manualAzimuthOffset = (manualAzimuthOffset - dragAmount / 10f)
                    }
                }
        ) {
            // AR 카메라 뷰
        AndroidView(
            factory = { ctx ->
                ArSceneView(ctx).also { arView ->
                    arSceneViewRef = arView
                    try {
                        arView.planeRenderer.isVisible = false
                    } catch (e: Exception) {}
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { arView ->
                val session = arView.arSession
                val config = session?.config
                if (session != null && config != null) {
                    try {
                        config.planeFindingMode = Config.PlaneFindingMode.DISABLED
                        config.depthMode = Config.DepthMode.DISABLED
                        config.lightEstimationMode = Config.LightEstimationMode.DISABLED
                        config.focusMode = Config.FocusMode.AUTO

                        // 원본 APK: CameraConfig 30fps 필터 적용
                        val filter = CameraConfigFilter(session)
                        filter.targetFps = EnumSet.of(CameraConfig.TargetFps.TARGET_FPS_30)
                        val configs = session.getSupportedCameraConfigs(filter)
                        val best = configs.maxByOrNull { it.imageSize.width * it.imageSize.height }
                            ?: configs.firstOrNull()
                        if (best != null && session.cameraConfig != best) {
                            session.cameraConfig = best
                        }
                        session.configure(config)

                        // 원본 APK: Reflection으로 setMaxFramesPerSeconds 적용
                        try {
                            val m = arView.javaClass.getMethod("setMaxFramesPerSeconds", Int::class.javaPrimitiveType)
                            m.invoke(arView, currentFps)
                        } catch (e: Exception) { /* 지원하지 않는 버전 무시 */ }

                        // 원본 APK: 에코 모드 처리
                        if (isEcoMode) {
                            session.pause()
                            arView.visibility = android.view.View.GONE
                        } else {
                            arView.visibility = android.view.View.VISIBLE
                            session.resume()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "AR Update Fail: ${e.message}")
                    }
                }
            }
        )

        // ─── HUD 레이어들 ───────────────────────────────────────────────
        // 나침반 + 지평선
        ArHorizonCompass(
            azimuth = azimuth,
            direction = getDirectionString(azimuth),
            pitch = pitch,
            fovY = fovY
        )

        // 천체 오버레이 (회전 행렬 기반 3D 투영)
        ArCelestialOverlay(
            markers = activeMarkers,
            rotationMatrix = rotationMatrix,
            magneticDeclination = manualAzimuthOffset, // 수동 보정값을 전달
            hFov = fovX,
            vFov = fovY
        )

        // FOV 프레임 (초점거리 시각화)
        if (toggles["FOV"] == true) {
            FovFrame(
                mm = selectedFocalLength.toInt(),
                ratio = fovRatio,
                isWarning = thermalStatus >= 4
            )
        }

        // 기울기 스케일 (좌측 고정, 가로 모드 시 약간 위로 이동)
        Box(
            modifier = Modifier.fillMaxSize().padding(top = if(isLandscape) 40.dp else 120.dp), 
            contentAlignment = Alignment.TopStart
        ) {
            ArTiltScale(pitch = pitch)
        }
        
        // 시간 시뮬레이션 스케일 (우측 고정, 가로 모드 시 약간 위로 이동)
        Box(
            modifier = Modifier.fillMaxSize().padding(top = if(isLandscape) 40.dp else 120.dp), 
            contentAlignment = Alignment.TopEnd
        ) {
            ArTimeSimulationScale(timeOffset = timeOffset, onTimeChange = { timeOffset = it })
        }



        // 하단 컨트롤 영역
        if (isLandscape) {
            Box(modifier = Modifier.fillMaxSize().padding(bottom = 16.dp, start = 16.dp, end = 16.dp), contentAlignment = Alignment.BottomCenter) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    ArObjectToggles(
                        toggles = toggles,
                        onToggle = { key ->
                            toggles = toggles.toMutableMap().also { it[key] = !(it[key] ?: true) }
                        },
                        onRotate = {
                            val activity = context as? Activity
                            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        }
                    )
                    
                    Button(
                        onClick = onClose,
                        modifier = Modifier
                            .width(200.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
                    ) {
                        Text("CLOSE VIEWER", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.weight(1f))

                ArObjectToggles(
                    toggles = toggles,
                    onToggle = { key ->
                        toggles = toggles.toMutableMap().also { it[key] = !(it[key] ?: true) }
                    },
                    onRotate = {
                        val activity = context as? Activity
                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }
                )

                Button(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 48.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
                ) {
                    Text("CLOSE AR VIEWER", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 에코 모드 오버레이
        if (isEcoMode) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)), contentAlignment = Alignment.Center) {
                Text("ECO MODE — TILT UP TO RESUME", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
            }
    }
}
