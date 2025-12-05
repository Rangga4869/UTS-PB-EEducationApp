package com.kelompok10.eeducation

data class Materi(
    val icon: String,
    val title: String,
    val description: String,
    val duration: String,
    val isCompleted: Boolean = false
)