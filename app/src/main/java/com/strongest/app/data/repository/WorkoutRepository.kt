package com.strongest.app.data.repository

import com.strongest.app.data.db.ExerciseDao
import com.strongest.app.data.db.ExerciseHistoryEntry
import com.strongest.app.data.db.ExerciseUsageCount
import com.strongest.app.data.db.HistorySetRow
import com.strongest.app.data.db.MuscleVolume
import com.strongest.app.data.db.PersonalRecord
import com.strongest.app.data.db.RoutineDao
import com.strongest.app.data.db.RoutineWithExercisesAndSets
import com.strongest.app.data.db.VolumeByDate
import com.strongest.app.data.db.WorkoutDao
import com.strongest.app.data.db.WorkoutsPerDay
import com.strongest.app.data.model.Exercise
import com.strongest.app.data.model.ExerciseNote
import com.strongest.app.data.model.MuscleGroup
import com.strongest.app.data.model.Routine
import com.strongest.app.data.model.RoutineExercise
import com.strongest.app.data.model.RoutineGroup
import com.strongest.app.data.model.RoutineSet
import com.strongest.app.data.model.SECONDARY_MUSCLE_WEIGHT
import com.strongest.app.data.model.SetLog
import com.strongest.app.data.model.Workout
import com.strongest.app.data.model.WorkoutExercise
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

data class WorkoutWithDetails(
    val workout: Workout,
    val exercises: List<WorkoutExerciseWithSets>
)

data class WorkoutExerciseWithSets(
    val workoutExercise: WorkoutExercise,
    val sets: List<SetLog>
)

@Singleton
class WorkoutRepository @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val routineDao: RoutineDao,
    private val workoutDao: WorkoutDao,
    private val settingsRepository: SettingsRepository
) {
    fun getAllExercises(): Flow<List<Exercise>> = exerciseDao.getAllExercises()

    fun getExercisesByMuscleGroup(muscleGroup: MuscleGroup): Flow<List<Exercise>> =
        exerciseDao.getExercisesByMuscleGroup(muscleGroup)

    fun searchExercises(query: String): Flow<List<Exercise>> = exerciseDao.searchExercises(query)

    fun getCustomExercises(): Flow<List<Exercise>> = exerciseDao.getCustomExercises()

    suspend fun getExerciseById(id: Long): Exercise? = exerciseDao.getExerciseById(id)

    suspend fun getAllExercisesList(): List<Exercise> = exerciseDao.getAllExercisesList()

    suspend fun insertCustomExercise(exercise: Exercise): Long =
        exerciseDao.insertExercise(exercise.copy(isCustom = true))

    suspend fun deleteCustomExercise(exercise: Exercise) = exerciseDao.deleteExercise(exercise)

    suspend fun updateExercise(exercise: Exercise) = exerciseDao.updateExercise(exercise)

    fun getAllRoutines(): Flow<List<Routine>> = routineDao.getAllRoutines()

    suspend fun getRoutineWithExercises(id: Long): Pair<Routine?, List<RoutineExercise>> =
        routineDao.getRoutineWithExercises(id)

    suspend fun getRoutineWithExercisesAndSets(id: Long): RoutineWithExercisesAndSets? =
        routineDao.getRoutineWithExercisesAndSets(id)

    suspend fun saveRoutine(
        name: String,
        description: String,
        exercises: List<RoutineExercise>,
        routineSets: Map<Long, List<RoutineSet>>,
        groupId: Long? = null
    ): Long {
        val routine = Routine(
            name = name,
            description = description,
            groupId = groupId,
            updatedAt = System.currentTimeMillis()
        )
        val routineId = routineDao.insertRoutine(routine)
        if (exercises.isNotEmpty()) {
            val insertedIds = mutableListOf<Long>()
            for ((index, re) in exercises.withIndex()) {
                val id = routineDao.insertRoutineExercise(
                    re.copy(routineId = routineId, id = 0)
                )
                insertedIds.add(id)
            }
            // Insert individual sets for each exercise
            val allSets = mutableListOf<RoutineSet>()
            for ((localIdx, insertedId) in insertedIds.withIndex()) {
                val localExerciseId = exercises[localIdx].id // The temp ID from the ViewModel
                val setsForExercise = routineSets[localExerciseId] ?: emptyList()
                allSets.addAll(setsForExercise.map { it.copy(routineExerciseId = insertedId, id = 0) })
            }
            if (allSets.isNotEmpty()) {
                routineDao.insertRoutineSets(allSets)
            }
        }
        return routineId
    }

    suspend fun saveRoutineExercises(
        routineId: Long,
        exercises: List<RoutineExercise>,
        routineSets: Map<Long, List<RoutineSet>>
    ) {
        routineDao.deleteRoutineExercises(routineId)
        routineDao.deleteRoutineSetsForRoutine(routineId)
        if (exercises.isNotEmpty()) {
            val insertedIds = mutableListOf<Long>()
            for ((index, re) in exercises.withIndex()) {
                val id = routineDao.insertRoutineExercise(
                    re.copy(routineId = routineId, id = 0)
                )
                insertedIds.add(id)
            }
            // Insert individual sets for each exercise
            val allSets = mutableListOf<RoutineSet>()
            for ((localIdx, insertedId) in insertedIds.withIndex()) {
                val localExerciseId = exercises[localIdx].id
                val setsForExercise = routineSets[localExerciseId] ?: emptyList()
                allSets.addAll(setsForExercise.map { it.copy(routineExerciseId = insertedId, id = 0) })
            }
            if (allSets.isNotEmpty()) {
                routineDao.insertRoutineSets(allSets)
            }
        }
    }

    suspend fun updateRoutine(routine: Routine) = routineDao.updateRoutine(routine)

    suspend fun getRoutineById(routineId: Long): Routine? = routineDao.getRoutineById(routineId)

    suspend fun getRoutineGroupById(groupId: Long): RoutineGroup? = routineDao.getRoutineGroupById(groupId)

    fun getAllRoutineGroups(): Flow<List<RoutineGroup>> = routineDao.getAllRoutineGroups()

    suspend fun createRoutineGroup(name: String): Long {
        val now = System.currentTimeMillis()
        return routineDao.insertRoutineGroup(
            RoutineGroup(name = name, orderIndex = 0, createdAt = now, updatedAt = now)
        )
    }

    suspend fun renameRoutineGroup(groupId: Long, newName: String) {
        val group = routineDao.getRoutineGroupById(groupId) ?: return
        routineDao.updateRoutineGroup(group.copy(name = newName, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteRoutineGroup(groupId: Long) {
        val group = routineDao.getRoutineGroupById(groupId) ?: return
        routineDao.clearGroupIdForRoutines(groupId)
        routineDao.deleteRoutineGroup(group)
    }

    suspend fun setRoutineGroup(routineId: Long, groupId: Long?) {
        routineDao.setRoutineGroup(routineId, groupId, System.currentTimeMillis())
    }

    /**
     * Replaces the sets of every routine exercise in [routineId] in the order they appear.
     * Caller must pass [setsByPosition] sized to match the existing routine exercises (i.e. only
     * after confirming there are no structural changes); extra entries are ignored, missing
     * positions skip the corresponding routine exercise. Position-based mapping lets the same
     * exercise appear multiple times in a routine without one entry overwriting another.
     */
    suspend fun updateRoutineSetsOnly(
        routineId: Long,
        setsByPosition: List<List<RoutineSet>>
    ) {
        val routine = routineDao.getRoutineById(routineId) ?: return
        val existing = routineDao.getRoutineExercises(routineId)
        for ((idx, re) in existing.withIndex()) {
            val newSets = setsByPosition.getOrNull(idx) ?: continue
            routineDao.deleteRoutineSetsForExercise(re.id)
            val toInsert = newSets.mapIndexed { sIdx, s ->
                s.copy(id = 0, routineExerciseId = re.id, setNumber = sIdx + 1)
            }
            if (toInsert.isNotEmpty()) {
                routineDao.insertRoutineSets(toInsert)
            }
        }
        routineDao.updateRoutine(routine.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteRoutine(routine: Routine) {
        routineDao.deleteRoutineExercises(routine.id)
        routineDao.deleteRoutine(routine)
    }

    suspend fun getPreviousSessionSets(exerciseId: Long): List<ExerciseHistoryEntry> =
        workoutDao.getLastSessionSets(exerciseId)

    suspend fun getExerciseHistory(exerciseId: Long): List<ExerciseHistoryEntry> =
        workoutDao.getExerciseHistory(exerciseId)

    private val startWorkoutMutex = Mutex()

    /**
     * Starts a workout, or returns the id of the existing ongoing workout if one is already in progress.
     * Serialised via a mutex to prevent racing callers from creating duplicate "ongoing" rows.
     */
    suspend fun startWorkout(routineId: Long? = null, routineName: String? = null, workoutName: String? = null): Long =
        startWorkoutMutex.withLock {
            workoutDao.getOngoingWorkout()?.let { return@withLock it.id }
            val workout = Workout(
                routineId = routineId,
                routineName = routineName,
                workoutName = workoutName,
                isOngoing = true
            )
            workoutDao.insertWorkout(workout)
        }

    suspend fun finishWorkout(workoutId: Long, workoutName: String? = null) {
        val workout = workoutDao.getWorkoutById(workoutId)
        if (workout != null) {
            workoutDao.updateWorkout(
                workout.copy(
                    endTime = System.currentTimeMillis(),
                    isOngoing = false,
                    workoutName = workoutName ?: workout.workoutName
                )
            )
        }
    }

    suspend fun getOngoingWorkout(): Workout? =
        workoutDao.getOngoingWorkout()

    suspend fun addExerciseToWorkout(workoutId: Long, exerciseId: Long, orderIndex: Int): Long {
        val workoutExercise = WorkoutExercise(
            workoutId = workoutId,
            exerciseId = exerciseId,
            orderIndex = orderIndex
        )
        return workoutDao.insertWorkoutExercise(workoutExercise)
    }

    suspend fun removeExerciseFromWorkout(workoutExerciseId: Long) {
        val workoutExercises = workoutDao.getAllWorkoutExercises()
        val toRemove = workoutExercises.find { it.id == workoutExerciseId }
        if (toRemove != null) {
            workoutDao.deleteWorkoutExercise(toRemove)
        }
    }

    /**
     * Removes any exercises in the workout that have no completed sets, so empty exercises
     * (e.g. all sets discarded when finishing) don't linger in history or the workout detail view.
     * Returns the ids of the workout_exercises that were removed.
     */
    suspend fun removeEmptyWorkoutExercises(workoutId: Long): List<Long> {
        val removed = mutableListOf<Long>()
        for (we in workoutDao.getWorkoutExercises(workoutId)) {
            val sets = workoutDao.getSetsForExercise(we.id)
            if (sets.none { it.completedAt > 0 }) {
                workoutDao.deleteWorkoutExercise(we)
                removed.add(we.id)
            }
        }
        return removed
    }

    suspend fun updateWorkoutExercise(workoutExercise: WorkoutExercise) =
        workoutDao.updateWorkoutExercise(workoutExercise)

    suspend fun logSet(
        workoutExerciseId: Long,
        setNumber: Int,
        weightKg: Float,
        reps: Int,
        rpe: Float?,
        setType: com.strongest.app.data.model.SetType,
        restSeconds: Int = 90,
        completedAt: Long = System.currentTimeMillis()
    ): Long {
        val set = SetLog(
            workoutExerciseId = workoutExerciseId,
            setNumber = setNumber,
            weightKg = weightKg,
            reps = reps,
            rpe = rpe,
            setType = setType,
            restSeconds = restSeconds,
            completedAt = completedAt
        )
        return workoutDao.insertSet(set)
    }

    suspend fun updateSet(set: SetLog) = workoutDao.updateSet(set)

    suspend fun deleteSet(set: SetLog) = workoutDao.deleteSet(set)

    suspend fun upsertSets(sets: List<SetLog>) {
        sets.forEach { set ->
            if (set.id != 0L) {
                workoutDao.updateSet(set)
            } else {
                workoutDao.insertSet(set)
            }
        }
    }

    suspend fun deleteSetsForExercise(workoutExerciseId: Long) {
        val existing = workoutDao.getSetsForExercise(workoutExerciseId)
        existing.forEach { workoutDao.deleteSet(it) }
    }

    suspend fun deleteExercisesForWorkout(workoutId: Long) {
        val exercises = workoutDao.getWorkoutExercises(workoutId)
        exercises.forEach { workoutDao.deleteWorkoutExercise(it) }
    }

    suspend fun getWorkoutWithDetails(workoutId: Long): WorkoutWithDetails? {
        val workout = workoutDao.getWorkoutById(workoutId) ?: return null
        val workoutExercises = workoutDao.getWorkoutExercises(workoutId)
        val exercisesWithSets = workoutExercises.map { we ->
            val sets = workoutDao.getSetsForExercise(we.id)
            WorkoutExerciseWithSets(we, sets)
        }
        return WorkoutWithDetails(workout, exercisesWithSets)
    }

    fun getAllWorkouts(): Flow<List<Workout>> = workoutDao.getAllWorkouts()

    fun getExerciseUsageCounts(): Flow<List<ExerciseUsageCount>> = workoutDao.getExerciseUsageCounts()

    suspend fun getAllPersonalRecords(): List<PersonalRecord> = workoutDao.getAllPersonalRecords()

    suspend fun getVolumeByDate(startDate: Long): List<VolumeByDate> = workoutDao.getVolumeByDate(startDate)

    /**
     * Aggregates completed-set volume by muscle group, crediting each exercise's primary muscle
     * fully and every secondary muscle at [SECONDARY_MUSCLE_WEIGHT]. Because secondary muscles are
     * stored as a serialized list (not SQL-groupable), the weighting is done in memory.
     */
    suspend fun getMuscleVolume(startDate: Long): List<MuscleVolume> {
        val rows = workoutDao.getExerciseWorkoutVolume(startDate)
        if (rows.isEmpty()) return emptyList()

        val musclesByExerciseId = exerciseDao.getAllExercisesList()
            .associate { it.id to (listOf(it.muscleGroup to 1f) + it.secondaryMuscles.map { m -> m to SECONDARY_MUSCLE_WEIGHT }) }

        data class Acc(var sets: Float = 0f, var volume: Float = 0f, val workoutIds: MutableSet<Long> = mutableSetOf())
        val byMuscle = mutableMapOf<MuscleGroup, Acc>()

        for (row in rows) {
            val contributions = musclesByExerciseId[row.exerciseId] ?: continue
            for ((muscle, weight) in contributions) {
                val acc = byMuscle.getOrPut(muscle) { Acc() }
                acc.sets += row.sets * weight
                acc.volume += row.volumeKg * weight
                acc.workoutIds.add(row.workoutId)
            }
        }

        return byMuscle.entries
            .map { (muscle, acc) ->
                MuscleVolume(
                    muscleGroup = muscle.name,
                    totalSets = acc.sets,
                    totalVolumeKg = acc.volume,
                    workoutCount = acc.workoutIds.size
                )
            }
            .sortedByDescending { it.totalVolumeKg }
    }

    suspend fun getMuscleLastTrained(): List<com.strongest.app.data.db.MuscleLastTrained> =
        workoutDao.getMuscleLastTrained()

    suspend fun getWorkoutsPerDay(startDate: Long): List<WorkoutsPerDay> {
        val tzOffsetMs = java.util.TimeZone.getDefault().getOffset(System.currentTimeMillis()).toLong()
        return workoutDao.getWorkoutsPerDay(startDate, tzOffsetMs)
    }

    fun getAllCompletedHistoryRows(): Flow<List<HistorySetRow>> = workoutDao.getAllCompletedHistoryRows()

    suspend fun updateWorkout(workout: Workout) = workoutDao.updateWorkout(workout)

    suspend fun getWorkoutById(id: Long): Workout? =
        workoutDao.getWorkoutById(id)

    suspend fun deleteWorkout(workout: Workout) =
        workoutDao.deleteWorkout(workout)

    suspend fun seedExercisesIfEmpty() {
        val count = exerciseDao.getExerciseCount()
        if (count == 0) {
            exerciseDao.insertExercises(exerciseSeedData)
            // Fresh installs already have classifications baked into the seed data.
            settingsRepository.markExerciseClassificationBackfillDone()
            settingsRepository.setExerciseSeedVersion(EXERCISE_SEED_VERSION)
        } else {
            // Insert any seed exercises whose IDs aren't in the DB yet (newly added entries).
            // We check by ID so existing exercises (including user-modified ones) are never overwritten.
            val existingIds = exerciseDao.getAllExercisesList().map { it.id }.toSet()
            val newExercises = exerciseSeedData.filter { it.id !in existingIds }
            if (newExercises.isNotEmpty()) {
                exerciseDao.insertExercises(newExercises)
            }
            if (!settingsRepository.isExerciseClassificationBackfillDone()) {
                backfillExerciseClassifications()
                settingsRepository.markExerciseClassificationBackfillDone()
            }
            // Re-sync built-in exercise definitions when the seed data version was bumped, so edits
            // to names/descriptions/instructions reach existing installs without a reinstall.
            if (settingsRepository.getExerciseSeedVersion() < EXERCISE_SEED_VERSION) {
                syncSeedExerciseDetails()
                settingsRepository.setExerciseSeedVersion(EXERCISE_SEED_VERSION)
            }
        }
    }

    /**
     * Updates the definition fields (name, muscle group, equipment, description, instructions,
     * classification) of built-in exercises to match the current seed data. Custom exercises and
     * all user data (workout history, sets, notes, routines) are left untouched, since those live
     * in separate tables keyed by exerciseId.
     */
    private suspend fun syncSeedExerciseDetails() {
        val existingById = exerciseDao.getAllExercisesList().associateBy { it.id }
        val updates = mutableListOf<Exercise>()
        for (seed in exerciseSeedData) {
            val current = existingById[seed.id] ?: continue
            if (current.isCustom) continue
            val merged = current.copy(
                name = seed.name,
                muscleGroup = seed.muscleGroup,
                equipment = seed.equipment,
                description = seed.description,
                instructions = seed.instructions,
                secondaryMuscles = seed.secondaryMuscles,
                classification = seed.classification
            )
            if (merged != current) updates.add(merged)
        }
        if (updates.isNotEmpty()) {
            exerciseDao.updateExercises(updates)
        }
    }

    private suspend fun backfillExerciseClassifications() {
        val all = exerciseDao.getAllExercisesList()
        for (ex in all) {
            if (ex.isCustom) continue
            val computed = com.strongest.app.data.model.classifyExercise(ex.name, ex.equipment, ex.muscleGroup)
            if (ex.classification != computed) {
                exerciseDao.updateExercise(ex.copy(classification = computed))
            }
        }
    }

    suspend fun getNote(exerciseId: Long): ExerciseNote? = exerciseDao.getNote(exerciseId)

    suspend fun upsertNote(note: ExerciseNote) = exerciseDao.upsertNote(note)
}
