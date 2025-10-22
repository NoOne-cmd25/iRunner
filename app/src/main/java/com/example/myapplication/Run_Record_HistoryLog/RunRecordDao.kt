package com.example.myapplication

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RunRecordDao {
    @Insert
    fun insert(record: RunRecordEntity)

    @Query("SELECT * FROM run_records ORDER BY startTime DESC")
    fun getAllRecords(): List<RunRecordEntity>

    @Query("SELECT * FROM run_records WHERE recordId = :id")
    fun getRecordById(id: String): RunRecordEntity?
}