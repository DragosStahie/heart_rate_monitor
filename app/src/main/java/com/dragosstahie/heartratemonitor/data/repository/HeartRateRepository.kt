package com.dragosstahie.heartratemonitor.data.repository

import com.dragosstahie.heartratemonitor.data.dao.HeartRateDao
import com.dragosstahie.heartratemonitor.data.entity.HeartRateEntity
import java.sql.Date

class HeartRateRepository(
    private val heartRateDao: HeartRateDao
) {

    suspend fun insert(reading: Int, timeStamp: Long) {
        heartRateDao.insert(HeartRateEntity(value = reading, timeStamp = timeStamp))
    }

    suspend fun deleteAll() = heartRateDao.deleteAll()

    suspend fun getAll(): List<HeartRateEntity> = heartRateDao.getAll()

}