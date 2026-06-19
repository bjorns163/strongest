package com.strongest.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.strongest.app.data.model.Exercise
import com.strongest.app.data.model.ExerciseNote
import com.strongest.app.data.model.MeasurementEntry
import com.strongest.app.data.model.Routine
import com.strongest.app.data.model.RoutineExercise
import com.strongest.app.data.model.RoutineGroup
import com.strongest.app.data.model.RoutineSet
import com.strongest.app.data.model.SetLog
import com.strongest.app.data.model.Workout
import com.strongest.app.data.model.WorkoutExercise

@Database(
    entities = [
        Exercise::class,
        Routine::class,
        RoutineExercise::class,
        RoutineSet::class,
        RoutineGroup::class,
        ExerciseNote::class,
        Workout::class,
        WorkoutExercise::class,
        SetLog::class,
        MeasurementEntry::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class StrongestDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun routineDao(): RoutineDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun measurementEntryDao(): MeasurementEntryDao

    companion object {
        const val DATABASE_NAME = "strongest.db"
    }
}
