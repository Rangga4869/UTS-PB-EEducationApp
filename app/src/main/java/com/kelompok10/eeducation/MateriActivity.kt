package com.kelompok10.eeducation

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MateriActivity : AppCompatActivity() {

    private lateinit var rvMateri: RecyclerView
    private lateinit var materiAdapter: MateriAdapter
    private lateinit var btnBack: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_materi)

        // Inisialisasi views
        initViews()

        // Setup RecyclerView
        setupRecyclerView()

        // Setup back button
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun initViews() {
        rvMateri = findViewById(R.id.rvMateri)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupRecyclerView() {
        // Membuat data dummy materi
        val materiList = listOf(
            Materi(
                "📚",
                "Pengenalan Pemrograman",
                "Dasar-dasar pemrograman dan algoritma",
                "15 menit",
                true
            ),
            Materi(
                "💻",
                "Variabel dan Tipe Data",
                "Memahami variabel, tipe data, dan konstanta",
                "20 menit",
                true
            ),
            Materi(
                "🔄",
                "Struktur Kontrol",
                "If-else, switch, dan perulangan",
                "25 menit",
                true
            ),
            Materi(
                "📦",
                "Array dan Collections",
                "Belajar array, list, dan struktur data",
                "30 menit",
                false
            ),
            Materi(
                "⚙️",
                "Fungsi dan Method",
                "Membuat dan menggunakan fungsi",
                "20 menit",
                false
            ),
            Materi(
                "🎯",
                "Object Oriented Programming",
                "Konsep OOP: Class, Object, Inheritance",
                "35 menit",
                false
            ),
            Materi(
                "📱",
                "Android Components",
                "Activity, Intent, dan Fragment",
                "30 menit",
                false
            ),
            Materi(
                "🎨",
                "User Interface Design",
                "Layout, Views, dan Material Design",
                "25 menit",
                false
            ),
            Materi(
                "💾",
                "Data Storage",
                "SharedPreferences, SQLite, dan Room",
                "30 menit",
                false
            ),
            Materi(
                "🌐",
                "Networking & API",
                "REST API, Retrofit, dan JSON",
                "40 menit",
                false
            )
        )

        // Setup adapter
        materiAdapter = MateriAdapter(materiList) { materi ->
            onMateriClick(materi)
        }

        // Setup RecyclerView
        rvMateri.apply {
            layoutManager = LinearLayoutManager(this@MateriActivity)
            adapter = materiAdapter
            setHasFixedSize(true)
        }
    }

    private fun onMateriClick(materi: Materi) {
        // Navigate to detail page
        val intent = Intent(this, MateriDetailActivity::class.java).apply {
            putExtra("MATERI_ICON", materi.icon)
            putExtra("MATERI_TITLE", materi.title)
            putExtra("MATERI_DESCRIPTION", materi.description)
            putExtra("MATERI_DURATION", materi.duration)
            putExtra("MATERI_IS_COMPLETED", materi.isCompleted)
        }
        startActivity(intent)
    }
}