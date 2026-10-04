package com.mocode.jobtracker.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun JobTrackerBottomBar(
    navController: NavController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show bottom bar only on top-level destinations
    val showBottomBar = Screen.bottomNavItems.any { it.route == currentRoute }

    if (showBottomBar) {
        NavigationBar {
            Screen.bottomNavItems.forEach { screen ->
                val selected = currentRoute == screen.route
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        if (currentRoute != screen.route) {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        screen.icon?.let {
                            Icon(imageVector = it, contentDescription = screen.title)
                        }
                    },
                    label = { Text(text = screen.title) }
                )
            }
        }
    }
}
