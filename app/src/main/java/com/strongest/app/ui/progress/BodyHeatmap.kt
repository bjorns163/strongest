package com.strongest.app.ui.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.strongest.app.data.model.MuscleGroup

private enum class Region {
    HEAD, NECK,
    CHEST, ABS,
    SHOULDER_L, SHOULDER_R,
    BICEP_L, BICEP_R,
    FOREARM_L, FOREARM_R,
    QUAD_L, QUAD_R,
    CALF_L, CALF_R,
    UPPER_BACK, LOWER_BACK, TRAPS,
    TRICEP_L, TRICEP_R,
    GLUTE_L, GLUTE_R,
    HAMSTRING_L, HAMSTRING_R
}

@Composable
fun BodyHeatmap(
    muscleValues: Map<MuscleGroup, Float>,
    modifier: Modifier = Modifier
) {
    val baseColor = MaterialTheme.colorScheme.primary
    val emptyColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val maxValue = muscleValues.values.maxOrNull()?.coerceAtLeast(1f) ?: 1f

    fun colorFor(group: MuscleGroup): Color {
        val v = muscleValues[group] ?: 0f
        if (v <= 0f) return emptyColor
        val intensity = (0.25f + 0.75f * (v / maxValue)).coerceIn(0.25f, 1f)
        return baseColor.copy(alpha = intensity)
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BodyView(
                title = "Front",
                draw = { drawFront(colorFor = ::colorFor, outline = outlineColor) },
                titleColor = onSurface,
                modifier = Modifier
                    .weight(1f)
                    .height(360.dp)
            )
            BodyView(
                title = "Back",
                draw = { drawBack(colorFor = ::colorFor, outline = outlineColor) },
                titleColor = onSurface,
                modifier = Modifier
                    .weight(1f)
                    .height(360.dp)
            )
        }

        HeatmapLegend(
            baseColor = baseColor,
            emptyColor = emptyColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun BodyView(
    title: String,
    draw: DrawScope.() -> Unit,
    titleColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = titleColor
        )
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) { draw() }
        }
    }
}

@Composable
private fun HeatmapLegend(
    baseColor: Color,
    emptyColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Less",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
            val steps = 5
            for (i in 0 until steps) {
                val alpha = if (i == 0) 0f else 0.25f + 0.75f * (i.toFloat() / (steps - 1))
                val c = if (alpha == 0f) emptyColor else baseColor.copy(alpha = alpha)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .padding(horizontal = 1.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(color = c, size = size)
                    }
                }
            }
        }
        Text(
            text = "More",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun DrawScope.drawFront(
    colorFor: (MuscleGroup) -> Color,
    outline: Color
) {
    val w = size.width
    val h = size.height
    val cx = w / 2f

    // Head
    drawOval(
        color = outline,
        topLeft = Offset(cx - w * 0.10f, h * 0.02f),
        size = Size(w * 0.20f, h * 0.13f),
        style = Stroke(width = 2f)
    )

    // Neck
    drawRectFilled(
        outline,
        Offset(cx - w * 0.05f, h * 0.13f),
        Size(w * 0.10f, h * 0.04f),
        outline
    )

    // Shoulders
    drawRegion(colorFor(MuscleGroup.SHOULDERS), outline,
        Offset(cx - w * 0.28f, h * 0.17f), Size(w * 0.15f, h * 0.08f))
    drawRegion(colorFor(MuscleGroup.SHOULDERS), outline,
        Offset(cx + w * 0.13f, h * 0.17f), Size(w * 0.15f, h * 0.08f))

    // Chest (left + right)
    drawRegion(colorFor(MuscleGroup.CHEST), outline,
        Offset(cx - w * 0.18f, h * 0.18f), Size(w * 0.16f, h * 0.13f))
    drawRegion(colorFor(MuscleGroup.CHEST), outline,
        Offset(cx + w * 0.02f, h * 0.18f), Size(w * 0.16f, h * 0.13f))

    // Abs
    drawRegion(colorFor(MuscleGroup.ABS), outline,
        Offset(cx - w * 0.10f, h * 0.32f), Size(w * 0.20f, h * 0.16f))

    // Biceps
    drawRegion(colorFor(MuscleGroup.BICEPS), outline,
        Offset(cx - w * 0.34f, h * 0.25f), Size(w * 0.10f, h * 0.14f))
    drawRegion(colorFor(MuscleGroup.BICEPS), outline,
        Offset(cx + w * 0.24f, h * 0.25f), Size(w * 0.10f, h * 0.14f))

    // Forearms
    drawRegion(colorFor(MuscleGroup.FOREARMS), outline,
        Offset(cx - w * 0.36f, h * 0.40f), Size(w * 0.10f, h * 0.14f))
    drawRegion(colorFor(MuscleGroup.FOREARMS), outline,
        Offset(cx + w * 0.26f, h * 0.40f), Size(w * 0.10f, h * 0.14f))

    // Quads
    drawRegion(colorFor(MuscleGroup.QUADS), outline,
        Offset(cx - w * 0.18f, h * 0.55f), Size(w * 0.15f, h * 0.20f))
    drawRegion(colorFor(MuscleGroup.QUADS), outline,
        Offset(cx + w * 0.03f, h * 0.55f), Size(w * 0.15f, h * 0.20f))

    // Calves
    drawRegion(colorFor(MuscleGroup.CALVES), outline,
        Offset(cx - w * 0.16f, h * 0.78f), Size(w * 0.13f, h * 0.18f))
    drawRegion(colorFor(MuscleGroup.CALVES), outline,
        Offset(cx + w * 0.03f, h * 0.78f), Size(w * 0.13f, h * 0.18f))
}

private fun DrawScope.drawBack(
    colorFor: (MuscleGroup) -> Color,
    outline: Color
) {
    val w = size.width
    val h = size.height
    val cx = w / 2f

    // Head
    drawOval(
        color = outline,
        topLeft = Offset(cx - w * 0.10f, h * 0.02f),
        size = Size(w * 0.20f, h * 0.13f),
        style = Stroke(width = 2f)
    )

    // Neck
    drawRectFilled(
        outline,
        Offset(cx - w * 0.05f, h * 0.13f),
        Size(w * 0.10f, h * 0.04f),
        outline
    )

    // Traps
    drawRegion(colorFor(MuscleGroup.TRAPS), outline,
        Offset(cx - w * 0.10f, h * 0.16f), Size(w * 0.20f, h * 0.06f))

    // Rear delts (use SHOULDERS)
    drawRegion(colorFor(MuscleGroup.SHOULDERS), outline,
        Offset(cx - w * 0.28f, h * 0.17f), Size(w * 0.15f, h * 0.08f))
    drawRegion(colorFor(MuscleGroup.SHOULDERS), outline,
        Offset(cx + w * 0.13f, h * 0.17f), Size(w * 0.15f, h * 0.08f))

    // Upper back (BACK)
    drawRegion(colorFor(MuscleGroup.BACK), outline,
        Offset(cx - w * 0.18f, h * 0.22f), Size(w * 0.16f, h * 0.16f))
    drawRegion(colorFor(MuscleGroup.BACK), outline,
        Offset(cx + w * 0.02f, h * 0.22f), Size(w * 0.16f, h * 0.16f))

    // Lower back
    drawRegion(colorFor(MuscleGroup.LOWER_BACK), outline,
        Offset(cx - w * 0.10f, h * 0.40f), Size(w * 0.20f, h * 0.08f))

    // Triceps
    drawRegion(colorFor(MuscleGroup.TRICEPS), outline,
        Offset(cx - w * 0.34f, h * 0.25f), Size(w * 0.10f, h * 0.14f))
    drawRegion(colorFor(MuscleGroup.TRICEPS), outline,
        Offset(cx + w * 0.24f, h * 0.25f), Size(w * 0.10f, h * 0.14f))

    // Forearms
    drawRegion(colorFor(MuscleGroup.FOREARMS), outline,
        Offset(cx - w * 0.36f, h * 0.40f), Size(w * 0.10f, h * 0.14f))
    drawRegion(colorFor(MuscleGroup.FOREARMS), outline,
        Offset(cx + w * 0.26f, h * 0.40f), Size(w * 0.10f, h * 0.14f))

    // Glutes
    drawRegion(colorFor(MuscleGroup.GLUTES), outline,
        Offset(cx - w * 0.18f, h * 0.49f), Size(w * 0.15f, h * 0.10f))
    drawRegion(colorFor(MuscleGroup.GLUTES), outline,
        Offset(cx + w * 0.03f, h * 0.49f), Size(w * 0.15f, h * 0.10f))

    // Hamstrings
    drawRegion(colorFor(MuscleGroup.HAMSTRINGS), outline,
        Offset(cx - w * 0.18f, h * 0.60f), Size(w * 0.15f, h * 0.17f))
    drawRegion(colorFor(MuscleGroup.HAMSTRINGS), outline,
        Offset(cx + w * 0.03f, h * 0.60f), Size(w * 0.15f, h * 0.17f))

    // Calves
    drawRegion(colorFor(MuscleGroup.CALVES), outline,
        Offset(cx - w * 0.16f, h * 0.78f), Size(w * 0.13f, h * 0.18f))
    drawRegion(colorFor(MuscleGroup.CALVES), outline,
        Offset(cx + w * 0.03f, h * 0.78f), Size(w * 0.13f, h * 0.18f))
}

private fun DrawScope.drawRegion(
    fill: Color,
    outline: Color,
    topLeft: Offset,
    size: Size,
    cornerRadius: Float = 12f
) {
    val path = Path().apply {
        addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                left = topLeft.x,
                top = topLeft.y,
                right = topLeft.x + size.width,
                bottom = topLeft.y + size.height,
                radiusX = cornerRadius,
                radiusY = cornerRadius
            )
        )
    }
    drawPath(path = path, color = fill)
    drawPath(path = path, color = outline, style = Stroke(width = 2f))
}

private fun DrawScope.drawRectFilled(
    fill: Color,
    topLeft: Offset,
    size: Size,
    outline: Color
) {
    drawRect(color = fill, topLeft = topLeft, size = size)
    drawRect(color = outline, topLeft = topLeft, size = size, style = Stroke(width = 2f))
}
