package com.kelompok10.eeducation

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.kelompok10.eeducation.data.local.Materi

class MateriDetailActivity : AppCompatActivity() {

    private lateinit var btnBack: TextView
    private lateinit var tvIcon: TextView
    private lateinit var tvTitle: TextView
    private lateinit var tvDuration: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvContent: TextView
    private lateinit var tvObjectives: TextView
    private lateinit var cbCompleted: CheckBox
    private lateinit var btnStartQuiz: Button
    
    private lateinit var viewModel: MateriViewModel
    private var currentMateri: Materi? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_materi_detail)

        // Initialize views
        initViews()
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[MateriViewModel::class.java]

        // Get Materi object from intent (Parcelable)
        currentMateri = intent.getParcelableExtra("MATERI_DATA")
        
        currentMateri?.let { materi ->
            displayMateriDetail(materi)
        } ?: run {
            Toast.makeText(this, "Error: Data materi tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Setup click listeners
        setupClickListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvIcon = findViewById(R.id.tvIcon)
        tvTitle = findViewById(R.id.tvTitle)
        tvDuration = findViewById(R.id.tvDuration)
        tvDescription = findViewById(R.id.tvDescription)
        tvContent = findViewById(R.id.tvContent)
        tvObjectives = findViewById(R.id.tvObjectives)
        cbCompleted = findViewById(R.id.cbCompleted)
        btnStartQuiz = findViewById(R.id.btnStartQuiz)
    }

    private fun displayMateriDetail(materi: Materi) {
        tvIcon.text = materi.icon
        tvTitle.text = materi.title
        tvDescription.text = materi.description
        tvDuration.text = "⏱️ ${materi.duration}"
        cbCompleted.isChecked = materi.isCompleted
        tvContent.text = materi.content
        tvObjectives.text = materi.learningObjectives
    }

    private fun setupClickListeners() {
        // Back button
        btnBack.setOnClickListener {
            finish()
        }

        // Checkbox completed - Update to database via ViewModel
        cbCompleted.setOnCheckedChangeListener { _, isChecked ->
            currentMateri?.let { materi ->
                // Update the completion status
                val updatedMateri = materi.copy(isCompleted = isChecked)
                
                // Update in database via ViewModel
                viewModel.updateMateri(updatedMateri)
                
                // Update current materi reference
                currentMateri = updatedMateri
                
                if (isChecked) {
                    Toast.makeText(this, "Materi ditandai sebagai selesai ✓", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Materi ditandai belum selesai", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Start quiz button
        btnStartQuiz.setOnClickListener {
            Toast.makeText(
                this,
                "Fitur kuis akan segera hadir!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
