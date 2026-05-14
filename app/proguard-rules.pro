# ============================================================
# Tracelux ProGuard / R8 규칙
# 코드 난독화 시 필요한 예외 규칙을 정의합니다.
# ============================================================

# --- R8 누락 클래스 무시 (findbugs 어노테이션, AR Core desugaring) ---
-dontwarn edu.umd.cs.findbugs.annotations.**
-dontwarn j$.util.function.**
-dontwarn javax.annotation.**
-dontwarn org.checkerframework.**
-dontwarn com.google.errorprone.**
-dontwarn sun.misc.Unsafe
# ============================================================

# --- Kotlin Serialization ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *; }
-keep,includedescriptorclasses class com.tracelux.**$$serializer { *; }
-keepclassmembers class com.tracelux.** {
    *** Companion;
}
-keepclasseswithmembers class com.tracelux.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class com.tl.** { *; }

# --- Retrofit / OkHttp ---
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# --- Gson ---
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# --- Data classes (API 모델) ---
-keep class com.tracelux.data.** { *; }
-keep class com.tl.data.** { *; }

# --- Compose ---
-dontwarn androidx.compose.**

# --- CameraX ---
-keep class androidx.camera.** { *; }

# --- SceneView / AR ---
-keep class io.github.sceneview.** { *; }
-dontwarn io.github.sceneview.**

# --- commons-suncalc (천문 계산) ---
-keep class org.shredzone.commons.suncalc.** { *; }

# --- Timber 로깅 (릴리스에서 로그 제거) ---
-assumenosideeffects class timber.log.Timber* {
    public static void v(...);
    public static void d(...);
    public static void i(...);
}

# --- 일반 Android ---
-keep class * extends android.app.Activity
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver
