package com.kelompok10.eeducation.data.repository

import androidx.lifecycle.LiveData
import com.kelompok10.eeducation.data.local.Materi
import com.kelompok10.eeducation.data.local.MateriDao

class MateriRepository(private val materiDao: MateriDao) {
    
    val allMateri: LiveData<List<Materi>> = materiDao.getAllMateri()
    
    suspend fun insertAll(materiList: List<Materi>) {
        materiDao.insertAll(materiList)
    }
    
    suspend fun update(materi: Materi) {
        materiDao.updateMateri(materi)
    }
    
    suspend fun getMateriById(id: Int): Materi? {
        return materiDao.getMateriById(id)
    }
}
