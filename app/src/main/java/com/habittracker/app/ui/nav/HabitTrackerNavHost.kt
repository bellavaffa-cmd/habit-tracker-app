package com.habittracker.app.ui.nav

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.habittracker.app.HabitTrackerApplication
import com.habittracker.app.ui.common.ComingSoonScreen
import com.habittracker.app.ui.home.HomeScreen
import com.habittracker.app.ui.settings.SettingsScreen
import com.habittracker.app.ui.smoking.SmokingScreen
import com.habittracker.app.ui.smoking.SmokingViewModel
import com.habittracker.app.update.UpdateInfo

@Composable
fun HabitTrackerNavHost(application: HabitTrackerApplication) {
    val navController = rememberNavController()
    val smokingViewModel: SmokingViewModel = viewModel(
        factory = SmokingViewModel.Factory(
            application = application,
            repository = application.smokingRepository,
            settingsRepository = application.smokingSettingsRepository,
            purchaseRepository = application.cigarettePurchaseRepository,
            quitPlanRepository = application.quitPlanRepository
        )
    )

    val updateManager = application.updateManager
    val updateState by updateManager.state.collectAsState()
    LaunchedEffect(Unit) { updateManager.checkForUpdate() }

    Scaffold(
        bottomBar = { HabitTrackerBottomBar(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    smokingViewModel = smokingViewModel,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) }
                )
            }
            composable(Routes.SMOKING) {
                SmokingScreen(viewModel = smokingViewModel)
            }
            composable(Routes.DRINKING) {
                ComingSoonScreen(title = "Drinking Tracker", icon = Icons.Filled.LocalBar)
            }
            composable(Routes.WORKOUT) {
                ComingSoonScreen(title = "Workout Tracker", icon = Icons.Filled.FitnessCenter)
            }
            composable(Routes.CALORIES) {
                ComingSoonScreen(
                    title = "Calories Tracker",
                    icon = Icons.Filled.Restaurant,
                    message = "Will use the Claude Vision API to estimate calories from a food photo."
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(updateManager = updateManager, onBack = { navController.popBackStack() })
            }
        }
    }

    val update = updateState.available
    if (update != null && !updateState.dismissed) {
        UpdateDialog(
            info = update,
            downloading = updateState.downloading,
            onUpdate = { updateManager.startUpdate() },
            onDismiss = { updateManager.dismiss() }
        )
    }
}

@Composable
private fun UpdateDialog(
    info: UpdateInfo,
    downloading: Boolean,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!downloading) onDismiss() },
        title = { Text("Update available") },
        text = {
            Column {
                Text(
                    "Version ${info.versionName} is available. Download and install it now?",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                )
                if (info.notes.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        info.notes.trim().take(300),
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 8,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (downloading) {
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("Downloading…", style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onUpdate, enabled = !downloading) { Text("Update") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !downloading) { Text("Later") }
        }
    )
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
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
