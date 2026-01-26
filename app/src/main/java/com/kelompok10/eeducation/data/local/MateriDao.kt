package com.kelompok10.eeducation.data.local

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MateriDao {
    @Query("SELECT * FROM materi_table ORDER BY id ASC")
    fun getAllMateri(): LiveData<List<Materi>>

    @Query("SELECT * FROM materi_table WHERE id = :id")
    suspend fun getMateriById(id: Int): Materi?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(materiList: List<Materi>)

    @Query("DELETE FROM materi_table")
    suspend fun deleteAll()

    @Update
    suspend fun updateMateri(materi: Materi)
}