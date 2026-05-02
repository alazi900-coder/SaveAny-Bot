package com.krau.saveany.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.krau.saveany.R
import com.krau.saveany.SaveAnyApp as SaveAnyApplication
import com.krau.saveany.data.prefs.ThemeMode
import com.krau.saveany.ui.nav.Routes
import com.krau.saveany.ui.screens.create.CreateTaskScreen
import com.krau.saveany.ui.screens.home.HomeScreen
import com.krau.saveany.ui.screens.settings.SettingsScreen
import com.krau.saveany.ui.screens.storages.StoragesScreen
import com.krau.saveany.ui.screens.tasks.TaskDetailScreen
import com.krau.saveany.ui.screens.tasks.TasksScreen
import com.krau.saveany.ui.theme.SaveAnyTheme

@Composable
fun SaveAnyAppRoot() {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as SaveAnyApplication).container }
    val state by container.settings.flow.collectAsState(initial = com.krau.saveany.data.prefs.SettingsState())

    val isDark = when (state.themeMode) {
        ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    SaveAnyTheme(darkTheme = isDark, dynamicColor = state.dynamicColor) {
        val nav = rememberNavController()
        val backStack by nav.currentBackStackEntryAsState()
        val current = backStack?.destination?.route
        val snackbarHostState = remember { SnackbarHostState() }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (current in setOf(Routes.HOME, Routes.TASKS, Routes.STORAGES, Routes.SETTINGS)) {
                    NavigationBar {
                        BottomTab(Routes.HOME, R.string.nav_home, Icons.Outlined.Home, current, nav)
                        BottomTab(Routes.TASKS, R.string.nav_tasks, Icons.Outlined.ListAlt, current, nav)
                        BottomTab(Routes.STORAGES, R.string.nav_storages, Icons.Outlined.Cloud, current, nav)
                        BottomTab(Routes.SETTINGS, R.string.nav_settings, Icons.Outlined.Settings, current, nav)
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                NavHost(navController = nav, startDestination = Routes.HOME) {
                    composable(Routes.HOME) {
                        HomeScreen(
                            container = container,
                            snackbarHostState = snackbarHostState,
                            onCreateTask = { type -> nav.navigate(Routes.create(type)) },
                            onOpenSettings = { nav.navigate(Routes.SETTINGS) },
                            onOpenTask = { id -> nav.navigate(Routes.task(id)) }
                        )
                    }
                    composable(Routes.TASKS) {
                        TasksScreen(
                            container = container,
                            snackbarHostState = snackbarHostState,
                            onOpenTask = { id -> nav.navigate(Routes.task(id)) }
                        )
                    }
                    composable(Routes.STORAGES) {
                        StoragesScreen(
                            container = container,
                            snackbarHostState = snackbarHostState
                        )
                    }
                    composable(Routes.SETTINGS) {
                        SettingsScreen(
                            container = container,
                            snackbarHostState = snackbarHostState
                        )
                    }
                    composable(Routes.CREATE_TASK_PATTERN) { entry ->
                        val typeArg = entry.arguments?.getString("type")
                        CreateTaskScreen(
                            container = container,
                            initialType = typeArg,
                            initialUrl = "",
                            snackbarHostState = snackbarHostState,
                            onClose = { nav.popBackStack() }
                        )
                    }
                    composable(Routes.TASK_DETAIL_PATTERN) { entry ->
                        val id = entry.arguments?.getString("id").orEmpty()
                        TaskDetailScreen(
                            container = container,
                            taskId = id,
                            snackbarHostState = snackbarHostState,
                            onClose = { nav.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.BottomTab(
    route: String,
    labelRes: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    current: String?,
    nav: androidx.navigation.NavHostController
) {
    NavigationBarItem(
        selected = current == route,
        onClick = {
            if (current != route) {
                nav.navigate(route) {
                    popUpTo(Routes.HOME) { inclusive = false }
                    launchSingleTop = true
                }
            }
        },
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(stringResource(labelRes)) }
    )
}
