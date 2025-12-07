package com.example.gridmaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gridmaster.ui.MainScreen
import com.example.gridmaster.ui.MaintenanceViewModel
import com.example.gridmaster.ui.theme.GridmasterTheme
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 1. Manage Theme State
            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemDark) }

            // 2. Wrap App in Theme
            GridmasterTheme(darkTheme = isDarkTheme) {

                // 3. Initialize ViewModel
                val viewModel: MaintenanceViewModel = viewModel(factory = MaintenanceViewModel.Factory)
                // [NEW] Subscribe to Alerts
                FirebaseMessaging.getInstance().subscribeToTopic("fault_alerts")
                    .addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            // Optional: Log failure if needed
                        }
                    }

                // 4. Show the Main Screen & Pass Toggle Logic
                MainScreen(
                    maintenanceViewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }
}