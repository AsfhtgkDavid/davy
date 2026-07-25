package dev.daika.davy.ui.screens.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import dev.daika.davy.domain.entity.Anime
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds

@Composable
fun SearchScreen(
    onAnimeSelected: (Anime) -> Unit = {},
    searchScreenViewModel: SearchScreenViewModel = hiltViewModel()
) {
    val uiState by searchScreenViewModel.uiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                searchJob?.cancel()
                searchJob = scope.launch {
                    delay(1.seconds)
                    searchScreenViewModel.searchAnime(searchQuery)
                }
            },
            keyboardActions = KeyboardActions(
                onSearch = {
                    searchJob?.cancel()
                    searchJob = scope.launch {
                        searchScreenViewModel.searchAnime(searchQuery)
                    }
                }
            ),
            trailingIcon = {
                IconButton(
                    onClick = {
                        searchQuery = ""
                        searchJob?.cancel()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search query",
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clip(MaterialTheme.shapes.medium),
        )
        when (uiState) {
            is SearchScreenUiState.Loading -> {
                // Show loading indicator
            }

            is SearchScreenUiState.Success -> {
            }

            is SearchScreenUiState.Error -> {
                // Show error message
            }
        }
    }
}

@Serializable
data object SearchScreenDestination