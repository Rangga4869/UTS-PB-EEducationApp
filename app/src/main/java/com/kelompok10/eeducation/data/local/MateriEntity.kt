package com.kelompok10.eeducation.data.local

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "materi_table")
data class Materi(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val icon: String,
    val title: String,
    val description: String,
    val duration: String,
    val content: String = "",
    var isCompleted: Boolean = false
) : Parcelable