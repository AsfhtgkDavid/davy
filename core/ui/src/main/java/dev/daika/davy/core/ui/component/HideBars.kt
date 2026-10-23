package dev.daika.davy.core.ui.component

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
fun HideBars() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as Activity).window;
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)

        val originalBehavior = insetsController.systemBarsBehavior
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())

        onDispose {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior = originalBehavior
        }
    }
}