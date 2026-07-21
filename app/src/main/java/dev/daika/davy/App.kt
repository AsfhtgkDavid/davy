package dev.daika.davy

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.daika.davy.ui.screens.anime.AnimeDetailsScreen
import dev.daika.davy.ui.screens.anime.AnimeDetailsScreenDestination
import dev.daika.davy.ui.screens.home.HomeScreen
import dev.daika.davy.ui.screens.home.HomeScreenDestination

@Composable
fun App(
    onBackPressed: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HomeScreenDestination,
        builder = {
            composable<HomeScreenDestination> {
                HomeScreen(
                    onAnimeSelected = { anime ->
                        navController.navigate(AnimeDetailsScreenDestination(anime.id))
                    }
                )
            }
            composable<AnimeDetailsScreenDestination> {
                AnimeDetailsScreen(
                    onBackPressed = {
                        if (navController.navigateUp()) {
                            onBackPressed()
                        }
                    },
                    onEpisodeSelected = { episode ->
                        // Handle episode selection, e.g., navigate to a player screen
                    },
                )
            }
        }
    )
}