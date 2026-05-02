package com.krau.saveany.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.krau.saveany.R
import com.krau.saveany.data.AppContainer
import com.krau.saveany.data.prefs.ServerEntry
import com.krau.saveany.data.prefs.ThemeMode
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    container: AppContainer,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    val state by container.settings.flow.collectAsState(initial = com.krau.saveany.data.prefs.SettingsState())

    var editTarget by remember { mutableStateOf<ServerEntry?>(null) }
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_settings)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Outlined.Add, contentDescription = null)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(stringResource(R.string.settings_servers), style = MaterialTheme.typography.titleMedium)
            }
            if (state.servers.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.settings_no_servers),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(state.servers, key = { it.id }) { server ->
                    ServerCard(
                        server = server,
                        active = server.id == (state.activeServer?.id),
                        onClick = {
                            scope.launch {
                                container.settings.update { it.copy(activeServerId = server.id) }
                            }
                        },
                        onEdit = { editTarget = server },
                        onDelete = {
                            scope.launch {
                                container.settings.update { s ->
                                    val left = s.servers.filterNot { it.id == server.id }
                                    s.copy(
                                        servers = left,
                                        activeServerId = if (s.activeServerId == server.id) left.firstOrNull()?.id else s.activeServerId
                                    )
                                }
                            }
                        },
                        onTest = {
                            scope.launch {
                                runCatching { container.repo.healthFor(server.baseUrl, server.token) }
                                    .onSuccess { snackbarHostState.showSnackbar("Connected — ${it.status}") }
                                    .onFailure { snackbarHostState.showSnackbar("Failed: ${it.message}") }
                            }
                        }
                    )
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text("Appearance", style = MaterialTheme.typography.titleMedium)
            }

            item {
                ThemePicker(
                    mode = state.themeMode,
                    onPick = { mode -> scope.launch { container.settings.update { it.copy(themeMode = mode) } } }
                )
            }

            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.settings_dynamic_color),
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = state.dynamicColor,
                            onCheckedChange = { v ->
                                scope.launch { container.settings.update { it.copy(dynamicColor = v) } }
                            }
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.settings_about),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            stringResource(R.string.settings_about_body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showAdd) {
        ServerEditDialog(
            initial = null,
            onDismiss = { showAdd = false },
            onSave = { name, url, token ->
                showAdd = false
                scope.launch {
                    container.settings.update { s ->
                        val entry = ServerEntry(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            baseUrl = url,
                            token = token
                        )
                        s.copy(
                            servers = s.servers + entry,
                            activeServerId = s.activeServerId ?: entry.id
                        )
                    }
                }
            }
        )
    }

    editTarget?.let { target ->
        ServerEditDialog(
            initial = target,
            onDismiss = { editTarget = null },
            onSave = { name, url, token ->
                editTarget = null
                scope.launch {
                    container.settings.update { s ->
                        val updated = s.servers.map {
                            if (it.id == target.id) it.copy(name = name, baseUrl = url, token = token) else it
                        }
                        s.copy(servers = updated)
                    }
                }
            }
        )
    }
}

@Composable
private fun ServerCard(
    server: ServerEntry,
    active: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Dns, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(0.dp).then(Modifier.padding(start = 8.dp)))
                Column(Modifier.weight(1f)) {
                    Text(server.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    Text(
                        server.baseUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (active) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = stringResource(R.string.settings_active),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onTest) { Text(stringResource(R.string.settings_test_connection)) }
                TextButton(onClick = onEdit) { Text("Edit") }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.settings_delete))
                }
            }
        }
    }
}

@Composable
private fun ServerEditDialog(
    initial: ServerEntry?,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var url by remember { mutableStateOf(initial?.baseUrl.orEmpty()) }
    var token by remember { mutableStateOf(initial?.token.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) stringResource(R.string.settings_add_server) else "Edit server") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.settings_server_name)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(stringResource(R.string.settings_server_url)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    label = { Text(stringResource(R.string.settings_server_token)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && url.isNotBlank(),
                onClick = { onSave(name.trim(), url.trim(), token.trim()) }
            ) { Text(stringResource(R.string.settings_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) }
        }
    )
}

@Composable
private fun ThemePicker(mode: ThemeMode, onPick: (ThemeMode) -> Unit) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.settings_dark_mode), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { m ->
                    val selected = m == mode
                    Button(
                        onClick = { onPick(m) },
                        colors = if (selected)
                            androidx.compose.material3.ButtonDefaults.buttonColors()
                        else
                            androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
                    ) { Text(m.name.lowercase()) }
                }
            }
        }
    }
}
