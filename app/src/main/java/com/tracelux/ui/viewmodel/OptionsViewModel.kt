package com.tracelux.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Process
import androidx.lifecycle.AndroidViewModel
import com.tracelux.models.AppLanguage
import com.tracelux.models.AppOptions
import com.tracelux.models.AppUnit
import com.tracelux.utils.OptionsStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OptionsViewModel(application: Application) : AndroidViewModel(application) {
    private val optionsStorage = OptionsStorage(application)
    
    private val _options = MutableStateFlow(optionsStorage.loadOptions())
    val options: StateFlow<AppOptions> = _options.asStateFlow()

    fun setLanguage(language: AppLanguage) {
        val newOptions = _options.value.copy(language = language)
        _options.value = newOptions
        optionsStorage.saveOptions(newOptions)
    }

    fun setUnit(unit: AppUnit) {
        val newOptions = _options.value.copy(unit = unit)
        _options.value = newOptions
        optionsStorage.saveOptions(newOptions)
    }

    fun resetAllData(context: Context) {
        optionsStorage.clearAllData(context)
        
        // 안드로이드 권장 앱 재시작 방식
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent?.component
        val restartIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(restartIntent)
        Runtime.getRuntime().exit(0)
    }

    fun getAppVersion(): String {
        return try {
            val pInfo = getApplication<Application>().packageManager.getPackageInfo(getApplication<Application>().packageName, 0)
            pInfo.versionName ?: "1.0.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0.0"
        }
    }
}
