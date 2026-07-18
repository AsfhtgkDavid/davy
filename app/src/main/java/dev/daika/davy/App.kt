package dev.daika.davy

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.daika.davy.ui.screens.Screens
import dev.daika.davy.ui.screens.home.HomeScreen

@Composable
fun App(
    onBackPressed: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screens.Home(),
        builder = {
            composable(route = Screens.Home()) {
                HomeScreen(
                    onAnimeSelected = { animeId ->
                        navController.navigate(Screens.AnimeDetails.withArgs(animeId))
                    }
                )
            }
//            composable(
//                route = Screens.AnimeDetails(),
//                arguments = listOf(
//                    navArgument("animeId") { type = NavType.StringType }
//                )
//            ) { backStackEntry ->
//                val animeId = backStackEntry.arguments?.getString("animeId")
//                AnimeDetailsScreen(
//                    animeId = animeId,
//                    onBackPressed = {
//                        if (navController.navigateUp()) {
//                            onBackPressed()
//                        }
//                    }
//                )
//            }
        }
    )
}