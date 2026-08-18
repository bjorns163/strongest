package com.strongest.app

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@Ignore("Flaky: compose test frame clock races with StateFlow VM emissions (snapshot conflict), crashes at random steps")
class WarmUpAddFlowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() {
            ApplicationProvider.getApplicationContext<Context>().deleteDatabase("strongest.db")
            // Notification dialog can't be clicked via compose; grant pre-empts it.
            InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
                ApplicationProvider.getApplicationContext<Context>().packageName,
                "android.permission.POST_NOTIFICATIONS"
            )
        }
    }

    @Test
    fun warmUpSliderAddsSetsToActiveWorkout() {
        composeRule.onNodeWithText("Start Empty Workout").performClick()

        composeRule.onNodeWithText("Add Exercise").performClick()

        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput("Bench")
        composeRule.onNodeWithText("Barbell Bench Press").performClick()

        // Workout name field + 3 sets x 3 fields each -> >= 10 means sets are loaded.
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().size >= 10
        }

        composeRule.onNodeWithText("Barbell Bench Press").performClick()

        composeRule.onNodeWithText("Warm-up").performClick()

        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput("100")
        composeRule.onAllNodes(hasSetTextAction())[1].performTextInput("10")

        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("50 kg × 8").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("70 kg × 5").assertExists()
        composeRule.onNodeWithText("85 kg × 3").assertExists()

        val slider = composeRule.onNode(
            SemanticsMatcher("is a Slider") {
                it.config.contains(SemanticsProperties.ProgressBarRangeInfo)
            }
        )
        slider.performScrollTo().performTouchInput { click(Offset(width * 0.45f, 0f)) }

        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("95 kg × 2").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("Add warm-up sets to workout")
            .performScrollTo()
            .performClick()

        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithContentDescription("Warm-up set")
                .fetchSemanticsNodes().size == 4
        }
        composeRule.onAllNodesWithContentDescription("Warm-up set").assertCountEquals(4)
        composeRule.onNodeWithText("50").assertExists()
        composeRule.onNodeWithText("70").assertExists()
        composeRule.onNodeWithText("85").assertExists()
        composeRule.onNodeWithText("95").assertExists()
    }
}
