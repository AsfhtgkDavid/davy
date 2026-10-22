package dev.daika.davy.feature.details.mobile

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.daika.davy.core.domain.model.Anime
import dev.daika.davy.core.domain.model.AnimeStatus
import dev.daika.davy.core.ui.component.MobileAnimeCard
import dev.daika.davy.core.ui.component.PosterImage
import dev.daika.davy.core.ui.component.RatingStars
import dev.daika.davy.feature.details.AnimeDetailsScreenUiState
import dev.daika.davy.feature.details.AnimeDetailsScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileAnimeDetailsScreen(
    onBackPressed: () -> Unit,
    onEpisodeSelected: (Int, Int) -> Unit,
    onAnotherAnimeSelected: (Anime) -> Unit,
    animeDetailsScreenViewModel: AnimeDetailsScreenViewModel = hiltViewModel()
) {
    val state by animeDetailsScreenViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state is AnimeDetailsScreenUiState.Success) {
                            (state as AnimeDetailsScreenUiState.Success).anime.title
                        } else {
                            ""
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val currentState = state) {
                is AnimeDetailsScreenUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                is AnimeDetailsScreenUiState.Success -> {
                    MobileAnimeDetailsContent(
                        anime = currentState.anime,
                        onEpisodeSelected = onEpisodeSelected,
                        onAnotherAnimeSelected = onAnotherAnimeSelected
                    )
                }

                is AnimeDetailsScreenUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Failed to load data",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { animeDetailsScreenViewModel.getAnimeDetails() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MobileAnimeDetailsContent(
    anime: Anime,
    onEpisodeSelected: (Int, Int) -> Unit,
    onAnotherAnimeSelected: (Anime) -> Unit
) {
    var isDescriptionExpanded by remember { mutableStateOf(false) }

    var selectedTranslation by remember { mutableStateOf(anime.translations.firstOrNull()) }
    var selectedPlayer by remember { mutableStateOf(selectedTranslation?.availablePlayers?.firstOrNull()) }

    var isTranslationDropdownOpen by remember { mutableStateOf(false) }
    var isPlayerDropdownOpen by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTranslation) {
        selectedPlayer = selectedTranslation?.availablePlayers?.firstOrNull()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                PosterImage(anime = anime, modifier = Modifier.fillMaxSize())
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = anime.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (anime.otherTitles.isNotEmpty()) {
                    Text(
                        text = anime.otherTitles.take(2).joinToString(" / "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingStars(rating = anime.rating.average, maxStars = 5)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = String.format("%.1f", anime.rating.average),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (anime.status) {
                        AnimeStatus.RELEASED -> Color(0xFF2E7D32).copy(alpha = 0.2f)
                        AnimeStatus.ONGOING -> Color(0xFFE65100).copy(alpha = 0.2f)
                        AnimeStatus.ANNOUNCE -> Color(0xFF1565C0).copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = when (anime.status) {
                            AnimeStatus.RELEASED -> "Released"
                            AnimeStatus.ONGOING -> "Ongoing"
                            AnimeStatus.ANNOUNCE -> "Announced"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = when (anime.status) {
                            AnimeStatus.RELEASED -> Color(0xFF81C784)
                            AnimeStatus.ONGOING -> Color(0xFFFFB74D)
                            AnimeStatus.ANNOUNCE -> Color(0xFF90CAF9)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val hasPlayableEpisodes = anime.translations.any { it.availablePlayers.isNotEmpty() }
        Button(
            onClick = {
                val firstEpisode = selectedPlayer?.episodes?.firstOrNull()
                    ?: anime.translations.firstOrNull()?.availablePlayers?.firstOrNull()?.episodes?.firstOrNull()
                if (firstEpisode != null) {
                    onEpisodeSelected(anime.id, firstEpisode.videoId)
                }
            },
            enabled = hasPlayableEpisodes,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Watch online",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Genres chips
        if (anime.genres.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(anime.genres) { genre ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Text(
                            text = genre,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Description with expandable toggle
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = anime.description,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 4,
                overflow = TextOverflow.Ellipsis
            )
            if (anime.description.length > 200) {
                Text(
                    text = if (isDescriptionExpanded) "Show less" else "Read more...",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                        .padding(top = 4.dp, bottom = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Dubbing & Player Selector
        if (anime.translations.isNotEmpty()) {
            Text(
                text = "Voiceover & Episodes",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Translation Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { isTranslationDropdownOpen = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = selectedTranslation?.title ?: "Select voiceover",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = isTranslationDropdownOpen,
                        onDismissRequest = { isTranslationDropdownOpen = false }
                    ) {
                        anime.translations.forEach { translation ->
                            DropdownMenuItem(
                                text = { Text("${translation.title} (${translation.availablePlayers.maxOfOrNull { it.episodes.size } ?: 0} eps)") },
                                onClick = {
                                    selectedTranslation = translation
                                    isTranslationDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                // Player Dropdown
                val availablePlayers = selectedTranslation?.availablePlayers ?: emptyList()
                if (availablePlayers.size > 1) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { isPlayerDropdownOpen = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = selectedPlayer?.player ?: "Player",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                        DropdownMenu(
                            expanded = isPlayerDropdownOpen,
                            onDismissRequest = { isPlayerDropdownOpen = false }
                        ) {
                            availablePlayers.forEach { player ->
                                DropdownMenuItem(
                                    text = { Text(player.player) },
                                    onClick = {
                                        selectedPlayer = player
                                        isPlayerDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Episodes list/grid
            val episodes = selectedPlayer?.episodes ?: emptyList()
            if (episodes.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    episodes.forEach { episode ->
                        Surface(
                            onClick = { onEpisodeSelected(anime.id, episode.videoId) },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(width = 64.dp, height = 40.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = episode.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Viewing order
        if (anime.viewingOrder.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Viewing Order",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(anime.viewingOrder, key = { it.id }) { relatedAnime ->
                    MobileAnimeCard(
                        anime = relatedAnime,
                        onClick = { onAnotherAnimeSelected(relatedAnime) },
                        modifier = Modifier.width(130.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
