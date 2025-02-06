package com.sleepbuddy.sleeptracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sleepbuddy.sleeptracker.R
import com.airbnb.lottie.compose.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sleepbuddy.sleeptracker.viewmodel.SleepGoalViewModel
import com.sleepbuddy.sleeptracker.data.MascotState
import android.content.Context
import com.sleepbuddy.sleeptracker.ui.utils.rememberPreference
import androidx.compose.ui.platform.LocalContext
import com.sleepbuddy.sleeptracker.ui.utils.rememberSleepStartTime
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.sleepbuddy.sleeptracker.ui.utils.rememberLastSession
import com.sleepbuddy.sleeptracker.data.MessageState
import androidx.compose.foundation.Image

@Composable
fun HomeScreen(
    onSetGoalClick: () -> Unit,
    viewModel: SleepGoalViewModel = viewModel()
) {
    val context = LocalContext.current
    val isTracking = rememberPreference("is_tracking", false)
    val sleepStartTime = rememberSleepStartTime()
    val lastSession = rememberLastSession()
    val currentStreak by viewModel.currentStreak.collectAsState()
    val mascotState by viewModel.mascotState.collectAsState()
    val messageState by viewModel.messageState.collectAsState()
    val sleepGoal by viewModel.sleepGoal.collectAsState()

    // Check streak when screen becomes active
    LaunchedEffect(Unit) {
        viewModel.checkAndUpdateStreak()
    }

    // Update streak when sleep goal changes
    LaunchedEffect(sleepGoal.bedTime) {
        viewModel.updateStreak()
    }

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
                message = messageState.getMessage(),
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
                    mascotState = mascotState,
                    modifier = Modifier.fillMaxSize(1f)
                )
            }

            // Sleep time info
            if (isTracking.value) {
                sleepStartTime.value?.let { startTime ->
                    SleepTimeInfo(
                        startTime = startTime,
                        isTracking = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                lastSession.value?.let { session ->
                    SleepTimeInfo(
                        startTime = session.startTime,
                        endTime = session.endTime,
                        duration = session.duration,
                        isTracking = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Bottom section with streak and buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Streak progress
                StreakProgress(
                    currentStreak = currentStreak,
                    targetStreak = sleepGoal.targetStreak,
                    modifier = Modifier.fillMaxWidth()
                )

                // Tracking button
                Button(
                    onClick = { 
                        val prefs = context.getSharedPreferences("sleep_tracking", Context.MODE_PRIVATE)
                        if (isTracking.value) {
                            viewModel.stopTracking()
                        } else {
                            viewModel.startTracking()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = if (isTracking.value) 
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
    targetStreak: Int,
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
            progress = (currentStreak % (targetStreak + 1)) / targetStreak.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
    }
}

@Composable
fun MascotAnimation(
    mascotState: MascotState,
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(mascotState.animationRes)
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

@Composable
fun SleepTimeInfo(
    startTime: LocalDateTime,
    endTime: LocalDateTime? = null,
    duration: Duration? = null,
    isTracking: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bedtime Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_moon),
                    contentDescription = stringResource(R.string.bedtime),
                    modifier = Modifier.size(30.dp)
                )
                Text(
                    text = stringResource(R.string.bedtime),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = startTime.format(DateTimeFormatter.ofPattern("h:mm a")),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Wake Up Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_sun),
                    contentDescription = stringResource(R.string.wake_up),
                    modifier = Modifier.size(30.dp)
                )
                Text(
                    text = stringResource(R.string.wake_up),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if (!isTracking && endTime != null) {
                        endTime.format(DateTimeFormatter.ofPattern("h:mm a"))
                    } else {
                        "--:--"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Duration Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_clock),
                    contentDescription = stringResource(R.string.duration_label),
                    modifier = Modifier.size(30.dp)
                )
                Text(
                    text = stringResource(R.string.duration_label),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if (!isTracking && duration != null) {
                        formatDurationConcise(duration)
                    } else {
                        "--h --m"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

private fun formatDurationConcise(duration: Duration): String {
    val hours = duration.toHours()
    val minutes = duration.toMinutesPart()
    return "${hours}h ${minutes}m"
} 