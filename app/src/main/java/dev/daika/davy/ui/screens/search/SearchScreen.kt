package dev.daika.davy.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.width
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.daika.davy.ui.common.PosterImage
import dev.daika.davy.ui.common.AnimeCard
import dev.daika.davy.ui.common.AnimePosterCard
import kotlinx.serialization.Serializable


@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: SearchScreenViewModel = hiltViewModel()
) {

    var searchQuery by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }


            TextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                },
                modifier = Modifier.fillMaxWidth(),

                placeholder = {
                    Text("Search anime...")
                },

                singleLine = true,

                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),

                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSearch = {
                        viewModel.search(searchQuery)
                    }
                ),

                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),

                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                searchQuery = ""
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                }
            )
        }


        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            when (val state = uiState) {

                SearchScreenUiState.Idle -> {
                    Text("Search for your favorite anime")
                }


                SearchScreenUiState.Loading -> {
                    CircularProgressIndicator()
                }


                is SearchScreenUiState.Success -> {

                    LazyRow(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {

                        items(state.anime) { anime ->

                            AnimePosterCard(
                                anime = anime,
                                onClick = {
                                    // TODO navigate to details
                                }
                            )
                        }
                    }
                }


                is SearchScreenUiState.Error -> {

                    Text(
                        text = state.message
                    )
                }
            }
        }
    }
}


@Serializable
object SearchScreenDestination