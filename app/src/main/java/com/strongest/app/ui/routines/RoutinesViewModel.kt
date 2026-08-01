package com.strongest.app.ui.routines

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strongest.app.data.model.Equipment
import com.strongest.app.data.model.Exercise
import com.strongest.app.data.model.MuscleGroup
import com.strongest.app.data.model.Routine
import com.strongest.app.data.model.RoutineExercise
import com.strongest.app.data.model.RoutineGroup
import com.strongest.app.data.model.RoutineSet
import com.strongest.app.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class RoutinesViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

    val routines: StateFlow<List<Routine>> = repository.getAllRoutines()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val routineGroups: StateFlow<List<RoutineGroup>> = repository.getAllRoutineGroups()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun createRoutine(name: String, description: String) {
        viewModelScope.launch {
            repository.saveRoutine(name, description, emptyList(), emptyMap())
        }
    }

    fun deleteRoutine(routine: Routine) {
        viewModelScope.launch {
            repository.deleteRoutine(routine)
        }
    }

    fun createGroup(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.createRoutineGroup(trimmed)
        }
    }

    fun renameGroup(groupId: Long, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.renameRoutineGroup(groupId, trimmed)
        }
    }

    fun deleteGroup(groupId: Long) {
        viewModelScope.launch {
            repository.deleteRoutineGroup(groupId)
        }
    }

    fun shareRoutine(routine: Routine, context: Context) {
        viewModelScope.launch {
            val full = repository.getRoutineWithExercisesAndSets(routine.id) ?: return@launch
            val json = exportRoutineToJson(full)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_TEXT, json)
                putExtra(Intent.EXTRA_SUBJECT, "Strongest Routine: ${routine.name}")
            }
            val chooser = Intent.createChooser(shareIntent, "Share Routine")
            context.startActivity(chooser)
        }
    }

    fun importRoutine(jsonString: String) {
        viewModelScope.launch {
            try {
                val routine = importRoutineFromJson(jsonString) ?: return@launch
                repository.saveRoutine(routine.name, routine.description, routine.exercises, routine.sets)
            } catch (_: Exception) {
            }
        }
    }

    private suspend fun exportRoutineToJson(full: com.strongest.app.data.db.RoutineWithExercisesAndSets): String {
        val routine = full.routine
        val exercisesArr = JSONArray()

        for (re in full.exercises) {
            val exercise = repository.getExerciseById(re.exerciseId)
            val exerciseObj = JSONObject().apply {
                put("name", exercise?.name ?: "Unknown")
                put("muscleGroup", exercise?.muscleGroup?.name ?: "OTHER")
                put("equipment", exercise?.equipment?.name ?: "NONE")
                val setsArr = JSONArray()
                val sets = full.sets[re.id] ?: emptyList()
                for (set in sets) {
                    setsArr.put(JSONObject().apply {
                        put("setNumber", set.setNumber)
                        put("weight", set.weight.toDouble())
                        put("reps", set.reps)
                        put("restSeconds", set.restSeconds)
                    })
                }
                put("sets", setsArr)
                put("defaultSets", re.defaultSets)
                put("defaultWeight", re.defaultWeight.toDouble())
                put("defaultReps", re.defaultReps)
                put("restSeconds", re.restSeconds)
            }
            exercisesArr.put(exerciseObj)
        }

        return JSONObject().apply {
            put("version", 1)
            put("name", routine.name)
            put("description", routine.description)
            put("exercises", exercisesArr)
        }.toString(2)
    }

    private suspend fun importRoutineFromJson(jsonString: String): RoutineImportData? {
        val obj = JSONObject(jsonString)
        val version = obj.optInt("version", 1)
        if (version != 1) return null

        val name = obj.getString("name")
        val description = obj.optString("description", "")
        val exercisesArr = obj.getJSONArray("exercises")

        val allExercises = repository.getAllExercisesList()
        val routineExercises = mutableListOf<RoutineExercise>()
        val routineSets = mutableMapOf<Long, List<RoutineSet>>()
        var tempId = -1L

        for (i in 0 until exercisesArr.length()) {
            val exObj = exercisesArr.getJSONObject(i)
            val exName = exObj.getString("name")
            val muscleGroup = try { MuscleGroup.valueOf(exObj.getString("muscleGroup")) } catch (_: Exception) { MuscleGroup.OTHER }
            val equipment = try { Equipment.valueOf(exObj.getString("equipment")) } catch (_: Exception) { Equipment.NONE }

            // Find existing exercise by name, or create custom one
            var exercise = allExercises.find { it.name.equals(exName, ignoreCase = true) }
            if (exercise == null) {
                val customId = -(System.currentTimeMillis() + i)
                exercise = Exercise(
                    id = customId,
                    name = exName,
                    muscleGroup = muscleGroup,
                    equipment = equipment,
                    isCustom = true
                )
                repository.insertCustomExercise(exercise)
            }

            val defaultSets = exObj.optInt("defaultSets", 3)
            val defaultWeight = exObj.optDouble("defaultWeight", 0.0).toFloat()
            val defaultReps = exObj.optInt("defaultReps", 10)
            val restSeconds = exObj.optInt("restSeconds", 90)

            val re = RoutineExercise(
                id = tempId,
                routineId = 0,
                exerciseId = exercise.id,
                orderIndex = i,
                defaultSets = defaultSets,
                defaultWeight = defaultWeight,
                defaultReps = defaultReps,
                restSeconds = restSeconds
            )
            routineExercises.add(re)

            val setsArr = exObj.optJSONArray("sets")
            val sets = mutableListOf<RoutineSet>()
            if (setsArr != null) {
                for (j in 0 until setsArr.length()) {
                    val setObj = setsArr.getJSONObject(j)
                    sets.add(RoutineSet(
                        id = 0,
                        routineExerciseId = tempId,
                        setNumber = setObj.getInt("setNumber"),
                        weight = setObj.getDouble("weight").toFloat(),
                        reps = setObj.getInt("reps"),
                        restSeconds = setObj.optInt("restSeconds", restSeconds)
                    ))
                }
            }
            routineSets[tempId] = sets
            tempId--
        }

        return RoutineImportData(name, description, routineExercises, routineSets)
    }

    private data class RoutineImportData(
        val name: String,
        val description: String,
        val exercises: List<RoutineExercise>,
        val sets: Map<Long, List<RoutineSet>>
    )
}
