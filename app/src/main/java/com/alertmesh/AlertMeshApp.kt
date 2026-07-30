package com.alertmesh

import android.app.Application
import com.google.crypto.tink.config.TinkConfig
import com.alertmesh.di.AppContainer

class AlertMeshApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        TinkConfig.register()
        container = AppContainer(this)
    }
}
