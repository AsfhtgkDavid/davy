package dev.daika.davy.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

@Composable
fun <T> TvDropdown(
    modifier: Modifier = Modifier,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    itemText: (T) -> String
) {
    val isEnabled = items.isNotEmpty()

    var buttonSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    var buttonFocused by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Button(
            onClick = { onExpandedChange(!isExpanded) },
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    buttonSize = coordinates.size
                }
                .onFocusChanged { focusState ->
                    buttonFocused = focusState.isFocused
                },
            shape = ButtonDefaults.shape(shape = RoundedCornerShape(8.dp)),
            colors = ButtonDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                focusedContainerColor = MaterialTheme.colorScheme.primary
            ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                var textModifier = Modifier.weight(1f)
                if (buttonFocused) {
                    textModifier = textModifier.basicMarquee()
                }
                Text(
                    text = selectedItem?.let { itemText(it) } ?: "Select an item",
                    maxLines = 1,
                    modifier = textModifier,
                    color = if (buttonFocused) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isEnabled) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "|",
                        color = if (buttonFocused) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.rotate(if (isExpanded) 180f else 0f),
                        tint = if (buttonFocused) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (isExpanded) {
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(0, buttonSize.height),
                onDismissRequest = { onExpandedChange(false) },
                properties = PopupProperties(focusable = true)
            ) {
                Box(
                    modifier = Modifier
                        .width(with(density) { buttonSize.width.toDp() })
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .onFocusChanged { focusState ->
                            if (!focusState.hasFocus && !buttonFocused) {
                                onExpandedChange(false)
                            }
                        }
                ) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(items) { item ->
                            val isSelected = item == selectedItem

                            var itemFocused by remember { mutableStateOf(false) }
                            Surface(
                                onClick = {
                                    onItemSelected(item)
                                    onExpandedChange(false)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { focusState ->
                                        itemFocused = focusState.isFocused
                                    },
                                colors = ClickableSurfaceDefaults.colors(
                                    containerColor = Color.Transparent,
                                    focusedContainerColor = MaterialTheme.colorScheme.primary.copy(
                                        alpha = 0.8f
                                    )
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    var textModifier = Modifier.weight(1f)
                                    if (itemFocused) {
                                        textModifier = textModifier.basicMarquee()
                                    }
                                    Text(
                                        text = itemText(item),
                                        color = if (itemFocused) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = textModifier
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}