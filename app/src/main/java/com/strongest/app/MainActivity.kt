package com.strongest.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.strongest.app.data.repository.SettingsRepository
import com.strongest.app.ui.navigation.AppNavigation
import com.strongest.app.ui.theme.StrongestTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val theme by settingsRepository.themeFlow.collectAsState(initial = ThemeMode.SYSTEM)
            val darkTheme = when (theme) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            StrongestTheme(darkTheme = darkTheme) {
                AppNavigation()
            }
        }
    }
}

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}
