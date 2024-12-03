package com.dragosstahie.heartratemonitor

import android.app.Application

class HeartRateMonitorApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        setupKoin()
    }
}