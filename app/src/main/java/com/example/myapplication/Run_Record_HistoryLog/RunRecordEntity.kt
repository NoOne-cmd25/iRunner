package com.example.myapplication

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "run_records")
data class RunRecordEntity(
    @PrimaryKey val recordId: String,
    val userId: String,
    val startTime: Long,
    val duration: Long,
    val totalDistance: Double,
    val averagePace: Double,
    val trackPoints: String
)