package dev.daika.davy.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ShapeDefaults
import androidx.tv.material3.StandardCardContainer
import androidx.tv.material3.Surface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AnimeCard(
    onClick: () -> Unit,
    modifier: Modifier,
    title: @Composable () -> Unit,
    image: @Composable BoxScope.() -> Unit
) {
    StandardCardContainer(
        modifier = modifier,
        title = title,
        imageCard = {
            Surface(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f),
                shape = ClickableSurfaceDefaults.shape(ShapeDefaults.ExtraSmall),
                border = ClickableSurfaceDefaults.border(
                    focusedBorder = Border(
                        border = BorderStroke(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = ShapeDefaults.ExtraSmall
                    )
                ),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
                content = image
            )
        },
    )
}

@Preview(showBackground = true)
@Composable
fun AnimeCardPreview() {
    AnimeCard(
        onClick = {},
        modifier = Modifier.fillMaxWidth(),
        title = { Text("Sample Title") },
        image = {
            Box(Modifier.fillMaxSize().background(Color.Gray))
        }
    )
}
