package com.strongest.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.strongest.app.R

/**
 * How far the top bar's actions shrink to fit: Cancel turns into an icon first, then the confirm
 * button. Only when even that doesn't fit is the title shortened with an ellipsis.
 */
enum class TopBarCompaction(val compactCancel: Boolean, val compactConfirm: Boolean) {
    NONE(false, false),
    CANCEL(true, false),
    BOTH(true, true)
}

/**
 * Back button, title and trailing actions on a single line.
 *
 * A plain Row measured the title first and squeezed the last button into whatever width was left,
 * so with a large font size or on a narrow phone "Finish" wrapped one letter per line. Here the
 * actions are measured at each [TopBarCompaction] and the first one that fits next to the full
 * title is used, so a button label never wraps.
 */
@Composable
fun AdaptiveTopBar(
    title: String,
    navigationIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.(TopBarCompaction) -> Unit
) {
    val titleStyle = MaterialTheme.typography.titleLarge
    SubcomposeLayout(modifier.fillMaxWidth()) { constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val unbounded = Constraints()
        // No extra gaps: the back button and the text/icon buttons carry their own padding, and
        // the default-size bar on a typical phone fits with only a few pixels to spare.
        val nav = subcompose("nav", navigationIcon).map { it.measure(loose) }
        val navWidth = nav.maxOfOrNull { it.width } ?: 0

        // Measure-only copies: never placed, and hidden from accessibility so nothing is announced twice.
        val fullTitleWidth = subcompose("measure-title") {
            Text(title, style = titleStyle, maxLines = 1, softWrap = false, modifier = Modifier.clearAndSetSemantics {})
        }.maxOfOrNull { it.measure(unbounded).width } ?: 0
        fun actionsWidth(compaction: TopBarCompaction): Int =
            subcompose("measure-actions-$compaction") {
                Row(Modifier.clearAndSetSemantics {}, verticalAlignment = Alignment.CenterVertically) {
                    actions(compaction)
                }
            }.maxOfOrNull { it.measure(unbounded).width } ?: 0

        val compaction = if (!constraints.hasBoundedWidth) {
            TopBarCompaction.NONE
        } else {
            val available = constraints.maxWidth - navWidth - fullTitleWidth
            TopBarCompaction.entries.firstOrNull { actionsWidth(it) <= available } ?: TopBarCompaction.BOTH
        }

        val actionPlaceables = subcompose("actions") {
            Row(verticalAlignment = Alignment.CenterVertically) { actions(compaction) }
        }.map { it.measure(loose) }
        val actionsWidth = actionPlaceables.maxOfOrNull { it.width } ?: 0

        val width = if (constraints.hasBoundedWidth) constraints.maxWidth else navWidth + fullTitleWidth + actionsWidth
        val titleMaxWidth = (width - navWidth - actionsWidth).coerceAtLeast(0)
        val titlePlaceables = subcompose("title") {
            Text(title, style = titleStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }.map { it.measure(loose.copy(maxWidth = titleMaxWidth)) }

        val height = (nav + titlePlaceables + actionPlaceables).maxOfOrNull { it.height } ?: 0
        layout(width, height) {
            nav.forEach { it.placeRelative(0, (height - it.height) / 2) }
            titlePlaceables.forEach { it.placeRelative(navWidth, (height - it.height) / 2) }
            actionPlaceables.forEach { it.placeRelative(width - it.width, (height - it.height) / 2) }
        }
    }
}

/** Red "Cancel" text, or a red ✕ when compact. */
@Composable
fun TopBarCancelAction(
    compact: Boolean,
    onClick: () -> Unit,
    contentDescription: String = stringResource(R.string.cancel)
) {
    if (compact) {
        IconButton(onClick = onClick) {
            Icon(Icons.Default.Close, contentDescription, tint = MaterialTheme.colorScheme.error)
        }
    } else {
        TextButton(onClick = onClick) {
            Text(
                stringResource(R.string.cancel),
                color = MaterialTheme.colorScheme.error,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

/** Filled button with icon and label, or just the icon when compact (the label becomes its description). */
@Composable
fun TopBarConfirmAction(
    compact: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    if (compact) {
        FilledIconButton(onClick = onClick, enabled = enabled) {
            Icon(icon, label)
        }
    } else {
        Button(onClick = onClick, enabled = enabled) {
            Icon(icon, null, Modifier.padding(end = 4.dp))
            Text(label, maxLines = 1, softWrap = false)
        }
    }
}
