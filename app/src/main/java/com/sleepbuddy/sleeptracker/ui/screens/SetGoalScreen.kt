package com.sleepbuddy.sleeptracker.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sleepbuddy.sleeptracker.R
import com.sleepbuddy.sleeptracker.data.SleepGoal
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import androidx.activity.compose.BackHandler
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetGoalScreen(
    initialGoal: SleepGoal,
    onSaveGoal: (SleepGoal) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedBedTime by remember { mutableStateOf(initialGoal.bedTime) }
    var tempSelectedTime by remember { mutableStateOf(initialGoal.bedTime) }
    var selectedDuration by remember { mutableStateOf(initialGoal.sleepDuration) }
    var tempSelectedDuration by remember { mutableStateOf(initialGoal.sleepDuration) }
    var durationSliderValue by remember { mutableStateOf(initialGoal.sleepDuration) }
    var selectedStreak by remember { mutableStateOf(initialGoal.targetStreak) }
    var streakSliderValue by remember { mutableStateOf(initialGoal.targetStreak.toFloat()) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var confirmationType by remember { mutableStateOf<ConfirmationType?>(null) }

    // Handle system back button press
    BackHandler(onBack = onNavigateBack)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.set_sleep_goal),
                            textAlign = TextAlign.Center
                        )
                    }
                },
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
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                tonalElevation = 2.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.preferred_bed_time),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedBedTime.format(DateTimeFormatter.ofPattern("hh:mm a")))
                    }
                }
            }

            // Sleep Duration Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                tonalElevation = 2.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.sleep_duration),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = stringResource(R.string.hours_value, durationSliderValue),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = durationSliderValue,
                        onValueChange = { durationSliderValue = it },
                        valueRange = 5f..10f,
                        steps = 9,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedButton(
                        onClick = {
                            tempSelectedDuration = durationSliderValue
                            if (tempSelectedDuration != initialGoal.sleepDuration) {
                                confirmationType = ConfirmationType.DURATION
                                showConfirmationDialog = true
                            } else {
                                selectedDuration = tempSelectedDuration
                                onSaveGoal(initialGoal.copy(sleepDuration = selectedDuration))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text(stringResource(R.string.save_duration))
                    }
                }
            }

            // Target Streak Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                tonalElevation = 2.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.target_streak),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = stringResource(R.string.days_value, streakSliderValue.toInt()),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = streakSliderValue,
                        onValueChange = { streakSliderValue = it },
                        valueRange = 5f..30f,
                        steps = 25,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedButton(
                        onClick = {
                            selectedStreak = streakSliderValue.toInt()
                            onSaveGoal(initialGoal.copy(targetStreak = selectedStreak))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text(stringResource(R.string.save_target_streak))
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Mascot Image
            Image(
                painter = painterResource(id = R.drawable.mascot_image),
                contentDescription = null,
                modifier = Modifier
                    .size(360.dp)
                    .align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showTimePicker) {
            TimePickerDialog(
                onDismiss = { 
                    showTimePicker = false 
                    tempSelectedTime = selectedBedTime
                },
                onConfirm = { hour, minute ->
                    tempSelectedTime = LocalTime.of(hour, minute)
                    if (tempSelectedTime != initialGoal.bedTime) {
                        confirmationType = ConfirmationType.BEDTIME
                        showConfirmationDialog = true
                    } else {
                        selectedBedTime = tempSelectedTime
                        onSaveGoal(initialGoal.copy(bedTime = selectedBedTime))
                    }
                    showTimePicker = false
                },
                initialTime = selectedBedTime
            )
        }

        if (showConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showConfirmationDialog = false
                    when (confirmationType) {
                        ConfirmationType.BEDTIME -> tempSelectedTime = selectedBedTime
                        ConfirmationType.DURATION -> {
                            tempSelectedDuration = selectedDuration
                            durationSliderValue = selectedDuration
                        }
                        null -> {}
                    }
                },
                title = { Text(stringResource(R.string.reset_progress_title)) },
                text = { Text(stringResource(R.string.reset_progress_message)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            when (confirmationType) {
                                ConfirmationType.BEDTIME -> {
                                    selectedBedTime = tempSelectedTime
                                    onSaveGoal(initialGoal.copy(bedTime = selectedBedTime))
                                }
                                ConfirmationType.DURATION -> {
                                    selectedDuration = tempSelectedDuration
                                    durationSliderValue = tempSelectedDuration
                                    onSaveGoal(initialGoal.copy(sleepDuration = selectedDuration))
                                }
                                null -> {}
                            }
                            showConfirmationDialog = false
                        }
                    ) {
                        Text(stringResource(R.string.confirm))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            showConfirmationDialog = false
                            when (confirmationType) {
                                ConfirmationType.BEDTIME -> tempSelectedTime = selectedBedTime
                                ConfirmationType.DURATION -> {
                                    tempSelectedDuration = selectedDuration
                                    durationSliderValue = selectedDuration
                                }
                                null -> {}
                            }
                        }
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
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

private enum class ConfirmationType {
    BEDTIME,
    DURATION
} 