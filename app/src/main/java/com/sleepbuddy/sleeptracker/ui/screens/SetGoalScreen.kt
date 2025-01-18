package com.sleepbuddy.sleeptracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sleepbuddy.sleeptracker.R
import com.sleepbuddy.sleeptracker.data.SleepGoal
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetGoalScreen(
    initialGoal: SleepGoal,
    onSaveGoal: (SleepGoal) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedBedTime by remember { mutableStateOf(initialGoal.bedTime) }
    var selectedDuration by remember { mutableStateOf(initialGoal.sleepDuration) }
    var selectedStreak by remember { mutableStateOf(initialGoal.targetStreak) }
    var showTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.set_sleep_goal)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Bed Time Section
            Column {
                Text(
                    text = stringResource(R.string.preferred_bed_time),
                    style = MaterialTheme.typography.titleMedium
                )
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedBedTime.format(DateTimeFormatter.ofPattern("hh:mm a")))
                }
            }

            // Sleep Duration Section
            Column {
                Text(
                    text = stringResource(R.string.sleep_duration),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.hours_value, selectedDuration),
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = selectedDuration,
                    onValueChange = { selectedDuration = it },
                    valueRange = 4f..12f,
                    steps = 16,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Target Streak Section
            Column {
                Text(
                    text = stringResource(R.string.target_streak),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.days_value, selectedStreak),
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = selectedStreak.toFloat(),
                    onValueChange = { selectedStreak = it.toInt() },
                    valueRange = 5f..30f,
                    steps = 25,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            Button(
                onClick = {
                    onSaveGoal(
                        SleepGoal(
                            bedTime = selectedBedTime,
                            sleepDuration = selectedDuration,
                            targetStreak = selectedStreak
                        )
                    )
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(stringResource(R.string.save_goal))
            }
        }

        if (showTimePicker) {
            TimePickerDialog(
                onDismiss = { showTimePicker = false },
                onConfirm = { hour, minute ->
                    selectedBedTime = LocalTime.of(hour, minute)
                    showTimePicker = false
                },
                initialTime = selectedBedTime
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    initialTime: LocalTime
) {
    var timeState by remember {
        mutableStateOf(TimePickerState(
            initialHour = initialTime.hour,
            initialMinute = initialTime.minute,
            is24Hour = false
        ))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_bed_time)) },
        confirmButton = {
            TextButton(
                onClick = { 
                    onConfirm(timeState.hour, timeState.minute)
                }
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        text = {
            TimePicker(
                state = timeState
            )
        }
    )
} 