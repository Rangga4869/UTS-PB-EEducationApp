package com.kelompok10.eeducation

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kelompok10.eeducation.data.local.Materi

class MateriActivity : AppCompatActivity() {

    private lateinit var rvMateri: RecyclerView
    private lateinit var materiAdapter: MateriAdapter
    private lateinit var btnBack: TextView
    private lateinit var viewModel: MateriViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_materi)

        // Initialize views
        initViews()

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[MateriViewModel::class.java]

        // Setup RecyclerView with adapter
        setupRecyclerView()

        // Observe LiveData from ViewModel
        observeViewModel()

        // Setup click listeners
        btnBack.setOnClickListener { finish() }
    }

    private fun initViews() {
        rvMateri = findViewById(R.id.rvMateri)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupRecyclerView() {
        materiAdapter = MateriAdapter { materi ->
            onMateriClick(materi)
        }

        rvMateri.apply {
            layoutManager = LinearLayoutManager(this@MateriActivity)
            adapter = materiAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        // Observe LiveData: when database content changes, UI updates automatically
        viewModel.allMateri.observe(this) { materiList ->
            materiList?.let { 
                materiAdapter.updateData(it)
            }
        }
    }

    private fun onMateriClick(materi: Materi) {
        // Navigate to detail page with Parcelable
        val intent = Intent(this, MateriDetailActivity::class.java).apply {
            putExtra("MATERI_DATA", materi)
        }
        startActivity(intent)
    }
}