package com.sahnurnursery.app.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sahnurnursery.app.model.UiFeedback
import com.sahnurnursery.app.ui.screens.DashboardScreen
import com.sahnurnursery.app.ui.screens.ExpensesScreen
import com.sahnurnursery.app.ui.screens.PlantsScreen
import com.sahnurnursery.app.ui.screens.SalesScreen
import com.sahnurnursery.app.viewmodel.NurseryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    viewModel: NurseryViewModel,
    navController: NavHostController = rememberNavController()
) {
    val items = listOf(
        Screen.Dashboard,
        Screen.Plants,
        Screen.Sales,
        Screen.Expenses
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val snackbarHostState = remember { SnackbarHostState() }
    val feedback by viewModel.uiFeedback.collectAsStateWithLifecycle()

    // Handle global feedback events
    LaunchedEffect(feedback) {
        val currentFeedback = feedback
        if (currentFeedback != null) {
            val result = snackbarHostState.showSnackbar(
                message = currentFeedback.message,
                actionLabel = when (currentFeedback) {
                    is UiFeedback.Error -> currentFeedback.actionLabel ?: "Dismiss"
                    else -> null
                },
                duration = when (currentFeedback) {
                    is UiFeedback.Error -> SnackbarDuration.Long
                    else -> SnackbarDuration.Short
                }
            )
            if (result == SnackbarResult.ActionPerformed && currentFeedback is UiFeedback.Error) {
                currentFeedback.retryAction?.invoke()
            }
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val isError = feedback is UiFeedback.Error
                Snackbar(
                    snackbarData = data,
                    containerColor = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.inverseSurface,
                    contentColor = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.inverseOnSurface,
                    actionColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.inversePrimary
                )
            }
        },
        bottomBar = {
            NavigationBar {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { screen.icon?.let { Icon(it, contentDescription = screen.title) } },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(viewModel = viewModel)
            }
            composable(Screen.Plants.route) {
                PlantsScreen(viewModel = viewModel)
            }
            composable(Screen.Sales.route) {
                SalesScreen(viewModel = viewModel)
            }
            composable(Screen.Expenses.route) {
                ExpensesScreen(viewModel = viewModel)
            }
        }
    }
}
