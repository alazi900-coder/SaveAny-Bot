package com.krau.saveany.ui.screens.tasks

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.krau.saveany.R
import com.krau.saveany.data.AppContainer
import com.krau.saveany.data.api.TaskInfo
import com.krau.saveany.data.api.TaskStatus
import com.krau.saveany.ui.components.GradientProgress
import com.krau.saveany.ui.components.StatusDot
import com.krau.saveany.ui.util.fraction
import com.krau.saveany.ui.util.labelRes
import com.krau.saveany.ui.util.percentInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    container: AppContainer,
    snackbarHostState: SnackbarHostState,
    onOpenTask: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val activeServer by container.repo.activeServer.collectAsState(initial = null)

    var tasks by remember { mutableStateOf<List<TaskInfo>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    suspend fun reload() {
        runCatching { container.repo.listTasks() }
            .onSuccess { tasks = it; error = null }
            .onFailure { error = it.message }
    }

    LaunchedEffect(activeServer?.id) {
        while (activeServer != null) {
            reload()
            delay(2000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_tasks)) },
                actions = {
                    IconButton(onClick = { scope.launch { reload() } }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = stringResource(R.string.tasks_refresh))
                    }
                }
            )
        }
    ) { padding ->
        if (tasks.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.tasks_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.snack_error, it), color = MaterialTheme.colorScheme.error)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tasks, key = { it.taskId }) { task ->
                    TaskRow(task = task, onClick = { onOpenTask(task.taskId) })
                }
            }
        }
    }
}

@Composable
private fun TaskRow(task: TaskInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusDot(color = task.status.dotColor(), modifier = Modifier.size(10.dp))
                Spacer(Modifier.size(8.dp))
                Text(
                    text = task.title.ifBlank { task.taskId },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(task.status.labelRes()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${task.type.wire} · ${task.storage}${if (task.path.isNotBlank()) "/${task.path}" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            GradientProgress(
                fraction = task.fraction(),
                indeterminate = task.status == TaskStatus.RUNNING && task.fraction() <= 0f
            )
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
            if (task.error.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = task.error,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun TaskStatus.dotColor(): Color = when (this) {
    TaskStatus.QUEUED -> Color(0xFF8FA0BA)
    TaskStatus.RUNNING -> Color(0xFF3F8AE0)
    TaskStatus.COMPLETED -> Color(0xFF2EA56E)
    TaskStatus.FAILED -> Color(0xFFD25555)
    TaskStatus.CANCELLED -> Color(0xFFB0884F)
}
