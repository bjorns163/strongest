package com.strongest.app.ui.whatsnew

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import com.strongest.app.R
import com.strongest.app.data.repository.WHATS_NEW_VERSION
import com.strongest.app.ui.workout.openUrl
import com.strongest.app.ui.workout.progressionFeedbackUrl
import com.strongest.app.utils.SettingsEntryPoint
import com.strongest.app.utils.WorkoutRepositoryEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** The Play Store listing; the release package, so debug builds point at the real app too. */
private const val PLAY_STORE_PACKAGE = "com.strongest.app"

private fun openPlayStore(context: Context) {
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, "market://details?id=$PLAY_STORE_PACKAGE".toUri())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (_: ActivityNotFoundException) {
        openUrl(context, "https://play.google.com/store/apps/details?id=$PLAY_STORE_PACKAGE")
    }
}

/**
 * Shows the "what's new" intro once, to people updating who already have workouts logged.
 * A fresh install has nothing to base suggestions on, so it is marked as seen silently.
 * Closing the intro in any way counts as seen.
 */
@Composable
fun WhatsNewHost() {
    val context = LocalContext.current
    val settingsRepository = remember {
        EntryPointAccessors.fromApplication(context, SettingsEntryPoint::class.java).settingsRepository()
    }
    val workoutRepository = remember {
        EntryPointAccessors.fromApplication(context, WorkoutRepositoryEntryPoint::class.java)
            .workoutRepository()
    }
    val scope = rememberCoroutineScope()
    var show by remember { mutableStateOf(false) }
    var initialEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (settingsRepository.whatsNewSeenVersionFlow.first() >= WHATS_NEW_VERSION) return@LaunchedEffect
        if (workoutRepository.countCompletedWorkouts() == 0) {
            settingsRepository.setWhatsNewSeenVersion(WHATS_NEW_VERSION)
        } else {
            initialEnabled = settingsRepository.settingsFlow.first().progression.enabled
            show = true
        }
    }

    if (show) {
        WhatsNewDialog(
            initialEnabled = initialEnabled,
            onFinish = { enabled ->
                show = false
                scope.launch {
                    settingsRepository.updateProgression { it.copy(enabled = enabled) }
                    settingsRepository.setWhatsNewSeenVersion(WHATS_NEW_VERSION)
                }
            }
        )
    }
}

@Composable
fun WhatsNewDialog(
    initialEnabled: Boolean,
    onFinish: (progressionEnabled: Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 })
    var enabled by remember { mutableStateOf(initialEnabled) }

    Dialog(onDismissRequest = { onFinish(enabled) }) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(modifier = Modifier.padding(vertical = 24.dp)) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 320.dp)
                ) { page ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (page == 0) {
                            SlideIcon { Icon(Icons.AutoMirrored.Filled.TrendingUp, null) }
                            Text(
                                stringResource(R.string.whats_new_progression_title),
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                stringResource(R.string.whats_new_progression_body),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .toggleable(
                                        value = enabled,
                                        onValueChange = { enabled = it },
                                        role = Role.Switch
                                    )
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    stringResource(R.string.whats_new_turn_on),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Switch(checked = enabled, onCheckedChange = null)
                            }
                        } else {
                            SlideIcon { Icon(Icons.Default.Star, null) }
                            Text(
                                stringResource(R.string.whats_new_support_title),
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                stringResource(R.string.whats_new_support_body),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            FilledTonalButton(
                                onClick = { openPlayStore(context) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Star, null, Modifier.size(18.dp))
                                Spacer(Modifier.size(8.dp))
                                Text(stringResource(R.string.whats_new_rate))
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                stringResource(R.string.whats_new_feedback_body),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            OutlinedButton(
                                onClick = { openUrl(context, progressionFeedbackUrl()) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Feedback, null, Modifier.size(18.dp))
                                Spacer(Modifier.size(8.dp))
                                Text(stringResource(R.string.whats_new_feedback))
                            }
                        }
                    }
                }

                PageDots(
                    current = pagerState.currentPage,
                    count = pagerState.pageCount,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 16.dp, bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (pagerState.currentPage == 0) {
                        Button(onClick = { scope.launch { pagerState.animateScrollToPage(1) } }) {
                            Text(stringResource(R.string.next))
                        }
                    } else {
                        TextButton(onClick = { scope.launch { pagerState.animateScrollToPage(0) } }) {
                            Text(stringResource(R.string.back))
                        }
                        Spacer(Modifier.size(8.dp))
                        Button(onClick = { onFinish(enabled) }) {
                            Text(stringResource(R.string.done))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SlideIcon(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides MaterialTheme.colorScheme.onPrimaryContainer,
            content = content
        )
    }
}

@Composable
private fun PageDots(current: Int, count: Int, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.whats_new_page, current + 1, count)
    Row(
        modifier = modifier.semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(count) { i ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        if (i == current) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                        CircleShape
                    )
            )
        }
    }
}
