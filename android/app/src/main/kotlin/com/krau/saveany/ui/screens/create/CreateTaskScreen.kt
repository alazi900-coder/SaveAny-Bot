package com.krau.saveany.ui.screens.create

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.krau.saveany.R
import com.krau.saveany.data.AppContainer
import com.krau.saveany.data.api.Aria2Params
import com.krau.saveany.data.api.CreateTaskRequest
import com.krau.saveany.data.api.DirectLinksParams
import com.krau.saveany.data.api.ParsedParams
import com.krau.saveany.data.api.StorageInfo
import com.krau.saveany.data.api.TGFilesParams
import com.krau.saveany.data.api.TPHPicsParams
import com.krau.saveany.data.api.TaskType
import com.krau.saveany.data.api.TransferParams
import com.krau.saveany.data.api.YtdlpParams
import com.krau.saveany.ui.util.descriptionRes
import com.krau.saveany.ui.util.labelRes
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    container: AppContainer,
    initialType: String?,
    initialUrl: String,
    snackbarHostState: SnackbarHostState,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val activeServer by container.repo.activeServer.collectAsState(initial = null)

    var type by remember { mutableStateOf(TaskType.fromWire(initialType) ?: TaskType.DIRECTLINKS) }
    var storages by remember { mutableStateOf<List<StorageInfo>>(emptyList()) }
    var storageName by remember { mutableStateOf<String?>(null) }
    var path by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var submitting by remember { mutableStateOf(false) }

    // Per-type fields
    var urls by remember { mutableStateOf(initialUrl) }
    var ytFlags by remember { mutableStateOf("") }
    var aria2Options by remember { mutableStateOf("") }
    var messageLinks by remember { mutableStateOf(initialUrl) }
    var telegraphUrl by remember { mutableStateOf(if (initialUrl.contains("telegra.ph")) initialUrl else "") }
    var parsedUrl by remember { mutableStateOf(initialUrl) }
    var sourceStorage by remember { mutableStateOf<String?>(null) }
    var sourcePath by remember { mutableStateOf("") }
    var targetStorage by remember { mutableStateOf<String?>(null) }
    var targetPath by remember { mutableStateOf("") }

    LaunchedEffect(activeServer?.id) {
        runCatching { container.repo.storages() }
            .onSuccess {
                storages = it
                storageName = it.firstOrNull()?.name
                sourceStorage = storageName
                targetStorage = it.getOrNull(1)?.name ?: storageName
                error = null
            }
            .onFailure { error = it.message }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_title)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { TaskTypeSelector(selected = type, onSelect = { type = it }) }

            if (type != TaskType.TRANSFER) {
                item {
                    StoragePicker(
                        label = stringResource(R.string.create_storage),
                        storages = storages,
                        selected = storageName,
                        onSelect = { storageName = it }
                    )
                }
                item {
                    OutlinedTextField(
                        value = path,
                        onValueChange = { path = it },
                        label = { Text(stringResource(R.string.create_path)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            when (type) {
                TaskType.DIRECTLINKS, TaskType.YTDLP, TaskType.ARIA2 -> {
                    item {
                        OutlinedTextField(
                            value = urls,
                            onValueChange = { urls = it },
                            label = { Text(stringResource(R.string.create_urls)) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                    if (type == TaskType.YTDLP) {
                        item {
                            OutlinedTextField(
                                value = ytFlags,
                                onValueChange = { ytFlags = it },
                                label = { Text(stringResource(R.string.create_yt_flags)) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                            )
                        }
                    }
                    if (type == TaskType.ARIA2) {
                        item {
                            OutlinedTextField(
                                value = aria2Options,
                                onValueChange = { aria2Options = it },
                                label = { Text(stringResource(R.string.create_aria2_options)) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                            )
                        }
                    }
                }
                TaskType.TGFILES -> item {
                    OutlinedTextField(
                        value = messageLinks,
                        onValueChange = { messageLinks = it },
                        label = { Text(stringResource(R.string.create_message_links)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
                TaskType.TPHPICS -> item {
                    OutlinedTextField(
                        value = telegraphUrl,
                        onValueChange = { telegraphUrl = it },
                        label = { Text(stringResource(R.string.create_telegraph_url)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                TaskType.PARSED -> item {
                    OutlinedTextField(
                        value = parsedUrl,
                        onValueChange = { parsedUrl = it },
                        label = { Text(stringResource(R.string.create_parsed_url)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                TaskType.TRANSFER -> {
                    item {
                        StoragePicker(
                            label = stringResource(R.string.create_source_storage),
                            storages = storages,
                            selected = sourceStorage,
                            onSelect = { sourceStorage = it }
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = sourcePath,
                            onValueChange = { sourcePath = it },
                            label = { Text(stringResource(R.string.create_source_path)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        StoragePicker(
                            label = stringResource(R.string.create_target_storage),
                            storages = storages,
                            selected = targetStorage,
                            onSelect = { targetStorage = it }
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = targetPath,
                            onValueChange = { targetPath = it },
                            label = { Text(stringResource(R.string.create_target_path)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(4.dp))
                Button(
                    enabled = !submitting,
                    onClick = {
                        scope.launch {
                            submitting = true
                            error = null
                            val result = runCatching {
                                val req = buildRequest(
                                    type = type,
                                    storage = storageName,
                                    path = path,
                                    urls = urls,
                                    ytFlags = ytFlags,
                                    aria2Options = aria2Options,
                                    messageLinks = messageLinks,
                                    telegraphUrl = telegraphUrl,
                                    parsedUrl = parsedUrl,
                                    sourceStorage = sourceStorage,
                                    sourcePath = sourcePath,
                                    targetStorage = targetStorage,
                                    targetPath = targetPath
                                )
                                container.repo.createTask(req)
                            }
                            submitting = false
                            result.onSuccess {
                                snackbarHostState.showSnackbar("Task created: ${it.taskId}")
                                onClose()
                            }.onFailure {
                                error = it.message
                                snackbarHostState.showSnackbar("Error: ${it.message}")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.create_submit)) }

                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

private fun buildRequest(
    type: TaskType,
    storage: String?,
    path: String,
    urls: String,
    ytFlags: String,
    aria2Options: String,
    messageLinks: String,
    telegraphUrl: String,
    parsedUrl: String,
    sourceStorage: String?,
    sourcePath: String,
    targetStorage: String?,
    targetPath: String
): CreateTaskRequest {
    fun lines(s: String) = s.split('\n', '\r', ',').map { it.trim() }.filter { it.isNotEmpty() }

    val (storageName, params) = when (type) {
        TaskType.DIRECTLINKS -> {
            val list = lines(urls)
            require(list.isNotEmpty()) { "Provide at least one URL" }
            require(!storage.isNullOrBlank()) { "Pick a storage" }
            storage to Json.encodeToJsonElement(DirectLinksParams.serializer(), DirectLinksParams(list))
        }
        TaskType.YTDLP -> {
            val list = lines(urls)
            require(list.isNotEmpty()) { "Provide at least one URL" }
            require(!storage.isNullOrBlank()) { "Pick a storage" }
            storage to Json.encodeToJsonElement(YtdlpParams.serializer(), YtdlpParams(list, lines(ytFlags)))
        }
        TaskType.ARIA2 -> {
            val list = lines(urls)
            require(list.isNotEmpty()) { "Provide at least one URL" }
            require(!storage.isNullOrBlank()) { "Pick a storage" }
            val options = lines(aria2Options).mapNotNull {
                val idx = it.indexOf('=')
                if (idx < 0) null else it.substring(0, idx).trim() to it.substring(idx + 1).trim()
            }.toMap()
            storage to Json.encodeToJsonElement(Aria2Params.serializer(), Aria2Params(list, options))
        }
        TaskType.TGFILES -> {
            val list = lines(messageLinks)
            require(list.isNotEmpty()) { "Provide at least one Telegram message link" }
            require(!storage.isNullOrBlank()) { "Pick a storage" }
            storage to Json.encodeToJsonElement(TGFilesParams.serializer(), TGFilesParams(list))
        }
        TaskType.TPHPICS -> {
            require(telegraphUrl.isNotBlank()) { "Provide the Telegraph URL" }
            require(!storage.isNullOrBlank()) { "Pick a storage" }
            storage to Json.encodeToJsonElement(TPHPicsParams.serializer(), TPHPicsParams(telegraphUrl))
        }
        TaskType.PARSED -> {
            require(parsedUrl.isNotBlank()) { "Provide the URL to parse" }
            require(!storage.isNullOrBlank()) { "Pick a storage" }
            storage to Json.encodeToJsonElement(ParsedParams.serializer(), ParsedParams(parsedUrl))
        }
        TaskType.TRANSFER -> {
            require(!sourceStorage.isNullOrBlank() && !targetStorage.isNullOrBlank()) { "Pick source and target storages" }
            require(sourcePath.isNotBlank() && targetPath.isNotBlank()) { "Provide source and target paths" }
            // For transfer, the top-level storage field is the target.
            targetStorage to Json.encodeToJsonElement(
                TransferParams.serializer(),
                TransferParams(
                    sourceStorage = sourceStorage,
                    sourcePath = sourcePath,
                    targetStorage = targetStorage,
                    targetPath = targetPath
                )
            )
        }
    }

    return CreateTaskRequest(
        type = type,
        storage = storageName,
        path = path,
        params = params as JsonElement
    )
}

@Composable
private fun TaskTypeSelector(selected: TaskType, onSelect: (TaskType) -> Unit) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(
                stringResource(R.string.create_type),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {}
            TaskType.entries.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { t ->
                        FilterChip(
                            selected = t == selected,
                            onClick = { onSelect(t) },
                            label = { Text(stringResource(t.labelRes())) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(6.dp))
            }
            Text(
                stringResource(selected.descriptionRes()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StoragePicker(
    label: String,
    storages: List<StorageInfo>,
    selected: String?,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { expanded = true }) {
                Text(selected ?: "—")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                storages.forEach { s ->
                    DropdownMenuItem(
                        text = { Text("${s.name} (${s.type})") },
                        onClick = {
                            onSelect(s.name)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
