package com.dragosstahie.heartratemonitor.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dragosstahie.heartratemonitor.data.entity.HeartRateEntity

@Dao
interface HeartRateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg device: HeartRateEntity)

    @Query("DELETE FROM HEART_RATE")
    suspend fun deleteAll()

    @Query("SELECT * FROM HEART_RATE")
    suspend fun getAll(): List<HeartRateEntity>
}