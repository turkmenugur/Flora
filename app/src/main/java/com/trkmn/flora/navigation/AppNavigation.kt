package com.trkmn.flora.navigation

import android.net.Uri
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.trkmn.flora.ui.screens.home.HomeScreen
import com.trkmn.flora.ui.screens.profile.ProfileScreen
import com.trkmn.flora.ui.screens.settings.SettingsScreen
import com.trkmn.flora.ui.screens.analysis.AnalysisScreen
import com.trkmn.flora.ui.screens.detail.DetailScreen
import com.trkmn.flora.service.GeminiService
import com.trkmn.flora.repository.PlantAnalysisRepository
import com.trkmn.flora.data.PlantAnalysisEntity

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object Analysis : Screen("analysis")
    object Detail : Screen("detail")
}

@Composable
fun AppNavigation(
    geminiService: GeminiService,
    repository: PlantAnalysisRepository
) {
    val navController = rememberNavController()
    var currentImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedAnalysis by remember { mutableStateOf<PlantAnalysisEntity?>(null) }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAnalysis = { uri ->
                    currentImageUri = uri
                    navController.navigate(Screen.Analysis.route)
                },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToDetail = { analysis ->
                    selectedAnalysis = analysis
                    navController.navigate(Screen.Detail.route)
                },
                repository = repository
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                repository = repository,
                onNavigateToDetail = { analysis ->
                    selectedAnalysis = analysis
                    navController.navigate(Screen.Detail.route)
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Analysis.route) {
            AnalysisScreen(
                imageUri = currentImageUri,
                onNavigateBack = { navController.popBackStack() },
                onSaveAnalysis = {
                    navController.popBackStack()
                },
                geminiService = geminiService,
                repository = repository
            )
        }

        composable(Screen.Detail.route) {
            selectedAnalysis?.let { analysis ->
                DetailScreen(
                    analysis = analysis,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
} 