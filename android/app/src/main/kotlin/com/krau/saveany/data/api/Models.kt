package com.krau.saveany.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * SaveAny-Bot HTTP API DTOs. Mirrors `api/types.go` in the Go server.
 */

@Serializable
enum class TaskType {
    @SerialName("directlinks") DIRECTLINKS,
    @SerialName("ytdlp") YTDLP,
    @SerialName("aria2") ARIA2,
    @SerialName("parseditem") PARSED,
    @SerialName("tgfiles") TGFILES,
    @SerialName("tphpics") TPHPICS,
    @SerialName("transfer") TRANSFER;

    companion object {
        fun fromWire(s: String?): TaskType? = entries.firstOrNull { it.wire == s }
    }

    val wire: String
        get() = when (this) {
            DIRECTLINKS -> "directlinks"
            YTDLP -> "ytdlp"
            ARIA2 -> "aria2"
            PARSED -> "parseditem"
            TGFILES -> "tgfiles"
            TPHPICS -> "tphpics"
            TRANSFER -> "transfer"
        }
}

@Serializable
enum class TaskStatus {
    @SerialName("queued") QUEUED,
    @SerialName("running") RUNNING,
    @SerialName("completed") COMPLETED,
    @SerialName("failed") FAILED,
    @SerialName("cancelled") CANCELLED;
}

@Serializable
data class CreateTaskRequest(
    val type: TaskType,
    val storage: String,
    val path: String = "",
    val webhook: String? = null,
    val params: JsonElement
)

@Serializable
data class CreateTaskResponse(
    @SerialName("task_id") val taskId: String,
    val type: TaskType,
    val status: TaskStatus,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class TaskProgress(
    @SerialName("total_bytes") val totalBytes: Long = 0,
    @SerialName("downloaded_bytes") val downloadedBytes: Long = 0,
    val percent: Double = 0.0,
    @SerialName("speed_mbps") val speedMbps: Double = 0.0
)

@Serializable
data class TaskInfo(
    @SerialName("task_id") val taskId: String,
    val type: TaskType,
    val status: TaskStatus,
    val title: String = "",
    val progress: TaskProgress? = null,
    val storage: String = "",
    val path: String = "",
    val error: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)

@Serializable
data class TasksList(
    val tasks: List<TaskInfo> = emptyList(),
    val total: Int = 0
)

@Serializable
data class StorageInfo(
    val name: String,
    val type: String
)

@Serializable
data class StoragesResponse(
    val storages: List<StorageInfo> = emptyList()
)

@Serializable
data class TaskTypesResponse(
    val types: List<TaskType> = emptyList()
)

@Serializable
data class HealthResponse(val status: String = "")

@Serializable
data class ApiError(
    val error: String = "",
    val message: String = ""
)

// Task params

@Serializable
data class DirectLinksParams(val urls: List<String>)

@Serializable
data class YtdlpParams(
    val urls: List<String>,
    val flags: List<String> = emptyList()
)

@Serializable
data class Aria2Params(
    val urls: List<String>,
    val options: Map<String, String> = emptyMap()
)

@Serializable
data class ParsedParams(val url: String)

@Serializable
data class TransferParams(
    @SerialName("source_storage") val sourceStorage: String,
    @SerialName("source_path") val sourcePath: String,
    @SerialName("target_storage") val targetStorage: String,
    @SerialName("target_path") val targetPath: String
)

@Serializable
data class TGFilesParams(@SerialName("message_links") val messageLinks: List<String>)

@Serializable
data class TPHPicsParams(@SerialName("telegraph_url") val telegraphUrl: String)
