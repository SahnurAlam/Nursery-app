package com.sahnurnursery.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.sahnurnursery.app.navigation.AppNavigation
import com.sahnurnursery.app.ui.theme.SahnurNurseryTheme
import com.sahnurnursery.app.viewmodel.NurseryViewModel
import com.sahnurnursery.app.viewmodel.NurseryViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: NurseryViewModel by viewModels {
        NurseryViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SahnurNurseryTheme {
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}
