package com.strongest.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.strongest.app.data.model.Routine
import com.strongest.app.data.model.RoutineExercise
import com.strongest.app.data.model.RoutineGroup
import com.strongest.app.data.model.RoutineSet
import kotlinx.coroutines.flow.Flow

data class RoutineWithExercisesAndSets(
    val routine: Routine,
    val exercises: List<RoutineExercise>,
    val sets: Map<Long, List<RoutineSet>>  // keyed by routineExerciseId
)

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines ORDER BY updatedAt DESC")
    fun getAllRoutines(): Flow<List<Routine>>

    @Query("SELECT * FROM routines ORDER BY updatedAt DESC")
    suspend fun getAllRoutinesList(): List<Routine>

    @Query("SELECT * FROM routine_exercises")
    suspend fun getAllRoutineExercisesList(): List<RoutineExercise>

    @Query("SELECT * FROM routines WHERE id = :id")
    suspend fun getRoutineById(id: Long): Routine?

    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY orderIndex ASC")
    suspend fun getRoutineExercises(routineId: Long): List<RoutineExercise>

    @Query("SELECT * FROM routine_sets WHERE routineExerciseId IN (:exerciseIds) ORDER BY setNumber ASC")
    suspend fun getRoutineSetsForExercises(exerciseIds: List<Long>): List<RoutineSet>

    @Transaction
    suspend fun getRoutineWithExercises(id: Long): Pair<Routine?, List<RoutineExercise>> {
        val routine = getRoutineById(id)
        val exercises = getRoutineExercises(id)
        return routine to exercises
    }

    @Transaction
    suspend fun getRoutineWithExercisesAndSets(id: Long): RoutineWithExercisesAndSets? {
        val routine = getRoutineById(id) ?: return null
        val exercises = getRoutineExercises(id)
        val exerciseIds = exercises.map { it.id }
        val sets = if (exerciseIds.isNotEmpty()) {
            getRoutineSetsForExercises(exerciseIds).groupBy { it.routineExerciseId }
        } else {
            emptyMap()
        }
        return RoutineWithExercisesAndSets(routine, exercises, sets)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: Routine): Long

    @Update
    suspend fun updateRoutine(routine: Routine)

    @Delete
    suspend fun deleteRoutine(routine: Routine)

    @Insert
    suspend fun insertRoutineExercise(routineExercise: RoutineExercise): Long

    @Insert
    suspend fun insertRoutineExercises(routineExercises: List<RoutineExercise>)

    @Insert
    suspend fun insertRoutineSets(sets: List<RoutineSet>)

    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId")
    suspend fun deleteRoutineExercises(routineId: Long)

    @Query("DELETE FROM routine_sets WHERE routineExerciseId IN (SELECT id FROM routine_exercises WHERE routineId = :routineId)")
    suspend fun deleteRoutineSetsForRoutine(routineId: Long)

    @Query("DELETE FROM routine_sets WHERE routineExerciseId = :routineExerciseId")
    suspend fun deleteRoutineSetsForExercise(routineExerciseId: Long)

    @Query("SELECT COUNT(*) FROM routines")
    suspend fun getRoutineCount(): Int

    @Query("SELECT * FROM routine_groups ORDER BY orderIndex ASC, name ASC")
    fun getAllRoutineGroups(): Flow<List<RoutineGroup>>

    @Query("SELECT * FROM routine_groups WHERE id = :id")
    suspend fun getRoutineGroupById(id: Long): RoutineGroup?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineGroup(group: RoutineGroup): Long

    @Update
    suspend fun updateRoutineGroup(group: RoutineGroup)

    @Delete
    suspend fun deleteRoutineGroup(group: RoutineGroup)

    @Query("UPDATE routines SET groupId = NULL WHERE groupId = :groupId")
    suspend fun clearGroupIdForRoutines(groupId: Long)

    @Query("UPDATE routines SET groupId = :groupId, updatedAt = :updatedAt WHERE id = :routineId")
    suspend fun setRoutineGroup(routineId: Long, groupId: Long?, updatedAt: Long)
}
