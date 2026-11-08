package com.strongest.app

import android.content.Context
import android.os.SystemClock
import androidx.test.core.app.ApplicationProvider
import com.strongest.app.data.repository.SILENT_SOUND
import com.strongest.app.utils.RestTimer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The rest timer lives outside the workout screen so leaving that screen (back, or swiping the
 * app away) no longer cancels the rest and its alert. These run it on virtual time.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RestTimerTest {

    private lateinit var dispatcher: TestDispatcher
    private lateinit var context: Context

    @Before
    fun setUp() {
        dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        RestTimer.now = { dispatcher.scheduler.currentTime }
        context = ApplicationProvider.getApplicationContext()
    }

    @After
    fun tearDown() {
        RestTimer.cancel()
        RestTimer.now = SystemClock::elapsedRealtime
        Dispatchers.resetMain()
    }

    @Test
    fun `counts down and clears itself when the rest runs out`() = runTest(dispatcher) {
        RestTimer.start(context, workoutId = 1L, setId = 7L, seconds = 3, soundUri = SILENT_SOUND)
        assertEquals(3, RestTimer.state.value!!.remainingSeconds)
        assertEquals(7L, RestTimer.state.value!!.setId)

        advanceTimeBy(1_000); runCurrent()
        assertEquals(2, RestTimer.state.value!!.remainingSeconds)

        advanceTimeBy(2_000); runCurrent()
        assertNull(RestTimer.state.value)
    }

    @Test
    fun `adjusting moves the end and grows the total`() = runTest(dispatcher) {
        RestTimer.start(context, workoutId = 1L, setId = null, seconds = 10, soundUri = SILENT_SOUND)
        RestTimer.adjust(context, 30)
        assertEquals(40, RestTimer.state.value!!.remainingSeconds)
        assertEquals(40, RestTimer.state.value!!.totalSeconds)

        RestTimer.adjust(context, -30)
        assertEquals(10, RestTimer.state.value!!.remainingSeconds)
        assertEquals(40, RestTimer.state.value!!.totalSeconds)

        advanceTimeBy(10_000); runCurrent()
        assertNull(RestTimer.state.value)
    }

    @Test
    fun `cancelFor only stops the rest of that workout`() = runTest(dispatcher) {
        RestTimer.start(context, workoutId = 1L, setId = null, seconds = 60, soundUri = SILENT_SOUND)

        RestTimer.cancelFor(2L)
        assertNotNull(RestTimer.state.value)
        RestTimer.cancelFor(null)
        assertNotNull(RestTimer.state.value)

        RestTimer.cancelFor(1L)
        assertNull(RestTimer.state.value)
    }

    @Test
    fun `a new rest replaces the running one`() = runTest(dispatcher) {
        RestTimer.start(context, workoutId = 1L, setId = 1L, seconds = 60, soundUri = SILENT_SOUND)
        advanceTimeBy(30_000); runCurrent()
        RestTimer.start(context, workoutId = 1L, setId = 2L, seconds = 90, soundUri = SILENT_SOUND)

        assertEquals(2L, RestTimer.state.value!!.setId)
        advanceTimeBy(60_000); runCurrent()
        assertEquals(30, RestTimer.state.value!!.remainingSeconds)
    }
}
