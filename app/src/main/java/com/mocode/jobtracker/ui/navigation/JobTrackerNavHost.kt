package com.mocode.jobtracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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
                    navController.navigate(Screen.AddApplication.route)
                },
                onNavigateToDetail = { id ->
                    navController.navigate(Screen.ApplicationDetail.createRoute(id))
                }
            )
        }

        composable(Screen.Applications.route) {
            ApplicationsScreen(
                onNavigateToAdd = {
                    navController.navigate(Screen.AddApplication.route)
                },
                onNavigateToDetail = { id ->
                    navController.navigate(Screen.ApplicationDetail.createRoute(id))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToLock = {
                    navController.navigate(Screen.Lock.route)
                }
            )
        }

        composable(Screen.AddApplication.route) {
            AddApplicationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.ApplicationDetail.route,
            arguments = listOf(
                navArgument(Screen.ApplicationDetail.ARG_APPLICATION_ID) {
                    type = NavType.LongType
                    defaultValue = 1L
                }
            )
        ) { backStackEntry ->
            val applicationId = backStackEntry.arguments?.getLong(Screen.ApplicationDetail.ARG_APPLICATION_ID) ?: 1L
            ApplicationDetailScreen(
                applicationId = applicationId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = {
                    navController.navigate(Screen.AddApplication.route)
                }
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
