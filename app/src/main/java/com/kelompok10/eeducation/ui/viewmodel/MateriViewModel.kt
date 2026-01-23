package com.kelompok10.eeducation.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.kelompok10.eeducation.data.local.AppDatabase
import com.kelompok10.eeducation.data.local.Materi
import com.kelompok10.eeducation.data.repository.MateriRepository
import kotlinx.coroutines.launch

class MateriViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MateriRepository
    val allMateri: LiveData<List<Materi>>

    init {
        val dao = AppDatabase.Companion.getDatabase(application).materiDao()
        repository = MateriRepository(dao)
        allMateri = repository.allMateri
    }

    fun updateMateri(materi: Materi) = viewModelScope.launch {
        repository.update(materi)
    }

    fun insertAllMateri(materiList: List<Materi>) = viewModelScope.launch {
        repository.insertAll(materiList)
    }

    suspend fun getMateriById(id: Int): Materi? {
        return repository.getMateriById(id)
    }
}