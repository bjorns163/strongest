package com.strongest.app

import com.strongest.app.data.db.ExerciseDao
import com.strongest.app.data.db.ExerciseWorkoutVolume
import com.strongest.app.data.db.RoutineDao
import com.strongest.app.data.db.WorkoutDao
import com.strongest.app.data.model.Equipment
import com.strongest.app.data.model.Exercise
import com.strongest.app.data.model.ExerciseType
import com.strongest.app.data.model.MuscleGroup
import com.strongest.app.data.repository.SettingsRepository
import com.strongest.app.data.repository.WorkoutRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Cardio has its own card on the Progress tab, so it must not show up in the muscle charts —
 * not even through a strength exercise that lists CARDIO as a secondary muscle.
 */
class MuscleVolumeCardioTest {

    @Test
    fun `a CARDIO secondary muscle is not credited in muscle volume`() = runTest {
        val exerciseDao = mock(ExerciseDao::class.java)
        val workoutDao = mock(WorkoutDao::class.java)
        `when`(exerciseDao.getAllExercisesList()).thenReturn(
            listOf(
                Exercise(
                    id = 1L,
                    name = "Burpee",
                    muscleGroup = MuscleGroup.FULL_BODY,
                    equipment = Equipment.BODYWEIGHT,
                    type = ExerciseType.COMPOUND,
                    secondaryMuscles = listOf(MuscleGroup.CARDIO)
                )
            )
        )
        `when`(workoutDao.getExerciseWorkoutVolume(anyLong(), anyLong())).thenReturn(
            listOf(ExerciseWorkoutVolume(exerciseId = 1L, workoutId = 1L, sets = 3, volumeKg = 0f))
        )
        val repository = WorkoutRepository(
            exerciseDao,
            mock(RoutineDao::class.java),
            workoutDao,
            mock(SettingsRepository::class.java)
        )

        val muscles = repository.getMuscleVolume(0L, Long.MAX_VALUE)

        assertNull(muscles.firstOrNull { it.muscleGroup == MuscleGroup.CARDIO.name })
        assertEquals(3f, muscles.single { it.muscleGroup == MuscleGroup.FULL_BODY.name }.totalSets, 0.01f)
    }
}
