package com.krau.saveany.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.krau.saveany.data.api.ApiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

@Serializable
data class ServerEntry(
    val id: String,
    val name: String,
    val baseUrl: String,
    val token: String
)

@Serializable
data class SettingsState(
    val servers: List<ServerEntry> = emptyList(),
    val activeServerId: String? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true
) {
    val activeServer: ServerEntry?
        get() = servers.firstOrNull { it.id == activeServerId } ?: servers.firstOrNull()
}

@Serializable
enum class ThemeMode { SYSTEM, LIGHT, DARK }

private val Context.settingsDataStore by preferencesDataStore(name = "saveany_settings")

class SettingsRepository(private val context: Context) {

    private val key = stringPreferencesKey("state_v1")

    val flow: Flow<SettingsState> = context.settingsDataStore.data.map { prefs ->
        prefs[key]?.let { runCatching { ApiClient.json.decodeFromString<SettingsState>(it) }.getOrNull() }
            ?: SettingsState()
    }

    suspend fun update(transform: (SettingsState) -> SettingsState) {
        context.settingsDataStore.edit { prefs ->
            val current = prefs[key]?.let {
                runCatching { ApiClient.json.decodeFromString<SettingsState>(it) }.getOrNull()
            } ?: SettingsState()
            val next = transform(current)
            prefs[key] = ApiClient.json.encodeToString(SettingsState.serializer(), next)
        }
    }
}
