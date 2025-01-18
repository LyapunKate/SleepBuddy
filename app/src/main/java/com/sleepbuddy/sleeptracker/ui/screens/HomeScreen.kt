package com.sleepbuddy.sleeptracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sleepbuddy.sleeptracker.R
import com.airbnb.lottie.compose.*

@Composable
fun HomeScreen(
    onSetGoalClick: () -> Unit
) {
    var isTracking by remember { mutableStateOf(false) }
    val currentStreak = 5 // This will be replaced with actual data later

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Message item above the mascot
            MessageItem(
                message = stringResource(R.string.encouraging_message),
                modifier = Modifier.fillMaxWidth()
            )

            // Mascot animation
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                MascotAnimation(
                    modifier = Modifier.fillMaxSize(0.8f)
                )
            }

            // Bottom section with streak and buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Streak progress
                StreakProgress(currentStreak = currentStreak)

                // Tracking button
                Button(
                    onClick = { isTracking = !isTracking },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = if (isTracking) 
                            stringResource(R.string.stop_tracking)
                        else 
                            stringResource(R.string.start_tracking)
                    )
                }

                // Set Goal button
                OutlinedButton(
                    onClick = onSetGoalClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(stringResource(R.string.set_sleep_goal))
                }
            }
        }
    }
}

@Composable
fun MessageItem(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun StreakProgress(
    currentStreak: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.current_streak, currentStreak),
            style = MaterialTheme.typography.titleMedium
        )
        
        LinearProgressIndicator(
            progress = (currentStreak % 7) / 7f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
    }
}

@Composable
fun MascotAnimation(
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.neutral_pet)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )
    
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier
    )
} 