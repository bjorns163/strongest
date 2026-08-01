package com.strongest.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.strongest.app.data.model.SetLog
import com.strongest.app.data.model.SetType
import com.strongest.app.data.model.Workout
import com.strongest.app.data.model.WorkoutExercise
import kotlinx.coroutines.flow.Flow

data class ExerciseHistoryEntry(
    val workoutDate: Long,
    val exerciseName: String,
    val weightKg: Float,
    val reps: Int,
    val rpe: Float?,
    val setType: SetType,
    val routineName: String?
)

data class VolumeByDate(
    val date: Long,
    val totalVolumeKg: Float,
    val totalSets: Int
)

data class PersonalRecord(
    val exerciseId: Long,
    val exerciseName: String,
    val muscleGroup: String,
    val equipment: String,
    val maxWeightKg: Float,
    val maxReps: Int,
    val workoutDate: Long
)

data class MuscleVolume(
    val muscleGroup: String,
    val totalSets: Float,
    val totalVolumeKg: Float,
    val workoutCount: Int
)

/** Per-exercise, per-workout completed-set totals, used to weight secondary-muscle contributions. */
data class ExerciseWorkoutVolume(
    val exerciseId: Long,
    val workoutId: Long,
    val sets: Int,
    val volumeKg: Float
)

/** Most recent time each primary muscle group was trained (completed sets only). */
data class MuscleLastTrained(
    val muscleGroup: String,
    val lastTrained: Long
)

data class WorkoutsPerDay(
    val dayStart: Long,
    val count: Int
)

data class HistorySetRow(
    val workoutId: Long,
    val workoutExerciseId: Long,
    val exerciseId: Long,
    val exerciseName: String,
    val muscleGroup: String = "",
    val orderIndex: Int,
    val setId: Long?,
    val setNumber: Int?,
    val weightKg: Float?,
    val reps: Int?,
    val setType: String?
)

data class ExerciseUsageCount(
    val exerciseId: Long,
    val workoutCount: Int
)

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY startTime DESC")
    fun getAllWorkouts(): Flow<List<Workout>>

    @Query("SELECT * FROM workouts ORDER BY startTime DESC")
    suspend fun getAllWorkoutsList(): List<Workout>

    @Query("SELECT * FROM workouts WHERE isOngoing = 1 ORDER BY startTime DESC LIMIT 1")
    suspend fun getOngoingWorkout(): Workout?

    @Query("SELECT * FROM workouts WHERE isOngoing = 1 ORDER BY startTime DESC")
    suspend fun getOngoingWorkouts(): List<Workout>

    @Query("SELECT * FROM workout_exercises")
    suspend fun getAllWorkoutExercises(): List<WorkoutExercise>

    @Query("SELECT * FROM sets")
    suspend fun getAllSets(): List<SetLog>

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getWorkoutById(id: Long): Workout?

    @Query("SELECT * FROM workout_exercises WHERE workoutId = :workoutId ORDER BY orderIndex ASC")
    suspend fun getWorkoutExercises(workoutId: Long): List<WorkoutExercise>

    @Query("SELECT * FROM sets WHERE workoutExerciseId = :workoutExerciseId ORDER BY setNumber ASC")
    suspend fun getSetsForExercise(workoutExerciseId: Long): List<SetLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout): Long

    @Update
    suspend fun updateWorkout(workout: Workout)

    @Delete
    suspend fun deleteWorkout(workout: Workout)

    @Insert
    suspend fun insertWorkoutExercise(workoutExercise: WorkoutExercise): Long

    @Insert
    suspend fun insertWorkoutExercises(exercises: List<WorkoutExercise>)

    @Update
    suspend fun updateWorkoutExercise(workoutExercise: WorkoutExercise)

    @Delete
    suspend fun deleteWorkoutExercise(workoutExercise: WorkoutExercise)

    @Insert
    suspend fun insertSet(setLog: SetLog): Long

    @Insert
    suspend fun insertSets(sets: List<SetLog>)

    @Update
    suspend fun updateSet(setLog: SetLog)

    @Delete
    suspend fun deleteSet(setLog: SetLog)

    @Query("""
        SELECT w.startTime as workoutDate, e.name as exerciseName,
               s.weightKg, s.reps, s.rpe, s.setType, w.routineName
        FROM sets s
        JOIN workout_exercises we ON s.workoutExerciseId = we.id
        JOIN exercises e ON we.exerciseId = e.id
        JOIN workouts w ON we.workoutId = w.id
        WHERE e.id = :exerciseId AND w.isOngoing = 0
        ORDER BY w.startTime DESC
    """)
    suspend fun getExerciseHistory(exerciseId: Long): List<ExerciseHistoryEntry>

    @Query("""
        SELECT w.startTime as workoutDate, e.name as exerciseName,
               s.weightKg, s.reps, s.rpe, s.setType, w.routineName
        FROM sets s
        JOIN workout_exercises we ON s.workoutExerciseId = we.id
        JOIN exercises e ON we.exerciseId = e.id
        JOIN workouts w ON we.workoutId = w.id
        WHERE we.exerciseId = :exerciseId AND s.setType = 'NORMAL'
        ORDER BY (s.weightKg * s.reps) DESC
        LIMIT 1
    """)
    suspend fun getPreviousBestSet(exerciseId: Long): ExerciseHistoryEntry?

    @Query("""
        SELECT w.startTime as workoutDate, e.name as exerciseName,
               s.weightKg, s.reps, s.rpe, s.setType, w.routineName
        FROM sets s
        JOIN workout_exercises we ON s.workoutExerciseId = we.id
        JOIN exercises e ON we.exerciseId = e.id
        JOIN workouts w ON we.workoutId = w.id
        WHERE we.exerciseId = :exerciseId AND w.isOngoing = 0
          AND w.startTime = (
              SELECT MAX(w2.startTime)
              FROM sets s2
              JOIN workout_exercises we2 ON s2.workoutExerciseId = we2.id
              JOIN workouts w2 ON we2.workoutId = w2.id
              WHERE we2.exerciseId = :exerciseId AND w2.isOngoing = 0
          )
        ORDER BY s.setNumber ASC
    """)
    suspend fun getLastSessionSets(exerciseId: Long): List<ExerciseHistoryEntry>

    @Query("""
        SELECT w.startTime as date,
               SUM(s.weightKg * s.reps) as totalVolumeKg,
               COUNT(s.id) as totalSets
        FROM workouts w
        JOIN workout_exercises we ON w.id = we.workoutId
        JOIN sets s ON we.id = s.workoutExerciseId
        WHERE w.isOngoing = 0 AND w.startTime >= :startDate
        GROUP BY w.startTime
        ORDER BY w.startTime ASC
    """)
    suspend fun getVolumeByDate(startDate: Long): List<VolumeByDate>

    @Query("""
        SELECT we.exerciseId AS exerciseId,
               e.name AS exerciseName,
               e.muscleGroup AS muscleGroup,
               e.equipment AS equipment,
               s.weightKg AS maxWeightKg,
               s.reps AS maxReps,
               w.startTime AS workoutDate
        FROM sets s
        JOIN workout_exercises we ON s.workoutExerciseId = we.id
        JOIN exercises e ON we.exerciseId = e.id
        JOIN workouts w ON we.workoutId = w.id
        WHERE w.isOngoing = 0 AND s.completedAt > 0 AND s.setType != 'WARM_UP'
          AND s.id = (
            SELECT s2.id
            FROM sets s2
            JOIN workout_exercises we2 ON s2.workoutExerciseId = we2.id
            JOIN workouts w2 ON we2.workoutId = w2.id
            WHERE we2.exerciseId = we.exerciseId
              AND w2.isOngoing = 0 AND s2.completedAt > 0 AND s2.setType != 'WARM_UP'
            ORDER BY s2.weightKg DESC, s2.reps DESC, s2.completedAt ASC
            LIMIT 1
          )
        ORDER BY e.name ASC
    """)
    suspend fun getAllPersonalRecords(): List<PersonalRecord>

    @Query("""
        SELECT we.exerciseId AS exerciseId,
               w.id AS workoutId,
               COUNT(s.id) AS sets,
               SUM(s.weightKg * s.reps) AS volumeKg
        FROM sets s
        JOIN workout_exercises we ON s.workoutExerciseId = we.id
        JOIN workouts w ON we.workoutId = w.id
        WHERE w.isOngoing = 0 AND w.startTime >= :startDate AND s.completedAt > 0
        GROUP BY we.exerciseId, w.id
    """)
    suspend fun getExerciseWorkoutVolume(startDate: Long): List<ExerciseWorkoutVolume>

    @Query("""
        SELECT e.muscleGroup AS muscleGroup, MAX(w.startTime) AS lastTrained
        FROM sets s
        JOIN workout_exercises we ON s.workoutExerciseId = we.id
        JOIN exercises e ON we.exerciseId = e.id
        JOIN workouts w ON we.workoutId = w.id
        WHERE w.isOngoing = 0 AND s.completedAt > 0
        GROUP BY e.muscleGroup
    """)
    suspend fun getMuscleLastTrained(): List<MuscleLastTrained>

    /**
     * Buckets workouts by local calendar day. The caller supplies [tzOffsetMs] (e.g.
     * `TimeZone.getDefault().getOffset(now)`) so the same workout is grouped under the same date
     * the user actually sees in their UI, regardless of which side of UTC midnight it sits on.
     */
    @Query("""
        SELECT (((w.startTime + :tzOffsetMs) / 86400000) * 86400000) - :tzOffsetMs AS dayStart,
               COUNT(DISTINCT w.id) AS count
        FROM workouts w
        WHERE w.isOngoing = 0 AND w.startTime >= :startDate
        GROUP BY dayStart
        ORDER BY dayStart ASC
    """)
    suspend fun getWorkoutsPerDay(startDate: Long, tzOffsetMs: Long): List<WorkoutsPerDay>

    @Query("""
        SELECT we.exerciseId AS exerciseId, COUNT(DISTINCT w.id) AS workoutCount
        FROM workout_exercises we
        JOIN workouts w ON we.workoutId = w.id
        JOIN sets s ON s.workoutExerciseId = we.id
        WHERE w.isOngoing = 0 AND s.completedAt > 0
        GROUP BY we.exerciseId
    """)
    fun getExerciseUsageCounts(): Flow<List<ExerciseUsageCount>>

    @Query("""
        SELECT we.workoutId AS workoutId,
               we.id AS workoutExerciseId,
               we.exerciseId AS exerciseId,
               e.name AS exerciseName,
               e.muscleGroup AS muscleGroup,
               we.orderIndex AS orderIndex,
               s.id AS setId,
               s.setNumber AS setNumber,
               s.weightKg AS weightKg,
               s.reps AS reps,
               s.setType AS setType
        FROM workouts w
        JOIN workout_exercises we ON we.workoutId = w.id
        JOIN exercises e ON we.exerciseId = e.id
        LEFT JOIN sets s ON s.workoutExerciseId = we.id AND s.completedAt > 0
        WHERE w.isOngoing = 0
        ORDER BY w.id ASC, we.orderIndex ASC, s.setNumber ASC
    """)
    fun getAllCompletedHistoryRows(): Flow<List<HistorySetRow>>

    @Query("DELETE FROM workouts")
    suspend fun deleteAllWorkouts()
}
