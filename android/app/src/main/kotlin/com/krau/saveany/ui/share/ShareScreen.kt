package com.krau.saveany.ui.share

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.krau.saveany.R
import com.krau.saveany.SaveAnyApp
import com.krau.saveany.data.prefs.ThemeMode
import com.krau.saveany.ui.screens.create.CreateTaskScreen
import com.krau.saveany.ui.theme.SaveAnyTheme
import com.krau.saveany.ui.util.guessTaskTypeFromUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareScreen(initialText: String, onClose: () -> Unit) {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as SaveAnyApp).container }
    val state by container.settings.flow.collectAsState(initial = com.krau.saveany.data.prefs.SettingsState())
    val isDark = when (state.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    SaveAnyTheme(darkTheme = isDark, dynamicColor = state.dynamicColor) {
        val snackbarHostState = remember { SnackbarHostState() }
        val activeServer = state.activeServer
        if (activeServer == null) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(R.string.share_title)) },
                        navigationIcon = {
                            IconButton(onClick = onClose) {
                                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                            }
                        }
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { padding ->
                Column(
                    Modifier.fillMaxSize().padding(padding).padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                stringResource(R.string.snack_no_active_server),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                initialText.ifBlank { "—" },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Button(onClick = onClose) { Text(stringResource(R.string.dialog_cancel)) }
                }
            }
        } else {
            val guessed = guessTaskTypeFromUrl(initialText).wire
            CreateTaskScreen(
                container = container,
                initialType = guessed,
                initialUrl = initialText,
                snackbarHostState = snackbarHostState,
                onClose = onClose
            )
        }
    }
}
