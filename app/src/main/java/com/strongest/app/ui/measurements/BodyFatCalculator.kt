package com.strongest.app.ui.measurements

import com.strongest.app.data.repository.CaliperMode
import com.strongest.app.data.repository.Sex
import kotlin.math.log10

/**
 * Skinfold sites used across the supported caliper protocols. [instruction] guides the user on
 * where and how to pinch each site (all measured on the right side of the body, in millimetres).
 */
enum class SkinfoldSite(val displayName: String, val instruction: String) {
    CHEST(
        "Chest",
        "Diagonal fold, halfway between the front armpit crease and the nipple (men) or one-third of the way (women)."
    ),
    ABDOMEN(
        "Abdomen",
        "Vertical fold about 2 cm to the right of the navel."
    ),
    THIGH(
        "Thigh",
        "Vertical fold on the front of the thigh, midway between the hip crease and the top of the kneecap."
    ),
    TRICEPS(
        "Triceps",
        "Vertical fold on the back of the upper arm, midway between the shoulder and elbow, arm relaxed."
    ),
    SUPRAILIAC(
        "Suprailiac",
        "Diagonal fold just above the hip bone, following the natural angle of the iliac crest."
    ),
    MIDAXILLARY(
        "Midaxillary",
        "Vertical fold on the midline of the side of the torso, level with the bottom of the sternum."
    ),
    SUBSCAPULAR(
        "Subscapular",
        "Diagonal fold just below the bottom tip of the shoulder blade."
    ),
    BICEPS(
        "Biceps",
        "Vertical fold on the front of the upper arm, midway between the shoulder and elbow, arm relaxed."
    )
}

object BodyFatCalculator {

    /** Ordered list of sites the user must measure for the given protocol + sex. */
    fun sitesFor(mode: CaliperMode, sex: Sex): List<SkinfoldSite> = when (mode) {
        CaliperMode.THREE_SITE ->
            if (sex == Sex.FEMALE) listOf(SkinfoldSite.TRICEPS, SkinfoldSite.SUPRAILIAC, SkinfoldSite.THIGH)
            else listOf(SkinfoldSite.CHEST, SkinfoldSite.ABDOMEN, SkinfoldSite.THIGH)
        CaliperMode.FOUR_SITE ->
            listOf(SkinfoldSite.BICEPS, SkinfoldSite.TRICEPS, SkinfoldSite.SUBSCAPULAR, SkinfoldSite.SUPRAILIAC)
        CaliperMode.SEVEN_SITE -> listOf(
            SkinfoldSite.CHEST, SkinfoldSite.MIDAXILLARY, SkinfoldSite.TRICEPS, SkinfoldSite.SUBSCAPULAR,
            SkinfoldSite.ABDOMEN, SkinfoldSite.SUPRAILIAC, SkinfoldSite.THIGH
        )
    }

    /**
     * Computes body-fat percentage from skinfold values (mm). Returns null if inputs are unusable.
     * Body density is derived per protocol/sex/age, then converted to % via the Siri equation.
     */
    fun bodyFatPercent(
        mode: CaliperMode,
        sex: Sex,
        ageYears: Int,
        values: Map<SkinfoldSite, Float>
    ): Float? {
        if (sex == Sex.UNSET || ageYears <= 0) return null
        val sites = sitesFor(mode, sex)
        val sum = sites.sumOf { (values[it] ?: return null).toDouble() }
        if (sum <= 0.0) return null

        val density = when (mode) {
            CaliperMode.THREE_SITE -> jacksonPollock3(sex, ageYears, sum)
            CaliperMode.SEVEN_SITE -> jacksonPollock7(sex, ageYears, sum)
            CaliperMode.FOUR_SITE -> durninWomersley4(sex, ageYears, sum)
        }
        if (density <= 0.0) return null
        val siri = 495.0 / density - 450.0
        return siri.coerceIn(2.0, 60.0).toFloat()
    }

    private fun jacksonPollock3(sex: Sex, age: Int, s: Double): Double =
        if (sex == Sex.FEMALE) {
            1.0994921 - 0.0009929 * s + 0.0000023 * s * s - 0.0001392 * age
        } else {
            1.10938 - 0.0008267 * s + 0.0000016 * s * s - 0.0002574 * age
        }

    private fun jacksonPollock7(sex: Sex, age: Int, s: Double): Double =
        if (sex == Sex.FEMALE) {
            1.097 - 0.00046971 * s + 0.00000056 * s * s - 0.00012828 * age
        } else {
            1.112 - 0.00043499 * s + 0.00000055 * s * s - 0.00028826 * age
        }

    /** Durnin & Womersley (1974): BD = c − m·log10(sum), constants by sex and age bracket. */
    private fun durninWomersley4(sex: Sex, age: Int, s: Double): Double {
        val (c, m) = if (sex == Sex.FEMALE) {
            when {
                age < 20 -> 1.1549 to 0.0678
                age < 30 -> 1.1599 to 0.0717
                age < 40 -> 1.1423 to 0.0632
                age < 50 -> 1.1333 to 0.0612
                else -> 1.1339 to 0.0645
            }
        } else {
            when {
                age < 20 -> 1.1620 to 0.0630
                age < 30 -> 1.1631 to 0.0632
                age < 40 -> 1.1422 to 0.0544
                age < 50 -> 1.1620 to 0.0700
                else -> 1.1715 to 0.0779
            }
        }
        return c - m * log10(s)
    }
}
