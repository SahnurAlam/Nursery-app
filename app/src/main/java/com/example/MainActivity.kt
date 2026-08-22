package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AppThemeMode
import com.example.ui.navigation.NurseryApp
import com.example.ui.theme.SahnurNurseryTheme
import com.example.ui.viewmodel.NurseryViewModel
import com.example.ui.viewmodel.NurseryViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: NurseryViewModel by viewModels {
        NurseryViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
            val isDark = when (preferences.themeMode) {
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            SahnurNurseryTheme(darkTheme = isDark) {
                NurseryApp(viewModel = viewModel)
            }
        }
    }
}
