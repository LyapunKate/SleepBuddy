package com.sleepbuddy.sleeptracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sleepbuddy.sleeptracker.ui.screens.HomeScreen
import com.sleepbuddy.sleeptracker.ui.screens.SetGoalScreen
import com.sleepbuddy.sleeptracker.ui.theme.SleepBuddyTheme
import com.sleepbuddy.sleeptracker.viewmodel.SleepGoalViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SleepBuddyTheme {
                SleepBuddyApp()
            }
        }
    }
}

@Composable
fun SleepBuddyApp() {
    val navController = rememberNavController()
    val sleepGoalViewModel: SleepGoalViewModel = viewModel()
    val sleepGoal by sleepGoalViewModel.sleepGoal.collectAsState(null)

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onSetGoalClick = {
                    navController.navigate("set_goal")
                }
            )
        }
        composable("set_goal") {
            sleepGoal?.let { currentGoal ->
                SetGoalScreen(
                    initialGoal = currentGoal,
                    onSaveGoal = { newGoal ->
                        sleepGoalViewModel.updateSleepGoal(newGoal)
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}