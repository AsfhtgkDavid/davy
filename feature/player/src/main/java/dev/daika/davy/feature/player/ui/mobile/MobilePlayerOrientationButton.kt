package dev.daika.davy.feature.player.ui.mobile

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import dev.daika.davy.core.common.util.findActivity

@Composable
fun PlayerOrientationButton(
    modifier: Modifier = Modifier,
    onUserInteraction: () -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val configuration = LocalConfiguration.current

    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    IconButton(
        onClick = {
            onUserInteraction()
            activity?.requestedOrientation = if (isLandscape) {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Rounded.ScreenRotation,
            contentDescription = if (isLandscape) {
                "Switch to portrait mode"
            } else {
                "Switch to landscape mode"
            },
            tint = Color.White
        )
    }
}