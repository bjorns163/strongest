package com.strongest.app.utils

import com.strongest.app.data.db.RoutineWithExercisesAndSets
import com.strongest.app.data.model.Equipment
import com.strongest.app.data.model.Exercise
import com.strongest.app.data.model.MuscleGroup
import com.strongest.app.data.model.SetType
import org.json.JSONArray
import org.json.JSONObject

/**
 * Format version of the single-routine share payload. Bumped only for breaking
 * changes; new optional fields keep the version so older builds can still read
 * the file (they simply ignore what they do not know).
 */
const val ROUTINE_SHARE_VERSION = 1

/** A single planned set of a shared routine, including its [SetType] (warm-up, failure, drop set). */
data class SharedRoutineSet(
    val setNumber: Int,
    val weight: Float = 0f,
    val reps: Int = 10,
    val restSeconds: Int = 90,
    val setType: SetType = SetType.NORMAL
)

/**
 * An exercise inside a shared routine. Exercises travel by name (plus muscle group and
 * equipment), because exercise ids are local to the database that produced the file.
 */
data class SharedRoutineExercise(
    val name: String,
    val muscleGroup: MuscleGroup = MuscleGroup.OTHER,
    val equipment: Equipment = Equipment.NONE,
    val defaultSets: Int = 3,
    val defaultReps: Int = 10,
    val defaultWeight: Float = 0f,
    val restSeconds: Int = 90,
    val sets: List<SharedRoutineSet> = emptyList()
)

data class SharedRoutine(
    val name: String,
    val description: String = "",
    val exercises: List<SharedRoutineExercise> = emptyList(),
    val version: Int = ROUTINE_SHARE_VERSION
)

fun RoutineWithExercisesAndSets.toSharedRoutine(
    exercisesById: Map<Long, Exercise>
): SharedRoutine = SharedRoutine(
    name = routine.name,
    description = routine.description,
    exercises = exercises.map { re ->
        val exercise = exercisesById[re.exerciseId]
        SharedRoutineExercise(
            name = exercise?.name ?: "Unknown",
            muscleGroup = exercise?.muscleGroup ?: MuscleGroup.OTHER,
            equipment = exercise?.equipment ?: Equipment.NONE,
            defaultSets = re.defaultSets,
            defaultReps = re.defaultReps,
            defaultWeight = re.defaultWeight,
            restSeconds = re.restSeconds,
            sets = (sets[re.id] ?: emptyList()).map { set ->
                SharedRoutineSet(
                    setNumber = set.setNumber,
                    weight = set.weight,
                    reps = set.reps,
                    restSeconds = set.restSeconds,
                    setType = set.setType
                )
            }
        )
    }
)

fun SharedRoutine.toJson(): String = JSONObject().apply {
    put("version", version)
    put("name", name)
    put("description", description)
    put("exercises", JSONArray(exercises.map { ex ->
        JSONObject().apply {
            put("name", ex.name)
            put("muscleGroup", ex.muscleGroup.name)
            put("equipment", ex.equipment.name)
            put("sets", JSONArray(ex.sets.map { set ->
                JSONObject().apply {
                    put("setNumber", set.setNumber)
                    put("weight", set.weight.toDouble())
                    put("reps", set.reps)
                    put("restSeconds", set.restSeconds)
                    put("setType", set.setType.name)
                }
            }))
            put("defaultSets", ex.defaultSets)
            put("defaultWeight", ex.defaultWeight.toDouble())
            put("defaultReps", ex.defaultReps)
            put("restSeconds", ex.restSeconds)
        }
    }))
}.toString(2)

/** Parses a shared routine payload, or returns null when it is unreadable or from a newer format. */
fun String.parseSharedRoutine(): SharedRoutine? = try {
    val root = JSONObject(this)
    if (root.optInt("version", ROUTINE_SHARE_VERSION) != ROUTINE_SHARE_VERSION) {
        null
    } else {
        val exercisesArr = root.getJSONArray("exercises")
        SharedRoutine(
            name = root.getString("name"),
            description = root.optString("description", ""),
            exercises = (0 until exercisesArr.length()).map { i ->
                val exObj = exercisesArr.getJSONObject(i)
                val restSeconds = exObj.optInt("restSeconds", 90)
                val setsArr = exObj.optJSONArray("sets")
                SharedRoutineExercise(
                    name = exObj.getString("name"),
                    muscleGroup = enumOrDefault(exObj.optString("muscleGroup"), MuscleGroup.OTHER),
                    equipment = enumOrDefault(exObj.optString("equipment"), Equipment.NONE),
                    defaultSets = exObj.optInt("defaultSets", 3),
                    defaultReps = exObj.optInt("defaultReps", 10),
                    defaultWeight = exObj.optDouble("defaultWeight", 0.0).toFloat(),
                    restSeconds = restSeconds,
                    sets = (0 until (setsArr?.length() ?: 0)).map { j ->
                        val setObj = setsArr!!.getJSONObject(j)
                        SharedRoutineSet(
                            setNumber = setObj.optInt("setNumber", j + 1),
                            weight = setObj.optDouble("weight", 0.0).toFloat(),
                            reps = setObj.optInt("reps", 10),
                            restSeconds = setObj.optInt("restSeconds", restSeconds),
                            setType = enumOrDefault(setObj.optString("setType"), SetType.NORMAL)
                        )
                    }
                )
            },
            version = root.optInt("version", ROUTINE_SHARE_VERSION)
        )
    }
} catch (_: Exception) {
    null
}

private inline fun <reified T : Enum<T>> enumOrDefault(name: String?, default: T): T = try {
    if (name.isNullOrEmpty()) default else enumValueOf(name)
} catch (_: Exception) {
    default
}
