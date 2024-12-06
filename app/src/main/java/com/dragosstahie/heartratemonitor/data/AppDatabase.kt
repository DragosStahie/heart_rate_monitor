package com.dragosstahie.heartratemonitor.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dragosstahie.heartratemonitor.data.dao.HeartRateDao
import com.dragosstahie.heartratemonitor.data.entity.HeartRateEntity

@Database(
    entities = [HeartRateEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun heartRateDao(): HeartRateDao
}