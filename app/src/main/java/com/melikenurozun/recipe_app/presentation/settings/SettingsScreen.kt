package com.melikenurozun.recipe_app.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun SettingsScreen(
    onSignOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Dark Mode Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Dark Mode", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { viewModel.onEvent(SettingsEvent.ToggleDarkMode) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.onEvent(SettingsEvent.SignOut)
                    onSignOut()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign Out")
            }
        }
    }
}
