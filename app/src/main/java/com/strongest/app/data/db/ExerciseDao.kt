package com.strongest.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.strongest.app.data.model.Equipment
import com.strongest.app.data.model.Exercise
import com.strongest.app.data.model.ExerciseNote
import com.strongest.app.data.model.ExerciseSettings
import com.strongest.app.data.model.MuscleGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises ORDER BY name ASC")
    suspend fun getAllExercisesList(): List<Exercise>

    @Query("SELECT * FROM exercises WHERE muscleGroup = :muscleGroup ORDER BY name ASC")
    fun getExercisesByMuscleGroup(muscleGroup: MuscleGroup): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE equipment = :equipment ORDER BY name ASC")
    fun getExercisesByEquipment(equipment: Equipment): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE isCustom = 1 ORDER BY name ASC")
    fun getCustomExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE isCustom = 1 ORDER BY name ASC")
    suspend fun getCustomExercisesList(): List<Exercise>

    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchExercises(query: String): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: Long): Exercise?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<Exercise>)

    @Update
    suspend fun updateExercise(exercise: Exercise)

    @Update
    suspend fun updateExercises(exercises: List<Exercise>)

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Query("DELETE FROM exercises WHERE isCustom = 1")
    suspend fun deleteAllCustomExercises()

    @Query("SELECT DISTINCT muscleGroup FROM exercises ORDER BY muscleGroup")
    suspend fun getAllMuscleGroups(): List<MuscleGroup>

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getExerciseCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: ExerciseNote)

    @Query("SELECT * FROM exercise_notes WHERE exerciseId = :exerciseId")
    suspend fun getNote(exerciseId: Long): ExerciseNote?

    @Query("SELECT * FROM exercise_notes")
    suspend fun getAllNotes(): List<ExerciseNote>

    @Query("SELECT * FROM exercise_settings WHERE exerciseId = :exerciseId")
    suspend fun getExerciseSettings(exerciseId: Long): ExerciseSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExerciseSettings(settings: ExerciseSettings)
}
