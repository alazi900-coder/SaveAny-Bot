package com.krau.saveany.ui.screens.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.krau.saveany.data.api.TaskInfo
import com.krau.saveany.data.api.TaskStatus
import com.krau.saveany.ui.components.GradientProgress
import com.krau.saveany.ui.util.fraction
import com.krau.saveany.ui.util.labelRes
import com.krau.saveany.ui.util.percentInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    container: AppContainer,
    taskId: String,
    snackbarHostState: SnackbarHostState,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var task by remember { mutableStateOf<TaskInfo?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(taskId) {
        while (true) {
            runCatching { container.repo.getTask(taskId) }
                .onSuccess { task = it; error = null }
                .onFailure { error = it.message }
            delay(1500)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(taskId) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val t = task
            if (t == null) {
                Text(
                    error ?: "Loading…",
                    color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            t.title.ifBlank { t.taskId },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("${t.type.wire} · ${stringResource(t.status.labelRes())}")
                        Text("Storage: ${t.storage}")
                        if (t.path.isNotBlank()) Text("Path: ${t.path}")
                        Spacer(Modifier.height(12.dp))
                        GradientProgress(
                            fraction = t.fraction(),
                            indeterminate = t.status == TaskStatus.RUNNING && t.fraction() <= 0f
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.tasks_progress, t.percentInt()))
                        t.progress?.let {
                            if (it.speedMbps > 0)
                                Text(stringResource(R.string.tasks_speed, it.speedMbps))
                            if (it.totalBytes > 0)
                                Text("Bytes: ${it.downloadedBytes} / ${it.totalBytes}")
                        }
                        if (t.error.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(t.error, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                if (t.status == TaskStatus.QUEUED || t.status == TaskStatus.RUNNING) {
                    Button(onClick = {
                        scope.launch {
                            runCatching { container.repo.cancelTask(t.taskId) }
                                .onSuccess {
                                    snackbarHostState.showSnackbar("Cancelled")
                                }
                                .onFailure {
                                    snackbarHostState.showSnackbar("Error: ${it.message}")
                                }
                        }
                    }) {
                        Icon(Icons.Outlined.Cancel, contentDescription = null)
                        Spacer(Modifier.height(0.dp))
                        Text(stringResource(R.string.tasks_cancel))
                    }
                }
            }
        }
    }
}
