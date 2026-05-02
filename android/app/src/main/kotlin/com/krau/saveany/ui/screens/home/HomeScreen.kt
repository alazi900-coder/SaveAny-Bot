package com.krau.saveany.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddLink
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CompareArrows
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.krau.saveany.R
import com.krau.saveany.data.AppContainer
import com.krau.saveany.data.api.CreateTaskRequest
import com.krau.saveany.data.api.DirectLinksParams
import com.krau.saveany.data.api.TaskInfo
import com.krau.saveany.data.api.TaskType
import com.krau.saveany.data.api.YtdlpParams
import com.krau.saveany.data.api.Aria2Params
import com.krau.saveany.data.api.TGFilesParams
import com.krau.saveany.data.api.TPHPicsParams
import com.krau.saveany.data.api.ParsedParams
import com.krau.saveany.ui.components.GradientProgress
import com.krau.saveany.ui.util.descriptionRes
import com.krau.saveany.ui.util.fraction
import com.krau.saveany.ui.util.guessTaskTypeFromUrl
import com.krau.saveany.ui.util.labelRes
import com.krau.saveany.ui.util.percentInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    container: AppContainer,
    snackbarHostState: SnackbarHostState,
    onCreateTask: (String?) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenTask: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val activeServer by container.repo.activeServer.collectAsState(initial = null)

    var quickInput by remember { mutableStateOf("") }
    var tasks by remember { mutableStateOf<List<TaskInfo>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(activeServer?.id) {
        while (activeServer != null) {
            runCatching { container.repo.listTasks() }
                .onSuccess { tasks = it; error = null }
                .onFailure { error = it.message }
            delay(2500)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Column {
                    Text(stringResource(R.string.app_name), fontWeight = FontWeight.SemiBold)
                    activeServer?.let {
                        Text(
                            text = it.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            })
        }
    ) { padding ->
        if (activeServer == null) {
            EmptyServerState(onOpenSettings = onOpenSettings, padding = padding)
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { HeroBanner(onCreate = { onCreateTask(null) }) }

            item {
                QuickSaveCard(
                    value = quickInput,
                    onValueChange = { quickInput = it },
                    onSubmit = {
                        val url = quickInput.trim()
                        if (url.isEmpty()) return@QuickSaveCard
                        scope.launch {
                            val type = guessTaskTypeFromUrl(url)
                            val storage = runCatching { container.repo.storages().firstOrNull()?.name }.getOrNull()
                            if (storage == null) {
                                snackbarHostState.showSnackbar("No storages available")
                                return@launch
                            }
                            val params = buildQuickSaveParams(type, url)
                            runCatching {
                                container.repo.createTask(
                                    CreateTaskRequest(
                                        type = type,
                                        storage = storage,
                                        path = "",
                                        params = params
                                    )
                                )
                            }.onSuccess {
                                quickInput = ""
                                snackbarHostState.showSnackbar("Saved as ${type.name.lowercase()} task")
                            }.onFailure {
                                snackbarHostState.showSnackbar("Error: ${it.message}")
                            }
                        }
                    }
                )
            }

            item { TaskTypeGrid(onPick = onCreateTask) }

            item {
                Text(
                    stringResource(R.string.home_recent),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (tasks.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.home_no_recent),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(tasks.take(5), key = { it.taskId }) { task ->
                    HomeTaskCard(task = task, onClick = { onOpenTask(task.taskId) })
                }
            }

            error?.let {
                item {
                    Text(
                        stringResource(R.string.snack_error, it),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private fun buildQuickSaveParams(type: TaskType, url: String) = when (type) {
    TaskType.DIRECTLINKS -> Json.encodeToJsonElement(
        DirectLinksParams.serializer(), DirectLinksParams(listOf(url))
    )
    TaskType.YTDLP -> Json.encodeToJsonElement(
        YtdlpParams.serializer(), YtdlpParams(listOf(url))
    )
    TaskType.ARIA2 -> Json.encodeToJsonElement(
        Aria2Params.serializer(), Aria2Params(listOf(url))
    )
    TaskType.TGFILES -> Json.encodeToJsonElement(
        TGFilesParams.serializer(), TGFilesParams(listOf(url))
    )
    TaskType.TPHPICS -> Json.encodeToJsonElement(
        TPHPicsParams.serializer(), TPHPicsParams(url)
    )
    TaskType.PARSED -> Json.encodeToJsonElement(
        ParsedParams.serializer(), ParsedParams(url)
    )
    TaskType.TRANSFER -> error("Transfer task is not eligible for quick save")
}

@Composable
private fun HeroBanner(onCreate: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(cs.primary, cs.tertiary, cs.secondary)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    "Save anything, anywhere.",
                    style = MaterialTheme.typography.headlineMedium,
                    color = cs.onPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Telegram media · 1000+ websites via yt-dlp · BitTorrent · Telegraph · cross-storage transfer.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onPrimary.copy(alpha = 0.85f)
                )
                Spacer(Modifier.height(14.dp))
                Button(onClick = onCreate) {
                    Icon(Icons.Outlined.Send, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.home_create))
                }
            }
        }
    }
}

@Composable
private fun QuickSaveCard(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.home_quick_save),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = { Text(stringResource(R.string.home_quick_save_hint)) },
                    modifier = Modifier.weight(1f),
                    singleLine = false,
                    minLines = 1,
                    maxLines = 3
                )
                Spacer(Modifier.size(8.dp))
                FilledTonalIconButton(onClick = onSubmit) {
                    Icon(Icons.Outlined.Send, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun TaskTypeGrid(onPick: (String?) -> Unit) {
    val types = TaskType.entries
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(R.string.create_type),
            style = MaterialTheme.typography.titleMedium
        )
        types.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { type ->
                    TaskTypeTile(
                        modifier = Modifier.weight(1f),
                        type = type,
                        onClick = { onPick(type.wire) }
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TaskTypeTile(
    modifier: Modifier = Modifier,
    type: TaskType,
    onClick: () -> Unit
) {
    val icon: ImageVector = when (type) {
        TaskType.DIRECTLINKS -> Icons.Outlined.Link
        TaskType.YTDLP -> Icons.Outlined.Movie
        TaskType.ARIA2 -> Icons.Outlined.AddLink
        TaskType.PARSED -> Icons.Outlined.AutoAwesome
        TaskType.TGFILES -> Icons.Outlined.Forum
        TaskType.TPHPICS -> Icons.Outlined.Image
        TaskType.TRANSFER -> Icons.Outlined.CompareArrows
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(type.labelRes()),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                stringResource(type.descriptionRes()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HomeTaskCard(task: TaskInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.DownloadDone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = task.title.ifBlank { task.taskId },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(task.status.labelRes()),
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Spacer(Modifier.height(8.dp))
            GradientProgress(fraction = task.fraction())
            Spacer(Modifier.height(6.dp))
            Row {
                Text(
                    text = stringResource(R.string.tasks_progress, task.percentInt()),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f)
                )
                task.progress?.speedMbps?.let {
                    if (it > 0)
                        Text(
                            text = stringResource(R.string.tasks_speed, it),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                }
            }
        }
    }
}

@Composable
private fun EmptyServerState(onOpenSettings: () -> Unit, padding: PaddingValues) {
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.home_no_server),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onOpenSettings) {
            Text(stringResource(R.string.home_open_settings))
        }
    }
}
