package com.strongest.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.strongest.app.data.model.Exercise
import com.strongest.app.data.model.ExerciseNote
import com.strongest.app.data.model.ExerciseSettings
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
        ExerciseSettings::class,
        Workout::class,
        WorkoutExercise::class,
        SetLog::class,
        MeasurementEntry::class
    ],
    version = 5,
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

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE routine_sets ADD COLUMN setType TEXT NOT NULL DEFAULT 'NORMAL'"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS exercise_settings (" +
                        "exerciseId INTEGER NOT NULL PRIMARY KEY, " +
                        "warmUpSetCount INTEGER NOT NULL)"
                )
            }
        }

        /** Remembers how the plate calculator is set up per exercise. */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE exercise_settings ADD COLUMN barWeightKg REAL")
                db.execSQL(
                    "ALTER TABLE exercise_settings " +
                        "ADD COLUMN plateSingleSide INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        /**
         * Replaces `classification` (which mixed the biomechanical category Compound/Isolation
         * with the programming role Accessory) by `type`. Accessory lifts are compound movements,
         * so they fold into COMPOUND; built-ins then pick up their exact type from the seed
         * re-sync.
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS exercises_new (" +
                        "id INTEGER NOT NULL PRIMARY KEY, " +
                        "name TEXT NOT NULL, " +
                        "muscleGroup TEXT NOT NULL, " +
                        "equipment TEXT NOT NULL, " +
                        "description TEXT NOT NULL, " +
                        "instructions TEXT NOT NULL, " +
                        "secondaryMuscles TEXT NOT NULL, " +
                        "imageUrl TEXT NOT NULL, " +
                        "isCustom INTEGER NOT NULL, " +
                        "type TEXT NOT NULL)"
                )
                db.execSQL(
                    "INSERT INTO exercises_new (id, name, muscleGroup, equipment, description, " +
                        "instructions, secondaryMuscles, imageUrl, isCustom, type) " +
                        "SELECT id, name, muscleGroup, equipment, description, instructions, " +
                        "secondaryMuscles, imageUrl, isCustom, " +
                        "CASE classification WHEN 'ISOLATION' THEN 'ISOLATION' ELSE 'COMPOUND' END " +
                        "FROM exercises"
                )
                db.execSQL("DROP TABLE exercises")
                db.execSQL("ALTER TABLE exercises_new RENAME TO exercises")
            }
        }
    }
}
