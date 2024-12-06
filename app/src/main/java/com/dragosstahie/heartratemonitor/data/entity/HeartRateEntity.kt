package com.dragosstahie.heartratemonitor.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "HEART_RATE")
data class HeartRateEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    val id: Int = 0,

    @ColumnInfo(name = "VALUE")
    val value: Int,

    @ColumnInfo(name = "TIME_STAMP")
    val timeStamp: Long,
)