package com.mocode.jobtracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mocode.jobtracker.ui.AppViewModelProvider
import com.mocode.jobtracker.ui.addapplication.AddApplicationScreen
import com.mocode.jobtracker.ui.applicationdetail.ApplicationDetailScreen
import com.mocode.jobtracker.ui.applications.ApplicationsScreen
import com.mocode.jobtracker.ui.dashboard.DashboardScreen
import com.mocode.jobtracker.ui.lock.LockScreen
import com.mocode.jobtracker.ui.settings.SettingsScreen

@Composable
fun JobTrackerNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToAdd = {
                    navController.navigate(Screen.AddApplication.createRoute())
                },
                onNavigateToDetail = { id ->
                    navController.navigate(Screen.ApplicationDetail.createRoute(id))
                },
                viewModel = viewModel(factory = AppViewModelProvider.Factory)
            )
        }

        composable(Screen.Applications.route) {
            ApplicationsScreen(
                onNavigateToAdd = {
                    navController.navigate(Screen.AddApplication.createRoute())
                },
                onNavigateToDetail = { id ->
                    navController.navigate(Screen.ApplicationDetail.createRoute(id))
                },
                viewModel = viewModel(factory = AppViewModelProvider.Factory)
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToLock = {
                    navController.navigate(Screen.Lock.route)
                },
                viewModel = viewModel(factory = AppViewModelProvider.Factory)
            )
        }

        composable(
            route = Screen.AddApplication.route,
            arguments = listOf(
                navArgument(Screen.AddApplication.ARG_APPLICATION_ID) {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            AddApplicationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = viewModel(factory = AppViewModelProvider.Factory)
            )
        }

        composable(
            route = Screen.ApplicationDetail.route,
            arguments = listOf(
                navArgument(Screen.ApplicationDetail.ARG_APPLICATION_ID) {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            ApplicationDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.AddApplication.createRoute(id))
                },
                viewModel = viewModel(factory = AppViewModelProvider.Factory)
            )
        }

        composable(Screen.Lock.route) {
            LockScreen(
                onUnlock = {
                    navController.popBackStack()
                }
            )
        }
    }
}
