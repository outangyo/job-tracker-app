package com.mocode.jobtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.mocode.jobtracker.ui.navigation.JobTrackerBottomBar
import com.mocode.jobtracker.ui.navigation.JobTrackerNavHost
import com.mocode.jobtracker.ui.theme.JobTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JobTrackerTheme {
                JobTrackerApp()
            }
        }
    }
}

@Composable
fun JobTrackerApp() {
    val navController = rememberNavController()

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