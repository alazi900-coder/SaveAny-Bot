package com.krau.saveany.data

import android.content.Context
import com.krau.saveany.data.prefs.SettingsRepository
import com.krau.saveany.data.repo.SaveAnyRepository

/** Tiny manual DI container — keeps things simple without dragging in Hilt. */
class AppContainer(context: Context) {
    val settings: SettingsRepository = SettingsRepository(context.applicationContext)
    val repo: SaveAnyRepository = SaveAnyRepository(settings)
}
