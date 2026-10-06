package com.mocode.jobtracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.mocode.jobtracker.data.preferences.ThemePreference
import com.mocode.jobtracker.notification.NotificationHelper
import com.mocode.jobtracker.ui.navigation.JobTrackerBottomBar
import com.mocode.jobtracker.ui.navigation.JobTrackerNavHost
import com.mocode.jobtracker.ui.navigation.Screen
import com.mocode.jobtracker.ui.theme.JobTrackerTheme

class MainActivity : ComponentActivity() {

    private val targetApplicationId = mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        extractNotificationTarget(intent)

        val app = application as JobTrackerApplication

        setContent {
            val themePreference by app.userPreferencesRepository.themePreference
                .collectAsState(initial = ThemePreference.SYSTEM)

            JobTrackerTheme(themePreference = themePreference) {
                JobTrackerApp(
                    targetApplicationId = targetApplicationId.value,
                    onTargetNavigated = { targetApplicationId.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractNotificationTarget(intent)
    }

    private fun extractNotificationTarget(intent: Intent?) {
        val appId = intent?.getLongExtra(NotificationHelper.EXTRA_APPLICATION_ID, -1L) ?: -1L
        if (appId > 0) {
            targetApplicationId.value = appId
        }
    }
}

@Composable
fun JobTrackerApp(
    targetApplicationId: Long? = null,
    onTargetNavigated: () -> Unit = {}
) {
    val navController = rememberNavController()

    LaunchedEffect(targetApplicationId) {
        val targetId = targetApplicationId
        if (targetId != null && targetId > 0) {
            navController.navigate(Screen.ApplicationDetail.createRoute(targetId))
            onTargetNavigated()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            JobTrackerBottomBar(navController = navController)
        }
    ) { innerPadding ->
        JobTrackerNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}