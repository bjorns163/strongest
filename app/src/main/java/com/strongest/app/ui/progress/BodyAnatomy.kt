package com.strongest.app.ui.progress

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import com.strongest.app.data.model.MuscleGroup
import com.strongest.app.data.repository.Sex
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/** Which figure the heatmap draws. An unset profile gets the male figure. */
enum class BodyFigure { MALE, FEMALE }

fun Sex.toBodyFigure(): BodyFigure = when (this) {
    Sex.FEMALE -> BodyFigure.FEMALE
    else -> BodyFigure.MALE
}

enum class BodyView { FRONT, BACK }

/** Body outline plus the muscle regions drawn on top of it, in view-box space. */
class Anatomy(
    val body: Path,
    val muscles: List<Pair<MuscleGroup, Path>>
)

private const val VB_W = 200f
private const val VB_H = 380f
private const val CX = VB_W / 2f

/**
 * Landmark half-widths and vertical anchors. Every shape is derived from these, so the
 * male and female figures share one construction and only the numbers differ.
 */
private data class Figure(
    val headW: Float, val headTop: Float, val chin: Float, val jawW: Float,
    val neckW: Float, val neckBot: Float,
    val shoulder: Float, val shoulderY: Float,
    val chest: Float, val chestY: Float,
    val waist: Float, val waistY: Float,
    val hip: Float, val hipY: Float, val crotchY: Float,
    val thigh: Float, val knee: Float, val kneeY: Float,
    val calf: Float, val ankle: Float, val ankleY: Float,
    val uarm: Float, val elbow: Float, val elbowY: Float,
    val farm: Float, val wrist: Float, val wristY: Float, val handY: Float
)

private val MALE_FIGURE = Figure(
    headW = 13.5f, headTop = 10f, chin = 44f, jawW = 11f,
    neckW = 9.5f, neckBot = 56f,
    shoulder = 48f, shoulderY = 74f,
    chest = 40f, chestY = 100f,
    waist = 29f, waistY = 152f,
    hip = 36f, hipY = 192f, crotchY = 212f,
    thigh = 20f, knee = 12.5f, kneeY = 272f,
    calf = 13f, ankle = 6.5f, ankleY = 344f,
    uarm = 12.5f, elbow = 9.5f, elbowY = 168f,
    farm = 10.5f, wrist = 6.5f, wristY = 232f, handY = 258f
)

private val FEMALE_FIGURE = Figure(
    headW = 12.5f, headTop = 10f, chin = 43f, jawW = 10f,
    neckW = 8f, neckBot = 56f,
    shoulder = 41f, shoulderY = 74f,
    chest = 33f, chestY = 100f,
    waist = 24f, waistY = 150f,
    hip = 39f, hipY = 194f, crotchY = 212f,
    thigh = 20.5f, knee = 11.5f, kneeY = 272f,
    calf = 12f, ankle = 6f, ankleY = 344f,
    uarm = 10.5f, elbow = 8f, elbowY = 168f,
    farm = 9f, wrist = 5.5f, wristY = 232f, handY = 258f
)

private class Joints(val side: Float, f: Figure) {
    val shoulder = Offset(CX + side * f.shoulder * 0.84f, f.shoulderY + 4f)
    val elbow = Offset(CX + side * (f.shoulder + 6f), f.elbowY)
    val wrist = Offset(CX + side * (f.shoulder + 11f), f.wristY)
    val hip = Offset(CX + side * f.hip * 0.50f, f.hipY + 4f)
    val knee = Offset(CX + side * f.hip * 0.46f, f.kneeY)
    val ankle = Offset(CX + side * f.hip * 0.38f, f.ankleY)
}

/** Closed Catmull-Rom through [pts] — a handful of control points become a smooth outline. */
private fun smooth(pts: List<Offset>): Path {
    val n = pts.size
    return Path().apply {
        moveTo(pts[0].x, pts[0].y)
        for (i in 0 until n) {
            val p0 = pts[(i - 1 + n) % n]
            val p1 = pts[i % n]
            val p2 = pts[(i + 1) % n]
            val p3 = pts[(i + 2) % n]
            cubicTo(
                p1.x + (p2.x - p0.x) / 6f, p1.y + (p2.y - p0.y) / 6f,
                p2.x - (p3.x - p1.x) / 6f, p2.y - (p3.y - p1.y) / 6f,
                p2.x, p2.y
            )
        }
        close()
    }
}

/** Tapered capsule between two points — limbs and long muscles. */
private fun capsule(a: Offset, ra: Float, b: Offset, rb: Float): Path {
    val dx = b.x - a.x
    val dy = b.y - a.y
    val len = hypot(dx, dy).coerceAtLeast(0.0001f)
    val ux = dx / len
    val uy = dy / len
    val nx = -uy
    val ny = ux
    val pts = mutableListOf<Offset>()
    for (k in 0..6) {
        val t = Math.PI.toFloat() * k / 6f
        pts += Offset(b.x + nx * rb * cos(t) + ux * rb * sin(t), b.y + ny * rb * cos(t) + uy * rb * sin(t))
    }
    for (k in 0..6) {
        val t = Math.PI.toFloat() * k / 6f
        pts += Offset(a.x - nx * ra * cos(t) - ux * ra * sin(t), a.y - ny * ra * cos(t) - uy * ra * sin(t))
    }
    return Path().apply {
        moveTo(pts[0].x, pts[0].y)
        for (p in pts.drop(1)) lineTo(p.x, p.y)
        close()
    }
}

private fun Path.mirroredX(): Path {
    val copy = Path().apply { addPath(this@mirroredX) }
    copy.transform(Matrix().apply {
        translate(VB_W, 0f)
        scale(-1f, 1f)
    })
    return copy
}

private fun union(parts: List<Path>): Path =
    parts.reduce { acc, part -> Path().apply { op(acc, part, PathOperation.Union) } }

/** Head, neck, torso, arms, hands, legs and feet — unioned into one silhouette. */
private fun bodyPath(f: Figure): Path {
    val parts = mutableListOf<Path>()

    parts += smooth(listOf(
        Offset(CX, f.headTop),
        Offset(CX - f.headW, f.headTop + 8f),
        Offset(CX - f.headW * 0.94f, f.chin - 10f),
        Offset(CX - f.jawW * 0.60f, f.chin),
        Offset(CX, f.chin + 3f),
        Offset(CX + f.jawW * 0.60f, f.chin),
        Offset(CX + f.headW * 0.94f, f.chin - 10f),
        Offset(CX + f.headW, f.headTop + 8f)
    ))
    parts += Path().apply {
        moveTo(CX - f.neckW, f.chin - 8f)
        lineTo(CX - f.neckW, f.neckBot + 4f)
        lineTo(CX + f.neckW, f.neckBot + 4f)
        lineTo(CX + f.neckW, f.chin - 8f)
        close()
    }

    val left = listOf(
        Offset(CX - f.neckW - 1f, f.neckBot - 8f),
        Offset(CX - f.shoulder * 0.66f, f.neckBot + 2f),
        Offset(CX - f.shoulder, f.shoulderY + 6f),
        Offset(CX - f.chest, f.chestY),
        Offset(CX - f.chest * 0.90f, f.chestY + 26f),
        Offset(CX - f.waist, f.waistY),
        Offset(CX - f.hip * 0.92f, f.hipY - 18f),
        Offset(CX - f.hip, f.hipY + 8f),
        Offset(CX - f.hip * 0.80f, f.crotchY)
    )
    parts += smooth(left + Offset(CX, f.crotchY + 6f) + left.reversed().map { Offset(VB_W - it.x, it.y) })

    for (side in listOf(-1f, 1f)) {
        val j = Joints(side, f)
        parts += capsule(Offset(j.shoulder.x, j.shoulder.y - 2f), f.uarm, j.elbow, f.elbow)
        parts += capsule(j.elbow, f.elbow, j.wrist, f.wrist)
        parts += smooth(listOf(
            j.wrist,
            Offset(j.wrist.x - side * f.wrist * 1.3f, j.wrist.y + 12f),
            Offset(j.wrist.x - side * f.wrist * 0.8f, f.handY),
            Offset(j.wrist.x + side * f.wrist * 0.9f, f.handY - 4f),
            Offset(j.wrist.x + side * f.wrist * 1.2f, j.wrist.y + 10f)
        ))
        parts += capsule(j.hip, f.thigh, j.knee, f.knee)
        parts += capsule(j.knee, f.calf, j.ankle, f.ankle)
        parts += smooth(listOf(
            j.ankle,
            Offset(j.ankle.x - side * f.ankle * 1.5f, j.ankle.y + 12f),
            Offset(j.ankle.x - side * f.ankle * 1.1f, j.ankle.y + 18f),
            Offset(j.ankle.x + side * f.ankle * 1.8f, j.ankle.y + 18f),
            Offset(j.ankle.x + side * f.ankle * 1.2f, j.ankle.y + 4f)
        ))
    }
    return union(parts)
}

private fun frontMuscles(f: Figure): List<Pair<MuscleGroup, Path>> {
    val j = Joints(-1f, f)
    val out = mutableListOf<Pair<MuscleGroup, Path>>()

    // Upper trapezius, the slope beside the neck
    out += MuscleGroup.TRAPS to smooth(listOf(
        Offset(CX - f.neckW, f.neckBot - 12f),
        Offset(CX - f.shoulder * 0.50f, f.neckBot + 4f),
        Offset(CX - f.shoulder * 0.42f, f.shoulderY + 6f),
        Offset(CX - f.neckW * 0.85f, f.shoulderY - 2f)
    ))
    // Deltoid
    out += MuscleGroup.SHOULDERS to capsule(
        Offset(j.shoulder.x + 3f, j.shoulder.y - 8f), f.uarm * 1.02f,
        Offset(j.shoulder.x - 1f, j.shoulder.y + 22f), f.uarm * 0.82f
    )
    // Pectoral
    out += MuscleGroup.CHEST to smooth(listOf(
        Offset(CX - 2.5f, f.shoulderY + 8f),
        Offset(CX - f.chest * 0.60f, f.shoulderY + 9f),
        Offset(CX - f.chest * 0.90f, f.shoulderY + 20f),
        Offset(CX - f.chest * 0.80f, f.chestY + 8f),
        Offset(CX - f.chest * 0.42f, f.chestY + 16f),
        Offset(CX - 2.5f, f.chestY + 12f)
    ))
    // Rectus abdominis, four segments a side
    val top = f.chestY + 22f
    for (i in 0 until 4) {
        val y0 = top + i * 11f
        val wq = f.waist * (0.50f - i * 0.028f)
        out += MuscleGroup.ABS to smooth(listOf(
            Offset(CX - 2.5f, y0), Offset(CX - wq, y0 + 0.5f),
            Offset(CX - wq, y0 + 8.5f), Offset(CX - 2.5f, y0 + 9f)
        ))
    }
    // Lower abdomen
    out += MuscleGroup.ABS to smooth(listOf(
        Offset(CX - 2.5f, top + 45f), Offset(CX - f.waist * 0.38f, top + 45f),
        Offset(CX - f.waist * 0.16f, f.hipY - 22f), Offset(CX - 2.5f, f.hipY - 19f)
    ))
    // Oblique
    out += MuscleGroup.ABS to smooth(listOf(
        Offset(CX - f.chest * 0.58f, f.chestY + 20f),
        Offset(CX - f.waist * 0.90f, f.waistY - 22f),
        Offset(CX - f.waist * 0.80f, f.waistY + 2f),
        Offset(CX - f.waist * 0.48f, f.waistY - 8f),
        Offset(CX - f.waist * 0.50f, f.chestY + 24f)
    ))
    out += MuscleGroup.BICEPS to capsule(
        Offset(j.shoulder.x, j.shoulder.y + 16f), f.uarm * 0.78f,
        Offset(j.elbow.x, j.elbow.y - 10f), f.elbow * 0.74f
    )
    out += MuscleGroup.FOREARMS to capsule(
        Offset(j.elbow.x, j.elbow.y + 3f), f.elbow * 0.86f,
        Offset(j.wrist.x, j.wrist.y - 10f), f.wrist * 1.05f
    )
    // Quadriceps: three heads
    out += MuscleGroup.QUADS to capsule(
        Offset(j.hip.x - f.thigh * 0.52f, j.hip.y + 16f), f.thigh * 0.42f,
        Offset(j.knee.x - f.knee * 0.62f, j.knee.y - 12f), f.knee * 0.52f
    )
    out += MuscleGroup.QUADS to capsule(
        Offset(j.hip.x + f.thigh * 0.10f, j.hip.y + 12f), f.thigh * 0.46f,
        Offset(j.knee.x + f.knee * 0.02f, j.knee.y - 10f), f.knee * 0.50f
    )
    out += MuscleGroup.QUADS to capsule(
        Offset(j.hip.x + f.thigh * 0.42f, j.knee.y - 54f), f.thigh * 0.26f,
        Offset(j.knee.x + f.knee * 0.46f, j.knee.y - 12f), f.knee * 0.42f
    )
    // Shin
    out += MuscleGroup.CALVES to capsule(
        Offset(j.knee.x - f.calf * 0.44f, j.knee.y + 14f), f.calf * 0.44f,
        Offset(j.ankle.x - f.ankle * 0.30f, j.ankle.y - 26f), f.ankle * 0.70f
    )
    out += MuscleGroup.CALVES to capsule(
        Offset(j.knee.x + f.calf * 0.42f, j.knee.y + 12f), f.calf * 0.40f,
        Offset(j.ankle.x + f.ankle * 0.50f, j.ankle.y - 30f), f.ankle * 0.60f
    )
    return out
}

private fun backMuscles(f: Figure): List<Pair<MuscleGroup, Path>> {
    val j = Joints(-1f, f)
    val out = mutableListOf<Pair<MuscleGroup, Path>>()

    // Trapezius, the diamond between the shoulder blades
    out += MuscleGroup.TRAPS to smooth(listOf(
        Offset(CX - 1.5f, f.neckBot - 6f),
        Offset(CX - f.shoulder * 0.62f, f.neckBot + 8f),
        Offset(CX - f.shoulder * 0.50f, f.shoulderY + 12f),
        Offset(CX - f.chest * 0.44f, f.chestY + 4f),
        Offset(CX - 1.5f, f.chestY + 26f)
    ))
    out += MuscleGroup.SHOULDERS to capsule(
        Offset(j.shoulder.x + 3f, j.shoulder.y - 8f), f.uarm * 1.02f,
        Offset(j.shoulder.x - 1f, j.shoulder.y + 22f), f.uarm * 0.82f
    )
    // Latissimus dorsi
    out += MuscleGroup.BACK to smooth(listOf(
        Offset(CX - f.chest * 0.82f, f.chestY - 10f),
        Offset(CX - f.chest * 0.90f, f.chestY + 16f),
        Offset(CX - f.waist * 0.80f, f.waistY - 16f),
        Offset(CX - f.waist * 0.26f, f.waistY - 2f),
        Offset(CX - 3f, f.waistY - 18f),
        Offset(CX - 3f, f.chestY + 2f)
    ))
    // Erector spinae
    out += MuscleGroup.LOWER_BACK to smooth(listOf(
        Offset(CX - 3f, f.waistY - 4f),
        Offset(CX - f.waist * 0.42f, f.waistY + 8f),
        Offset(CX - f.waist * 0.40f, f.hipY - 20f),
        Offset(CX - 3f, f.hipY - 16f)
    ))
    out += MuscleGroup.TRICEPS to capsule(
        Offset(j.shoulder.x - 1f, j.shoulder.y + 16f), f.uarm * 0.76f,
        Offset(j.elbow.x, j.elbow.y - 10f), f.elbow * 0.72f
    )
    out += MuscleGroup.FOREARMS to capsule(
        Offset(j.elbow.x, j.elbow.y + 3f), f.elbow * 0.86f,
        Offset(j.wrist.x, j.wrist.y - 10f), f.wrist * 1.05f
    )
    // Gluteus
    out += MuscleGroup.GLUTES to smooth(listOf(
        Offset(CX - 3f, f.hipY - 16f),
        Offset(CX - f.hip * 0.86f, f.hipY - 12f),
        Offset(CX - f.hip * 0.88f, f.hipY + 16f),
        Offset(CX - f.hip * 0.34f, f.hipY + 26f),
        Offset(CX - 3f, f.hipY + 20f)
    ))
    // Hamstrings, two heads
    out += MuscleGroup.HAMSTRINGS to capsule(
        Offset(j.hip.x - f.thigh * 0.40f, j.hip.y + 30f), f.thigh * 0.40f,
        Offset(j.knee.x - f.knee * 0.52f, j.knee.y - 14f), f.knee * 0.48f
    )
    out += MuscleGroup.HAMSTRINGS to capsule(
        Offset(j.hip.x + f.thigh * 0.30f, j.hip.y + 28f), f.thigh * 0.42f,
        Offset(j.knee.x + f.knee * 0.36f, j.knee.y - 12f), f.knee * 0.46f
    )
    // Gastrocnemius
    out += MuscleGroup.CALVES to capsule(
        Offset(j.knee.x - f.calf * 0.46f, j.knee.y + 10f), f.calf * 0.48f,
        Offset(j.ankle.x - f.ankle * 0.20f, j.ankle.y - 32f), f.ankle * 0.65f
    )
    out += MuscleGroup.CALVES to capsule(
        Offset(j.knee.x + f.calf * 0.44f, j.knee.y + 10f), f.calf * 0.44f,
        Offset(j.ankle.x + f.ankle * 0.60f, j.ankle.y - 34f), f.ankle * 0.55f
    )
    return out
}

/**
 * Builds one view, scaled from the 200x380 view box to [width] x [height]. Muscles are
 * authored on the left and mirrored, so both halves always match.
 */
fun buildAnatomy(figure: BodyFigure, view: BodyView, width: Float, height: Float): Anatomy {
    val f = if (figure == BodyFigure.FEMALE) FEMALE_FIGURE else MALE_FIGURE
    val half = if (view == BodyView.FRONT) frontMuscles(f) else backMuscles(f)
    val muscles = half.flatMap { (group, path) -> listOf(group to path, group to path.mirroredX()) }

    val scale = Matrix().apply { scale(width / VB_W, height / VB_H) }
    val body = bodyPath(f).also { it.transform(scale) }
    muscles.forEach { (_, p) -> p.transform(scale) }
    return Anatomy(body, muscles)
}
