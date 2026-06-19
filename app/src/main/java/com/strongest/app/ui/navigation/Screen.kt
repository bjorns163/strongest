package com.strongest.app.ui.navigation

sealed class Screen(val route: String) {
    object Workout : Screen("workout")
    object ActiveWorkout : Screen("workout/active")
    object ActiveWorkoutResume : Screen("workout/active/resume/{id}") {
        fun createRoute(id: Long) = "workout/active/resume/$id"
    }
    object ActiveWorkoutFromRoutine : Screen("workout/active/{routineId}") {
        fun createRoute(routineId: Long) = "workout/active/$routineId"
    }
    object ExercisePicker : Screen("workout/active/picker")
    object ExercisePickerForRoutine : Screen("routines/builder/picker")
    object Routines : Screen("routines")
    object RoutineBuilder : Screen("routines/builder")
    object RoutineBuilderWithId : Screen("routines/builder/{id}") {
        fun createRoute(id: Long) = "routines/builder/$id"
    }
    object History : Screen("history")
    object WorkoutDetail : Screen("history/{id}") {
        fun createRoute(id: Long) = "history/$id"
    }
    object Progress : Screen("progress")
    object ExerciseDetail : Screen("exercise/{id}") {
        fun createRoute(id: Long) = "exercise/$id"
    }
    object Measurements : Screen("measurements")
    object MeasurementDetail : Screen("measurements/{metric}") {
        fun createRoute(metric: String) = "measurements/$metric"
    }
    object Settings : Screen("settings")
}
