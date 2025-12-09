package com.kelompok10.eeducation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Materi(
    val icon: String,
    val title: String,
    val description: String,
    val duration: String,
    val isCompleted: Boolean = false
) : Parcelable