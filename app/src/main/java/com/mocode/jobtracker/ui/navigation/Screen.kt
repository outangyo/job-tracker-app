package com.mocode.jobtracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    // Top-level destinations (with bottom bar items)
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Applications : Screen("applications", "Applications", Icons.Default.Work)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    // Secondary destinations
    object AddApplication : Screen("add_edit_application?applicationId={applicationId}", "Application Form") {
        const val ARG_APPLICATION_ID = "applicationId"
        fun createRoute(applicationId: Long? = null): String {
            return if (applicationId != null && applicationId > 0) {
                "add_edit_application?applicationId=$applicationId"
            } else {
                "add_edit_application"
            }
        }
    }
    object ApplicationDetail : Screen("application_detail/{applicationId}", "Application Detail") {
        const val ARG_APPLICATION_ID = "applicationId"
        fun createRoute(applicationId: Long): String = "application_detail/$applicationId"
    }
    object Lock : Screen("lock", "Lock", Icons.Default.Lock)

    companion object {
        val bottomNavItems = listOf(Dashboard, Applications, Settings)
    }
}
