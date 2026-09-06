package com.strongest.app.ui.measurements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import coil3.compose.SubcomposeAsyncImage
import com.strongest.app.data.repository.CaliperMode
import com.strongest.app.utils.parseDecimalInput
import com.strongest.app.data.repository.Sex
import java.util.Calendar

/**
 * Resolves the asset URI for a skinfold site's guidance image at `assets/caliper/<site>.<ext>`,
 * probing common formats. Returns null if no image is bundled for that site.
 */
@Composable
private fun rememberCaliperImageUri(site: SkinfoldSite): String? {
    val context = LocalContext.current
    return remember(site) {
        val key = site.name.lowercase()
        listOf("png", "webp", "jpg", "jpeg").firstNotNullOfOrNull { ext ->
            val path = "caliper/$key.$ext"
            try {
                context.assets.open(path).use { }
                "file:///android_asset/$path"
            } catch (_: Exception) {
                null
            }
        }
    }
}

/**
 * Guided multi-step skinfold body-fat flow. Collects the profile (sex + birth year) if missing,
 * then one skinfold per site for the active [CaliperProfile.mode], and finally computes a body-fat %
 * via [BodyFatCalculator]. The result is saved as a normal BODY_FAT measurement through [onSave].
 */
@Composable
fun CaliperBodyFatDialog(
    profile: CaliperProfile,
    onSaveProfile: (Sex, Int) -> Unit,
    onSave: (percent: Float, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    val mode: CaliperMode = profile.mode
    val needProfileStep = profile.sex == Sex.UNSET || profile.birthYear <= 0

    var sex by remember { mutableStateOf(profile.sex) }
    var birthYearText by remember {
        mutableStateOf(if (profile.birthYear > 0) profile.birthYear.toString() else "")
    }
    val values = remember { mutableStateMapOf<SkinfoldSite, String>() }
    var step by remember { mutableIntStateOf(0) }

    val sites = remember(mode, sex) {
        if (sex == Sex.UNSET) emptyList() else BodyFatCalculator.sitesFor(mode, sex)
    }
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    val birthYear = birthYearText.toIntOrNull() ?: 0
    val age = if (birthYear in 1900..currentYear) currentYear - birthYear else 0

    val profileOffset = if (needProfileStep) 1 else 0
    val resultStep = profileOffset + sites.size
    val onProfileStep = needProfileStep && step == 0
    val siteIndex = step - profileOffset
    val onResultStep = step >= resultStep && sites.isNotEmpty()

    val computedPercent: Float? = if (onResultStep) {
        BodyFatCalculator.bodyFatPercent(
            mode = mode,
            sex = sex,
            ageYears = age,
            values = sites.associateWith { (values[it]?.let(::parseDecimalInput) ?: 0f) }
        )
    } else null

    val profileValid = sex != Sex.UNSET && age > 0
    val currentSite = sites.getOrNull(siteIndex)
    val currentSiteValid = currentSite != null &&
        (values[currentSite]?.let(::parseDecimalInput)?.let { it > 0f } == true)

    val canAdvance = when {
        onProfileStep -> profileValid
        onResultStep -> computedPercent != null
        else -> currentSiteValid
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                when {
                    onProfileStep -> "Your profile"
                    onResultStep -> "Body fat result"
                    else -> "Skinfold ${siteIndex + 1} of ${sites.size}"
                }
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                when {
                    onProfileStep -> {
                        Text(
                            "The caliper formula needs your gender and age. This is saved once.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = sex == Sex.MALE,
                                onClick = { sex = Sex.MALE },
                                label = { Text("Male") }
                            )
                            FilterChip(
                                selected = sex == Sex.FEMALE,
                                onClick = { sex = Sex.FEMALE },
                                label = { Text("Female") }
                            )
                        }
                        OutlinedTextField(
                            value = birthYearText,
                            onValueChange = { birthYearText = it.filter { c -> c.isDigit() }.take(4) },
                            label = { Text("Birth year") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        )
                    }

                    onResultStep -> {
                        if (computedPercent != null) {
                            Text(
                                text = "${"%.1f".format(computedPercent)}%",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${mode.label} • sum ${"%.0f".format(sites.sumOf { (values[it]?.let(::parseDecimalInput)?.toDouble() ?: 0.0) })} mm",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            sites.forEach { site ->
                                Text(
                                    text = "${site.displayName}: ${values[site] ?: "-"} mm",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        } else {
                            Text("Couldn't compute — please check the values.")
                        }
                    }

                    currentSite != null -> {
                        Text(
                            text = currentSite.displayName,
                            style = MaterialTheme.typography.titleMedium
                        )
                        val imageUri = rememberCaliperImageUri(currentSite)
                        if (imageUri != null) {
                            SubcomposeAsyncImage(
                                model = imageUri,
                                contentDescription = "${currentSite.displayName} skinfold location",
                                contentScale = ContentScale.Fit,
                                loading = {},
                                error = {},
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .padding(top = 8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                        Text(
                            text = currentSite.instruction,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        OutlinedTextField(
                            value = values[currentSite] ?: "",
                            onValueChange = { input ->
                                values[currentSite] = input.filter { c -> c.isDigit() || c == '.' }
                            },
                            label = { Text("Skinfold (mm)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canAdvance,
                onClick = {
                    if (onResultStep) {
                        val percent = computedPercent ?: return@TextButton
                        val notes = sites.joinToString(", ") { "${it.displayName} ${values[it]}mm" }
                        onSave(percent, "${mode.label}: $notes")
                    } else {
                        if (onProfileStep) onSaveProfile(sex, birthYear)
                        step++
                    }
                }
            ) {
                Text(if (onResultStep) "Save" else "Next")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (step == 0) onDismiss() else step--
                }
            ) {
                Text(if (step == 0) "Cancel" else "Back")
            }
        }
    )
}
