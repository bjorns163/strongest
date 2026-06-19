package com.strongest.app.ui.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.strongest.app.StrongestApp

@Composable
fun rememberExerciseAssetUri(exerciseId: Long): String {
    val context = LocalContext.current
    return remember(exerciseId) {
        val extensions = listOf("gif", "webp")
        for (ext in extensions) {
            val path = "exercises/$exerciseId.$ext"
            try {
                context.assets.open(path).use { }
                return@remember "file:///android_asset/$path"
            } catch (_: Exception) { }
        }
        "file:///android_asset/exercises/$exerciseId.gif"
    }
}

@Composable
fun ExerciseThumbnail(
    exerciseId: Long,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    fallbackIconSize: Dp = 24.dp,
) {
    val context = LocalContext.current
    val staticLoader = remember(context) {
        (context.applicationContext as StrongestApp).staticImageLoader
    }
    SubcomposeAsyncImage(
        model = rememberExerciseAssetUri(exerciseId),
        imageLoader = staticLoader,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier.background(Color.White),
        loading = {},
        error = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(fallbackIconSize)
                )
            }
        }
    )
}

@Composable
fun ExerciseMovementImage(
    exerciseId: Long,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    fallbackIconSize: Dp = 64.dp,
) {
    SubcomposeAsyncImage(
        model = rememberExerciseAssetUri(exerciseId),
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier.background(Color.White),
        loading = {},
        error = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(fallbackIconSize)
                )
            }
        }
    )
}
