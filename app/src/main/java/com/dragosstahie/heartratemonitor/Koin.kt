package com.dragosstahie.heartratemonitor

import android.app.Application
import androidx.room.Room
import com.dragosstahie.heartratemonitor.data.AppDatabase
import com.dragosstahie.heartratemonitor.data.dao.HeartRateDao
import com.dragosstahie.heartratemonitor.data.repository.HeartRateRepository
import com.dragosstahie.heartratemonitor.ui.common.BLEClientViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
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

    viewModel { BLEClientViewModel(get(), get()) }


    // DATA
    factory<HeartRateDao> { get<AppDatabase>().heartRateDao() }
    single<HeartRateRepository> { HeartRateRepository(get()) }

    //DOMAIN
}