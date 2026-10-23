package dev.daika.davy.core.common.preference

import android.app.UiModeManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("davy_mode_prefs", Context.MODE_PRIVATE)

    private val _currentMode = MutableStateFlow(getSavedMode())
    val currentMode: StateFlow<AppMode?> = _currentMode.asStateFlow()

    fun getSavedMode(): AppMode? {
        val saved = prefs.getString(KEY_APP_MODE, null) ?: return null
        return try {
            AppMode.valueOf(saved)
        } catch (e: Exception) {
            null
        }
    }

    fun setAppMode(mode: AppMode) {
        prefs.edit().apply {
            putString(KEY_APP_MODE, mode.name)
            apply()
        }
        _currentMode.value = mode
    }

    fun clearSavedMode() {
        prefs.edit().apply {
            remove(KEY_APP_MODE)
            apply()
        }
        _currentMode.value = null
    }

    fun detectDefaultDeviceMode(): AppMode {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
        val isTelevision = uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
        val hasTouchscreen =
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)
        val hasLeanback =
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)

        return if (isTelevision || hasLeanback || !hasTouchscreen) {
            AppMode.TV
        } else {
            AppMode.MOBILE
        }
    }

    companion object {
        private const val KEY_APP_MODE = "key_app_mode"
    }
}
