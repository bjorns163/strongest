package com.strongest.app

import com.strongest.app.data.model.SetType
import com.strongest.app.utils.parseExportData
import com.strongest.app.utils.parseSharedRoutine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards the documented samples in docs/samples against format drift: they must stay
 * readable by the parsers that ship in the app.
 */
class SampleFilesTest {

    private fun sample(name: String): String {
        val file = listOf(File("../docs/samples/$name"), File("docs/samples/$name")).first { it.exists() }
        return file.readText()
    }

    @Test
    fun `routine share sample parses and keeps its warm-up sets`() {
        val routine = sample("routine-share-sample.json").parseSharedRoutine()
            ?: throw AssertionError("docs/samples/routine-share-sample.json no longer parses")

        assertEquals("Monday Push/Pull", routine.name)
        assertEquals(2, routine.exercises.size)
        assertEquals(
            listOf(SetType.WARM_UP, SetType.WARM_UP, SetType.NORMAL, SetType.NORMAL),
            routine.exercises[0].sets.map { it.setType }
        )
        assertEquals(SetType.FAILURE, routine.exercises[1].sets.last().setType)
    }

    @Test
    fun `backup sample parses into every documented section`() {
        val data = sample("backup-sample.json").parseExportData()
            ?: throw AssertionError("docs/samples/backup-sample.json no longer parses")

        assertEquals(1, data.version)
        assertEquals("com.strongest.app", data.source)
        assertEquals(2, data.exercises.size)
        assertEquals(1, data.routineGroups.size)
        assertEquals(1, data.routines.size)
        assertEquals(2, data.routineExercises.size)
        assertEquals(7, data.routineSets.size)
        assertEquals(1, data.exerciseNotes.size)
        assertEquals(1, data.exerciseSettings.size)
        assertEquals(1, data.workouts.size)
        assertEquals(1, data.workoutExercises.size)
        assertEquals(2, data.sets.size)
        assertEquals(1, data.measurementEntries.size)

        assertEquals(2, data.exerciseSettings.single().warmUpSetCount)
        assertTrue(data.routineSets.any { it.setType == SetType.WARM_UP })
        assertTrue(data.sets.any { it.setType == SetType.WARM_UP })
    }
}
