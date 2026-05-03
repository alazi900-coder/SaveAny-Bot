package com.krau.saveany.data.repo

import com.krau.saveany.data.api.ApiClient
import com.krau.saveany.data.api.CreateTaskRequest
import com.krau.saveany.data.api.CreateTaskResponse
import com.krau.saveany.data.api.SaveAnyApi
import com.krau.saveany.data.api.StorageInfo
import com.krau.saveany.data.api.TaskInfo
import com.krau.saveany.data.prefs.ServerEntry
import com.krau.saveany.data.prefs.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Thin facade in front of the active server's [SaveAnyApi].
 * Resolves the active server from [SettingsRepository] for every call so the user
 * can switch servers without the app needing to rebuild anything.
 */
class SaveAnyRepository(private val settings: SettingsRepository) {

    val activeServer: Flow<ServerEntry?> = settings.flow.map { it.activeServer }

    private suspend fun api(): SaveAnyApi {
        val server = settings.flow.first().activeServer
            ?: throw NoActiveServerException()
        return ApiClient.build(server.baseUrl, server.token)
    }

    suspend fun health() = api().health()

    suspend fun storages(): List<StorageInfo> = api().listStorages().storages

    suspend fun listTasks(): List<TaskInfo> = api().listTasks().tasks

    suspend fun getTask(id: String): TaskInfo = api().getTask(id)

    suspend fun createTask(req: CreateTaskRequest): CreateTaskResponse = api().createTask(req)

    suspend fun cancelTask(id: String) = api().cancelTask(id)

    /** Health check against an arbitrary server (used by the "test connection" button). */
    suspend fun healthFor(baseUrl: String, token: String) =
        ApiClient.build(baseUrl, token).health()
}

class NoActiveServerException : Exception("No active SaveAny-Bot server is configured")
