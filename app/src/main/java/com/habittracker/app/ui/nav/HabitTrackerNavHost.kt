package com.habittracker.app.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.habittracker.app.data.smoking.SmokingRepository
import com.habittracker.app.ui.common.ComingSoonScreen
import com.habittracker.app.ui.home.HomeScreen
import com.habittracker.app.ui.smoking.SmokingScreen
import com.habittracker.app.ui.smoking.SmokingViewModel

@Composable
fun HabitTrackerNavHost(smokingRepository: SmokingRepository) {
    val navController = rememberNavController()
    val smokingViewModel: SmokingViewModel = viewModel(factory = SmokingViewModel.Factory(smokingRepository))

    Scaffold(
        bottomBar = { HabitTrackerBottomBar(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(smokingViewModel = smokingViewModel, onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                })
            }
            composable(Routes.SMOKING) {
                SmokingScreen(viewModel = smokingViewModel)
            }
            composable(Routes.DRINKING) {
                ComingSoonScreen(title = "Drinking Tracker", icon = androidx.compose.material.icons.Icons.Filled.LocalBar)
            }
            composable(Routes.WORKOUT) {
                ComingSoonScreen(title = "Workout Tracker", icon = androidx.compose.material.icons.Icons.Filled.FitnessCenter)
            }
            composable(Routes.CALORIES) {
                ComingSoonScreen(
                    title = "Calories Tracker",
                    icon = androidx.compose.material.icons.Icons.Filled.Restaurant,
                    message = "Will use the Claude Vision API to estimate calories from a food photo."
                )
            }
        }
    }
}

@Composable
private fun HabitTrackerBottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { androidx.compose.material3.Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
