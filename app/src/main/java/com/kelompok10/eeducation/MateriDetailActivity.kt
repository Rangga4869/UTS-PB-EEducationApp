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

        // Set content from database or use detailed content method
        if (materi.content.isNotEmpty()) {
            tvContent.text = materi.content
            setLearningObjectives(materi.title)
        } else {
            // Fallback to detailed content method
            setDetailedContent(materi.title)
        }
    }

    private fun setLearningObjectives(title: String) {
        // Set learning objectives
        val objectives = when (title) {
            "Pengenalan Pemrograman" -> """
                • Memahami konsep dasar pemrograman
                • Mengenal algoritma dan flowchart
                • Memahami logika pemrograman
                • Mengenal berbagai bahasa pemrograman
            """.trimIndent()
            
            "Variabel dan Tipe Data" -> """
                • Memahami konsep variabel
                • Mengenal berbagai tipe data
                • Memahami cara deklarasi variabel
                • Memahami konstanta dan penggunaannya
            """.trimIndent()
            
            "Struktur Kontrol" -> """
                • Memahami percabangan (if-else, switch)
                • Mengenal perulangan (for, while, do-while)
                • Memahami nested control structure
                • Menggunakan break dan continue
            """.trimIndent()
            
            else -> """
                • Memahami konsep dasar materi
                • Menerapkan teori ke praktik
                • Mengerjakan latihan soal
                • Memahami studi kasus
            """.trimIndent()
        }
        tvObjectives.text = objectives
    }

    private fun setDetailedContent(title: String) {
        // Set learning objectives
        setLearningObjectives(title)

        // Set main content
        val content = when (title) {
            "Pengenalan Pemrograman" -> """
                Pemrograman adalah proses menulis, menguji, dan memelihara kode yang membuat program komputer. Pemrograman memungkinkan komputer untuk melakukan tugas-tugas tertentu sesuai dengan instruksi yang diberikan.
                
                Konsep Dasar:
                
                1. Algoritma
                   Algoritma adalah langkah-langkah sistematis untuk menyelesaikan masalah. Sebelum menulis kode, penting untuk merancang algoritma yang efisien.
                
                2. Flowchart
                   Flowchart adalah diagram yang menggambarkan alur proses atau algoritma menggunakan simbol-simbol standar.
                
                3. Pseudocode
                   Pseudocode adalah cara menulis algoritma menggunakan bahasa yang mirip dengan bahasa pemrograman tetapi lebih mudah dipahami manusia.
                
                4. Debugging
                   Debugging adalah proses menemukan dan memperbaiki kesalahan (bug) dalam program.
                
                Bahasa Pemrograman Populer:
                • Python - Mudah dipelajari, cocok untuk pemula
                • Java - Platform independent, banyak digunakan
                • JavaScript - Bahasa web, frontend dan backend
                • C++ - High performance, system programming
                • Kotlin - Modern, untuk Android development
            """.trimIndent()
            
            "Variabel dan Tipe Data" -> """
                Variabel adalah tempat penyimpanan data dalam program yang nilainya dapat berubah selama program berjalan.
                
                Tipe Data Dasar:
                
                1. Integer (Bilangan Bulat)
                   - Menyimpan bilangan bulat positif atau negatif
                   - Contoh: 10, -5, 0, 1000
                
                2. Float/Double (Bilangan Desimal)
                   - Menyimpan bilangan dengan desimal
                   - Contoh: 3.14, -0.5, 2.71828
                
                3. String (Teks)
                   - Menyimpan rangkaian karakter
                   - Contoh: "Hello", "Pemrograman", "123"
                
                4. Boolean (Nilai Logika)
                   - Menyimpan nilai true atau false
                   - Digunakan untuk kondisi dan logika
                
                5. Char (Karakter)
                   - Menyimpan satu karakter
                   - Contoh: 'A', 'z', '1', '@'
                
                Aturan Penamaan Variabel:
                • Harus dimulai dengan huruf atau underscore
                • Tidak boleh menggunakan spasi
                • Case sensitive (huruf besar/kecil berbeda)
                • Gunakan nama yang deskriptif
            """.trimIndent()
            
            "Struktur Kontrol" -> """
                Struktur kontrol mengatur alur eksekusi program berdasarkan kondisi tertentu.
                
                1. Percabangan (Branching)
                
                If Statement:
                Menjalankan blok kode jika kondisi benar.
                
                If-Else Statement:
                Memberikan alternatif jika kondisi salah.
                
                If-Else If-Else:
                Menguji beberapa kondisi secara berurutan.
                
                Switch Statement:
                Memilih salah satu dari banyak blok kode untuk dieksekusi.
                
                2. Perulangan (Looping)
                
                For Loop:
                Digunakan ketika jumlah iterasi sudah diketahui.
                
                While Loop:
                Mengulang selama kondisi bernilai true.
                
                Do-While Loop:
                Mirip while, tapi minimal dieksekusi sekali.
                
                3. Kontrol Perulangan
                
                Break:
                Menghentikan perulangan lebih awal.
                
                Continue:
                Melompati iterasi saat ini dan lanjut ke iterasi berikutnya.
                
                Return:
                Mengembalikan nilai dan keluar dari fungsi.
            """.trimIndent()
            
            else -> """
                Materi ini akan membahas konsep-konsep penting dalam $title yang perlu Anda pahami untuk mengembangkan kemampuan pemrograman.
                
                Dalam materi ini, Anda akan mempelajari:
                • Konsep dasar dan teori
                • Implementasi praktis
                • Best practices
                • Common pitfalls dan cara menghindarinya
                • Studi kasus nyata
                
                Pastikan Anda memahami setiap bagian sebelum melanjutkan ke materi berikutnya. Jangan ragu untuk membaca ulang jika ada yang kurang jelas.
                
                Setelah menyelesaikan materi ini, Anda dapat mengerjakan kuis untuk menguji pemahaman Anda.
            """.trimIndent()
        }
        tvContent.text = content
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
