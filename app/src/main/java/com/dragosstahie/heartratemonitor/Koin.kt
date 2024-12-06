package com.dragosstahie.heartratemonitor

import android.app.Application
import androidx.room.Room
import com.dragosstahie.heartratemonitor.data.AppDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun Application.setupKoin() {
    startKoin {
        androidContext(this@setupKoin)
        modules(appModule)
    }
}


val appModule = module {
    single<AppDatabase> {
        Room.databaseBuilder(androidApplication(), AppDatabase::class.java, "HeartRateMonitorDB")
            .build()
    }

    // DATA

    //DOMAIN
}