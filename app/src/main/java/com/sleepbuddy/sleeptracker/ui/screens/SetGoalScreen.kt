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
import com.sleepbuddy.sleeptracker.ui.components.NeumorphicSurface
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.material.icons.filled.Close


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
        modifier = Modifier.fillMaxSize(),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Custom header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.set_sleep_goal),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                
                NeumorphicSurface(
                    modifier = Modifier.size(48.dp)
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                }
            }

            // Bed Time Section
            NeumorphicSurface(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    // First line: Title and Time Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.preferred_bed_time),
                            style = MaterialTheme.typography.titleMedium
                        )

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.background,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // Inner Shadow Effect
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.15f),
                                                Color.Transparent
                                            ),
                                            start = Offset.Zero,
                                            end = Offset(50f, 50f)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            )
                            
                            IconButton(
                                onClick = { showTimePicker = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.update_icon),
                                    contentDescription = stringResource(R.string.select_bed_time),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                    }

                    // Second line: Selected Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedBedTime.format(DateTimeFormatter.ofPattern("h:mm a")),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Sleep Duration Section
            NeumorphicSurface(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    // First line: Title and Save button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.sleep_duration),
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        ShadowedSaveButton(
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
                            contentDescription = stringResource(R.string.save_duration)
                        )
                    }

                    // Second line: Hours and Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.hours_value, durationSliderValue),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.width(120.dp)  // Fixed width for alignment
                        )
                        
                        Slider(
                            value = durationSliderValue,
                            onValueChange = { durationSliderValue = it },
                            valueRange = 5f..10f,
                            steps = 9,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                inactiveTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                            )
                        )
                    }
                }
            }

            // Target Streak Section
            NeumorphicSurface(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    // First line: Title and Save button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.target_streak),
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        ShadowedSaveButton(
                            onClick = {
                                selectedStreak = streakSliderValue.toInt()
                                onSaveGoal(initialGoal.copy(targetStreak = selectedStreak))
                            },
                            contentDescription = stringResource(R.string.save_target_streak)
                        )
                    }

                    // Second line: Days and Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.days_value, streakSliderValue.toInt()),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.width(120.dp)  // Fixed width for alignment
                        )
                        
                        Slider(
                            value = streakSliderValue,
                            onValueChange = { streakSliderValue = it },
                            valueRange = 5f..30f,
                            steps = 25,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                inactiveTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Mascot Image
            Image(
                painter = painterResource(id = R.drawable.mascot_image),
                contentDescription = null,
                modifier = Modifier
                    .size(240.dp)
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

@Composable
private fun ShadowedSaveButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // **Inner Shadow Effect (Dark overlay at top-left)**
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(8.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.15f),  // Darker at top-left
                            Color.Transparent                 // Fades out
                        ),
                        start = Offset.Zero,                      // Starts from top-left
                        end = Offset(50f, 50f)                    // Fades towards bottom-right
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
        )
        
        // Save icon button
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(36.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.save_icon),
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp)
            )
        }
    }
} 