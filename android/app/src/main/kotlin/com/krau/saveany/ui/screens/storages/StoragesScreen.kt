package com.krau.saveany.ui.screens.storages

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
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.krau.saveany.R
import com.krau.saveany.data.AppContainer
import com.krau.saveany.data.api.StorageInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoragesScreen(
    container: AppContainer,
    snackbarHostState: SnackbarHostState
) {
    val activeServer by container.repo.activeServer.collectAsState(initial = null)
    var storages by remember { mutableStateOf<List<StorageInfo>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(activeServer?.id) {
        runCatching { container.repo.storages() }
            .onSuccess { storages = it; error = null }
            .onFailure { error = it.message }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_storages)) }) }
    ) { padding ->
        if (storages.isEmpty()) {
            Column(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.storages_empty),
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
                items(storages, key = { it.name }) { StorageCard(it) }
            }
        }
    }
}

@Composable
private fun StorageCard(storage: StorageInfo) {
    val (icon, labelRes) = storageIconLabel(storage.type)
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    storage.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    stringResource(labelRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun storageIconLabel(type: String): Pair<ImageVector, Int> = when (type.lowercase()) {
    "local" -> Icons.Outlined.Folder to R.string.storage_type_local
    "alist" -> Icons.Outlined.Hub to R.string.storage_type_alist
    "webdav" -> Icons.Outlined.CloudQueue to R.string.storage_type_webdav
    "s3" -> Icons.Outlined.Cloud to R.string.storage_type_s3
    "minio" -> Icons.Outlined.Storage to R.string.storage_type_minio
    "telegram" -> Icons.Outlined.Forum to R.string.storage_type_telegram
    "rclone" -> Icons.Outlined.Cloud to R.string.storage_type_rclone
    else -> Icons.Outlined.Cloud to R.string.storage_type_local
}
