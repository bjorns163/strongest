package com.strongest.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.strongest.app.ui.exercise.ExercisePickerScreen
import com.strongest.app.ui.history.HistoryScreen
import com.strongest.app.ui.progress.ProgressScreen
import com.strongest.app.ui.routines.RoutinesScreen
import com.strongest.app.ui.settings.SettingsScreen
import com.strongest.app.ui.workout.WorkoutScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Workout.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Workout.route) {
                WorkoutScreen(
                    onExerciseClick = { navController.navigate(Screen.ExerciseDetail.createRoute(it)) },
                    onStartWorkout = { navController.navigate(Screen.ActiveWorkout.route) },
                    onResumeWorkout = { navController.navigate(Screen.ActiveWorkoutResume.createRoute(it)) },
                    onRoutineSelect = { navController.navigate(Screen.ActiveWorkoutFromRoutine.createRoute(it)) }
                )
            }
            composable(Screen.ActiveWorkout.route) {
                com.strongest.app.ui.workout.ActiveWorkoutScreen(
                    onBack = { navController.popBackStack() },
                    onAddExercise = { navController.navigate(Screen.ExercisePicker.route) },
                    onNavigateToReplacePicker = { navController.navigate(Screen.ExercisePicker.route) },
                    onViewExerciseDetail = { navController.navigate(Screen.ExerciseDetail.createRoute(it)) }
                )
            }
            composable(Screen.ActiveWorkoutResume.route) { backStackEntry ->
                val workoutId = backStackEntry.arguments?.getString("id")?.toLongOrNull()
                com.strongest.app.ui.workout.ActiveWorkoutScreen(
                    onBack = { navController.popBackStack() },
                    onAddExercise = { navController.navigate(Screen.ExercisePicker.route) },
                    onNavigateToReplacePicker = { navController.navigate(Screen.ExercisePicker.route) },
                    onViewExerciseDetail = { navController.navigate(Screen.ExerciseDetail.createRoute(it)) },
                    resumeWorkoutId = workoutId
                )
            }
            composable(Screen.ActiveWorkoutFromRoutine.route) { backStackEntry ->
                val routineId = backStackEntry.arguments?.getString("routineId")?.toLongOrNull()
                com.strongest.app.ui.workout.ActiveWorkoutScreen(
                    onBack = { navController.popBackStack() },
                    onAddExercise = { navController.navigate(Screen.ExercisePicker.route) },
                    onNavigateToReplacePicker = { navController.navigate(Screen.ExercisePicker.route) },
                    onViewExerciseDetail = { navController.navigate(Screen.ExerciseDetail.createRoute(it)) },
                    initialRoutineId = routineId
                )
            }
            composable(Screen.ExercisePicker.route) {
                ExercisePickerScreen(
                    onExercisesSelected = { _ ->
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Routines.route) {
                RoutinesScreen(
                    onCreateNew = { navController.navigate(Screen.RoutineBuilder.route) },
                    onRoutineClick = { navController.navigate(Screen.RoutineBuilderWithId.createRoute(it)) }
                )
            }
            composable(Screen.RoutineBuilder.route) {
                com.strongest.app.ui.routines.RoutineBuilderScreen(
                    onBack = { navController.popBackStack() },
                    onAddExercise = { navController.navigate(Screen.ExercisePickerForRoutine.route) },
                    onNavigateToReplacePicker = { navController.navigate(Screen.ExercisePickerForRoutine.route) },
                    onViewExerciseDetail = { navController.navigate(Screen.ExerciseDetail.createRoute(it)) }
                )
            }
            composable(Screen.RoutineBuilderWithId.route) { backStackEntry ->
                val routineId = backStackEntry.arguments?.getString("id")?.toLongOrNull()
                com.strongest.app.ui.routines.RoutineBuilderScreen(
                    onBack = { navController.popBackStack() },
                    onAddExercise = { navController.navigate(Screen.ExercisePickerForRoutine.route) },
                    onNavigateToReplacePicker = { navController.navigate(Screen.ExercisePickerForRoutine.route) },
                    onViewExerciseDetail = { navController.navigate(Screen.ExerciseDetail.createRoute(it)) },
                    routineId = routineId
                )
            }
            composable(Screen.ExercisePickerForRoutine.route) {
                ExercisePickerScreen(
                    onExercisesSelected = { _ ->
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(
                    onWorkoutClick = { navController.navigate(Screen.WorkoutDetail.createRoute(it)) }
                )
            }
            composable(Screen.WorkoutDetail.route) { backStackEntry ->
                backStackEntry.arguments?.getString("id")?.toLongOrNull()?.let { workoutId ->
                    com.strongest.app.ui.workout.ActiveWorkoutScreen(
                        onBack = { navController.popBackStack() },
                        onAddExercise = {},
                        onViewExerciseDetail = { navController.navigate(Screen.ExerciseDetail.createRoute(it)) },
                        initialWorkoutId = workoutId
                    )
                }
            }
            composable(Screen.Progress.route) {
                ProgressScreen(
                    onMeasurementsClick = { navController.navigate(Screen.Measurements.route) }
                )
            }
            composable(Screen.Measurements.route) {
                com.strongest.app.ui.measurements.MeasurementsScreen(
                    onBack = { navController.popBackStack() },
                    onMetricClick = { metric ->
                        navController.navigate(Screen.MeasurementDetail.createRoute(metric.name))
                    }
                )
            }
            composable(Screen.MeasurementDetail.route) { backStackEntry ->
                val metricName = backStackEntry.arguments?.getString("metric")
                val metric = metricName?.let {
                    runCatching { com.strongest.app.data.model.BodyMetric.valueOf(it) }.getOrNull()
                }
                metric?.let {
                    com.strongest.app.ui.measurements.MeasurementDetailScreen(
                        metric = it,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
            composable(Screen.ExerciseDetail.route) { backStackEntry ->
                val exerciseId = backStackEntry.arguments?.getString("id")?.toLongOrNull()
                if (exerciseId != null) {
                    com.strongest.app.ui.exercise.ExerciseDetailScreen(
                        exerciseId = exerciseId,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        Screen.Workout to "Workout",
        Screen.Routines to "Routines",
        Screen.History to "History",
        Screen.Progress to "Progress",
        Screen.Settings to "Settings"
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { (screen, label) ->
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = when (screen) {
                            Screen.Workout -> Icons.Default.FitnessCenter
                            Screen.Routines -> Icons.AutoMirrored.Filled.ListAlt
                            Screen.History -> Icons.Default.History
                            Screen.Progress -> Icons.AutoMirrored.Filled.TrendingUp
                            Screen.Settings -> Icons.Default.Settings
                            else -> Icons.Default.FitnessCenter
                        },
                        contentDescription = label
                    )
                },
                label = { Text(label) }
            )
        }
    }
}
