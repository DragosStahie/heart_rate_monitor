package com.dragosstahie.heartratemonitor.data.repository

import com.dragosstahie.heartratemonitor.data.dao.HeartRateDao
import com.dragosstahie.heartratemonitor.data.entity.HeartRateEntity

class HeartRateRepository(
    private val heartRateDao: HeartRateDao
) {

    suspend fun insert(vararg readings: Pair<Long, Int>) {
        heartRateDao.insert(*readings.map {
            HeartRateEntity(
                timeStamp = it.first,
                value = it.second
            )
        }.toTypedArray())
    }

    suspend fun deleteAll() = heartRateDao.deleteAll()

    suspend fun getAll(): List<HeartRateEntity> = heartRateDao.getAll()

}