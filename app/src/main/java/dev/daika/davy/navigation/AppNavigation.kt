package dev.daika.davy.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.daika.davy.core.common.preference.AppMode
import dev.daika.davy.core.common.preference.ModePreferences
import dev.daika.davy.core.ui.theme.DavyMobileTheme
import dev.daika.davy.core.ui.theme.DavyTvTheme
import dev.daika.davy.feature.details.AnimeDetailsScreenDestination
import dev.daika.davy.feature.details.mobile.MobileAnimeDetailsScreen
import dev.daika.davy.feature.details.tv.TvAnimeDetailsScreen
import dev.daika.davy.feature.home.HomeScreenDestination
import dev.daika.davy.feature.home.mobile.MobileHomeScreen
import dev.daika.davy.feature.home.tv.TvHomeScreen
import dev.daika.davy.feature.modeselection.ModeSelectionDestination
import dev.daika.davy.feature.modeselection.ModeSelectionScreen
import dev.daika.davy.feature.player.PlayerScreenDestination
import dev.daika.davy.feature.player.mobile.MobilePlayerScreen
import dev.daika.davy.feature.player.tv.TvPlayerScreen
import dev.daika.davy.feature.search.SearchScreenDestination
import dev.daika.davy.feature.search.mobile.MobileSearchScreen
import dev.daika.davy.feature.search.tv.TvSearchScreen

@Composable
fun AppNavigation(
    modePreferences: ModePreferences,
    onBackPressed: () -> Unit
) {
    var activeMode by remember {
        mutableStateOf(
            modePreferences.getSavedMode()
        )
    }

    when (activeMode) {
        AppMode.TV -> {
            DavyTvTheme {
                TvNavGraph(
                    onSwitchMode = {
                        activeMode = AppMode.MOBILE
                        modePreferences.setAppMode(AppMode.MOBILE)
                    }
                )
            }
        }

        AppMode.MOBILE -> {
            DavyMobileTheme {
                MobileNavGraph(
                    onSwitchMode = {
                        activeMode = AppMode.TV
                        modePreferences.setAppMode(AppMode.TV)
                    }
                )
            }
        }

        null -> {
            DavyMobileTheme {
                ModeSelectionScreen(
                    modePreferences = modePreferences,
                    onModeConfirmed = { selectedMode ->
                        activeMode = selectedMode
                        modePreferences.setAppMode(selectedMode)
                    }
                )
            }
        }
    }
}

@Composable
private fun TvNavGraph(
    onSwitchMode: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HomeScreenDestination
    ) {
        composable<HomeScreenDestination> {
            TvHomeScreen(
                onAnimeSelected = { anime ->
                    navController.navigate(AnimeDetailsScreenDestination(anime.id))
                },
                onSearchClicked = {
                    navController.navigate(SearchScreenDestination)
                },
                onSwitchModeClicked = onSwitchMode
            )
        }
        composable<AnimeDetailsScreenDestination> {
            TvAnimeDetailsScreen(
                onEpisodeSelected = { animeId, episodeId ->
                    navController.navigate(PlayerScreenDestination(animeId, episodeId))
                },
                onAnotherAnimeSelected = { anime ->
                    navController.navigate(AnimeDetailsScreenDestination(anime.id))
                }
            )
        }
        composable<PlayerScreenDestination> {
            TvPlayerScreen(
                onBackPressed = { navController.popBackStack() }
            )
        }
        composable<SearchScreenDestination> {
            TvSearchScreen(
                onAnimeSelected = { anime ->
                    navController.navigate(AnimeDetailsScreenDestination(anime.id))
                }
            )
        }
    }
}

@Composable
private fun MobileNavGraph(
    onSwitchMode: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HomeScreenDestination
    ) {
        composable<HomeScreenDestination> {
            MobileHomeScreen(
                onAnimeSelected = { anime ->
                    navController.navigate(AnimeDetailsScreenDestination(anime.id))
                },
                onSearchClicked = {
                    navController.navigate(SearchScreenDestination)
                },
                onSwitchModeClicked = onSwitchMode
            )
        }
        composable<AnimeDetailsScreenDestination> {
            MobileAnimeDetailsScreen(
                onBackPressed = { navController.popBackStack() },
                onEpisodeSelected = { animeId, episodeId ->
                    navController.navigate(PlayerScreenDestination(animeId, episodeId))
                },
                onAnotherAnimeSelected = { anime ->
                    navController.navigate(AnimeDetailsScreenDestination(anime.id))
                }
            )
        }
        composable<PlayerScreenDestination> {
            MobilePlayerScreen(
                onBackPressed = { navController.popBackStack() }
            )
        }
        composable<SearchScreenDestination> {
            MobileSearchScreen(
                onBackPressed = { navController.popBackStack() },
                onAnimeSelected = { anime ->
                    navController.navigate(AnimeDetailsScreenDestination(anime.id))
                }
            )
        }
    }
}
