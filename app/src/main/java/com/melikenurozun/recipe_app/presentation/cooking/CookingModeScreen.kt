package com.melikenurozun.recipe_app.presentation.cooking

import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.animation.*
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun KeepScreenOn() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as? android.app.Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingModeScreen(
    onNavigateBack: () -> Unit,
    viewModel: CookingModeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.keepScreenOn) {
        KeepScreenOn()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = uiState.recipe?.title ?: "Cooking Mode",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                        Text(
                            text = "Step ${uiState.currentStepIndex + 1} of ${uiState.steps.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, "Exit")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Button
                    if (uiState.currentStepIndex > 0) {
                        Button(onClick = { viewModel.onEvent(CookingEvent.PreviousStep) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Prev")
                        }
                    } else {
                        Spacer(Modifier.width(80.dp)) // Placeholder
                    }

                    // Timer Button (if detected or manual)
                    FloatingActionButton(
                        onClick = { 
                            // Toggle timer or open menu. For simple MVP, start 5 min timer or stop
                            if (uiState.isTimerRunning) {
                                viewModel.onEvent(CookingEvent.StopTimer)
                            } else {
                                viewModel.onEvent(CookingEvent.StartTimer(5)) // Default 5 min
                            }
                        },
                        containerColor = if(uiState.isTimerRunning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                    ) {
                        if (uiState.isTimerRunning) {
                           Text(
                               text = formatTime(uiState.timerSeconds),
                               style = MaterialTheme.typography.labelLarge,
                               modifier = Modifier.padding(horizontal = 8.dp)
                           )
                        } else {
                           Icon(Icons.Default.PlayArrow, "Timer")
                        }
                    }

                    // Next Button
                    if (uiState.currentStepIndex < uiState.steps.lastIndex) {
                         Button(onClick = { viewModel.onEvent(CookingEvent.NextStep) }) {
                            Text("Next")
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                        }
                    } else {
                        Button(onClick = onNavigateBack) {
                            Text("Finish")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.steps.isNotEmpty()) {
                val currentStep = uiState.steps[uiState.currentStepIndex]
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                        },
                        label = "stepAnimation"
                    ) { stepText ->
                        Text(
                            text = stepText,
                            style = MaterialTheme.typography.headlineSmall, // Bigger text
                            textAlign = TextAlign.Center,
                            lineHeight = 40.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Regex detection for timer in text
                    val timeMatch = remember(currentStep) {
                        Regex("(\\d+)\\s*(min|minute|dakika)").find(currentStep)
                    }
                    
                    if (timeMatch != null && !uiState.isTimerRunning) {
                        val minutes = timeMatch.groupValues[1].toIntOrNull() ?: 5
                        Button(
                            onClick = { viewModel.onEvent(CookingEvent.StartTimer(minutes)) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(Icons.Default.PlayArrow, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Start $minutes min Timer")
                        }
                    }
                }
            } else {
                Text("No instructions found.")
            }
        }
    }
}

// Removed empty helper

fun formatTime(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
