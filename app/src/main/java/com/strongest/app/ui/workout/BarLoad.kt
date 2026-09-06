package com.strongest.app.ui.workout

/**
 * A bar with plates loaded onto it.
 *
 * [perSide] counts plates on **one** side of a barbell, mirrored onto the other side —
 * which is how you actually load a bar. When [singleSide] is true (a machine with a
 * single pin, a loadable dumbbell) the counts are taken at face value instead.
 */
data class BarLoad(
    val bar: Float,
    val perSide: Map<Float, Int> = emptyMap(),
    val singleSide: Boolean = false
) {
    /** Plate weight on one side. */
    val plateWeight: Float
        get() = perSide.entries.fold(0f) { acc, (plate, count) -> acc + plate * count }

    /** What the bar weighs as loaded. */
    val total: Float
        get() = bar + plateWeight * if (singleSide) 1f else 2f

    /** Loaded plates, heaviest first — the order they go on the sleeve. */
    val loaded: List<Pair<Float, Int>>
        get() = perSide.filterValues { it > 0 }.toList().sortedByDescending { it.first }

    val isEmpty: Boolean get() = perSide.none { it.value > 0 }
}

/**
 * How many of [owned] plates can go on one side. A barbell needs a matching plate on the
 * far side, so an odd plate out cannot be used.
 */
fun maxPerSide(owned: Int, singleSide: Boolean): Int = if (singleSide) owned else owned / 2

fun BarLoad.countOf(plate: Float): Int = perSide[plate] ?: 0

fun BarLoad.canAdd(plate: Float, owned: Int): Boolean =
    countOf(plate) < maxPerSide(owned, singleSide)

fun BarLoad.add(plate: Float, owned: Int): BarLoad =
    if (!canAdd(plate, owned)) this
    else copy(perSide = perSide + (plate to countOf(plate) + 1))

fun BarLoad.remove(plate: Float): BarLoad =
    if (countOf(plate) <= 0) this
    else copy(perSide = perSide + (plate to countOf(plate) - 1))

fun BarLoad.cleared(): BarLoad = copy(perSide = emptyMap())

/**
 * Re-caps the current load against a new bar/mode/inventory, dropping plates that no
 * longer fit. Used when the bar or the single-side mode changes underneath a load.
 */
fun BarLoad.constrainedTo(availablePlates: Map<Float, Int>): BarLoad = copy(
    perSide = perSide.mapValues { (plate, count) ->
        count.coerceAtMost(maxPerSide(availablePlates[plate] ?: 0, singleSide))
    }.filterValues { it > 0 }
)

/** The closest load to [target] that the available plates allow — the auto-fill. */
fun barLoadForTarget(
    target: Float,
    bar: Float,
    availablePlates: Map<Float, Int>,
    singleSide: Boolean = false
): BarLoad = BarLoad(
    bar = bar,
    perSide = calculatePlates(target, bar, availablePlates, singleSide).perSide.toMap(),
    singleSide = singleSide
)
