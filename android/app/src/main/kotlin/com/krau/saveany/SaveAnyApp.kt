package com.krau.saveany

import android.app.Application
import com.krau.saveany.data.AppContainer

class SaveAnyApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
