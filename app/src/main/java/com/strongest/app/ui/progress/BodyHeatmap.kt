package com.strongest.app.ui.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.strongest.app.data.model.MuscleGroup
import kotlin.math.sqrt

/**
 * Heat scale, cold to hot. The untrained end is theme-aware — it is the body's own
 * colour, so an untrained muscle reads as "part of the body" rather than as data.
 */
private val HEAT_STOPS = listOf(
    Color(0xFFFDC9B7),
    Color(0xFFF78E74),
    Color(0xFFE24937),
    Color(0xFF7A0C0E)
)

private fun heatColor(t: Float, cold: Color): Color {
    if (t <= 0f) return cold
    val stops = listOf(cold) + HEAT_STOPS
    val pos = (t.coerceIn(0f, 1f)) * (stops.size - 1)
    val i = pos.toInt().coerceAtMost(stops.size - 2)
    return lerp(stops[i], stops[i + 1], pos - i)
}

@Composable
fun BodyHeatmap(
    muscleValues: Map<MuscleGroup, Float>,
    modifier: Modifier = Modifier,
    figure: BodyFigure = BodyFigure.MALE
) {
    val bodyColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val outlineColor = MaterialTheme.colorScheme.outline
    val titleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val maxValue = muscleValues.values.maxOrNull()?.coerceAtLeast(1f) ?: 1f

    fun colorFor(group: MuscleGroup): Color {
        val v = muscleValues[group] ?: 0f
        if (v <= 0f) return bodyColor
        // Square-root ramp so a lightly trained muscle still reads as trained rather
        // than being washed out by whatever was hit hardest.
        return heatColor(sqrt((v / maxValue).coerceIn(0f, 1f)), bodyColor)
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (view in listOf(BodyView.FRONT, BodyView.BACK)) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(360.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (view == BodyView.FRONT) "Front" else "Back",
                        style = MaterialTheme.typography.labelMedium,
                        color = titleColor
                    )
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawAnatomy(figure, view, bodyColor, outlineColor, ::colorFor)
                    }
                }
            }
        }

        HeatmapLegend(
            coldColor = bodyColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

private fun DrawScope.drawAnatomy(
    figure: BodyFigure,
    view: BodyView,
    bodyColor: Color,
    outlineColor: Color,
    colorFor: (MuscleGroup) -> Color
) {
    // The drawing is 200x380; fit it to the canvas without distorting the figure.
    val scale = minOf(size.width / 200f, size.height / 380f)
    val drawW = 200f * scale
    val drawH = 380f * scale
    val dx = (size.width - drawW) / 2f
    val dy = (size.height - drawH) / 2f

    translate(dx, dy) {
        val anatomy = buildAnatomy(figure, view, drawW, drawH)
        val stroke = Stroke(width = (scale * 0.9f).coerceIn(1f, 2.5f))

        drawPath(anatomy.body, color = bodyColor)
        clipPath(anatomy.body) {
            for ((group, path) in anatomy.muscles) {
                drawPath(path, color = colorFor(group))
                drawPath(path, color = outlineColor, style = stroke)
            }
        }
        drawPath(anatomy.body, color = outlineColor, style = Stroke(width = stroke.width * 1.4f))
    }
}

@Composable
private fun HeatmapLegend(coldColor: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Untrained",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val brush = remember(coldColor) {
            Brush.horizontalGradient(listOf(coldColor) + HEAT_STOPS)
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .background(brush = brush, shape = RoundedCornerShape(5.dp))
        )
        Text(
            text = "Most volume",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
