package com.strongest.app.utils

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import java.text.SimpleDateFormat

/**
 * A date format for [skeleton] (e.g. "MMMdyyyy") laid out the way the current locale expects:
 * "Mar 5, 2026" in English, "5 mrt 2026" in Dutch. A fixed pattern would keep the US order.
 */
@Composable
fun rememberLocalizedDateFormat(skeleton: String): SimpleDateFormat {
    val locale = LocalConfiguration.current.locales[0]
    return remember(locale, skeleton) {
        SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, skeleton), locale)
    }
}
