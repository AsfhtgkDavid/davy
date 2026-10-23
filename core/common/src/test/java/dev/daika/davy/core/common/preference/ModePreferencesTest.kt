package dev.daika.davy.core.common.preference

import android.app.UiModeManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ModePreferencesTest {

    private val context: Context = mockk()
    private val sharedPreferences: SharedPreferences = mockk()
    private val editor: SharedPreferences.Editor = mockk(relaxed = true)
    private val packageManager: PackageManager = mockk()
    private val uiModeManager: UiModeManager = mockk()

    @Before
    fun setup() {
        every { context.getSharedPreferences("davy_mode_prefs", Context.MODE_PRIVATE) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { context.packageManager } returns packageManager
        every { context.getSystemService(Context.UI_MODE_SERVICE) } returns uiModeManager
    }

    @Test
    fun `getSavedMode returns null when no preference is saved`() {
        every { sharedPreferences.getString("key_app_mode", null) } returns null

        val modePreferences = ModePreferences(context)

        assertNull(modePreferences.getSavedMode())
        assertNull(modePreferences.currentMode.value)
    }

    @Test
    fun `getSavedMode returns TV when TV is saved in preferences`() {
        every { sharedPreferences.getString("key_app_mode", null) } returns "TV"

        val modePreferences = ModePreferences(context)

        assertEquals(AppMode.TV, modePreferences.getSavedMode())
        assertEquals(AppMode.TV, modePreferences.currentMode.value)
    }

    @Test
    fun `getSavedMode returns MOBILE when MOBILE is saved in preferences`() {
        every { sharedPreferences.getString("key_app_mode", null) } returns "MOBILE"

        val modePreferences = ModePreferences(context)

        assertEquals(AppMode.MOBILE, modePreferences.getSavedMode())
        assertEquals(AppMode.MOBILE, modePreferences.currentMode.value)
    }

    @Test
    fun `getSavedMode returns null when invalid string is saved`() {
        every { sharedPreferences.getString("key_app_mode", null) } returns "INVALID_MODE"

        val modePreferences = ModePreferences(context)

        assertNull(modePreferences.getSavedMode())
    }

    @Test
    fun `setAppMode writes mode to preferences and updates currentMode`() {
        every { sharedPreferences.getString("key_app_mode", null) } returns null
        val modePreferences = ModePreferences(context)

        modePreferences.setAppMode(AppMode.TV)

        verify { editor.putString("key_app_mode", "TV") }
        verify { editor.apply() }
        assertEquals(AppMode.TV, modePreferences.currentMode.value)
    }

    @Test
    fun `clearSavedMode removes key from preferences and clears currentMode`() {
        every { sharedPreferences.getString("key_app_mode", null) } returns "TV"
        val modePreferences = ModePreferences(context)

        modePreferences.clearSavedMode()

        verify { editor.remove("key_app_mode") }
        verify { editor.apply() }
        assertNull(modePreferences.currentMode.value)
    }

    @Test
    fun `detectDefaultDeviceMode returns TV when uiMode is television`() {
        every { sharedPreferences.getString("key_app_mode", null) } returns null
        every { uiModeManager.currentModeType } returns Configuration.UI_MODE_TYPE_TELEVISION
        every { packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN) } returns true
        every { packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK) } returns false

        val modePreferences = ModePreferences(context)
        assertEquals(AppMode.TV, modePreferences.detectDefaultDeviceMode())
    }

    @Test
    fun `detectDefaultDeviceMode returns TV when device has leanback feature`() {
        every { sharedPreferences.getString("key_app_mode", null) } returns null
        every { uiModeManager.currentModeType } returns Configuration.UI_MODE_TYPE_NORMAL
        every { packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN) } returns true
        every { packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK) } returns true

        val modePreferences = ModePreferences(context)
        assertEquals(AppMode.TV, modePreferences.detectDefaultDeviceMode())
    }

    @Test
    fun `detectDefaultDeviceMode returns TV when device has no touchscreen`() {
        every { sharedPreferences.getString("key_app_mode", null) } returns null
        every { uiModeManager.currentModeType } returns Configuration.UI_MODE_TYPE_NORMAL
        every { packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN) } returns false
        every { packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK) } returns false

        val modePreferences = ModePreferences(context)
        assertEquals(AppMode.TV, modePreferences.detectDefaultDeviceMode())
    }

    @Test
    fun `detectDefaultDeviceMode returns MOBILE for regular phone with touchscreen`() {
        every { sharedPreferences.getString("key_app_mode", null) } returns null
        every { uiModeManager.currentModeType } returns Configuration.UI_MODE_TYPE_NORMAL
        every { packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN) } returns true
        every { packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK) } returns false

        val modePreferences = ModePreferences(context)
        assertEquals(AppMode.MOBILE, modePreferences.detectDefaultDeviceMode())
    }
}
