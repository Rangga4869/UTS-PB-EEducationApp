package com.kelompok10.eeducation.ui.main

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kelompok10.eeducation.ui.materi.MateriActivity
import com.kelompok10.eeducation.R
import com.kelompok10.eeducation.ui.news.NewsActivity
import com.kelompok10.eeducation.ui.settings.SettingsActivity
import com.kelompok10.eeducation.utils.DownloadPdfTask
import com.kelompok10.eeducation.utils.NetworkUtils
import com.kelompok10.eeducation.utils.SettingsManager
import java.io.File

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var cardMateri: CardView
    private lateinit var cardQuiz: CardView
    private lateinit var cardVideo: CardView
    private lateinit var cardProfile: CardView
    private lateinit var cardNews: CardView
    private lateinit var cardDownload: CardView
    private lateinit var cardSettings: CardView
    
    private var progressDialog: ProgressDialog? = null
    private lateinit var settingsManager: SettingsManager

    companion object {
        private const val TAG = "MainActivity"
        private const val KURIKULUM_URL = "https://github.com/Rangga4869/UTS-PB-EEducationApp/raw/refs/heads/dev-faisal/assets/materi/KURIKULUM.pdf"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Activity started")
        
        // Initialize SettingsManager and apply theme
        settingsManager = SettingsManager(this)
        settingsManager.applyTheme()
        
        setContentView(R.layout.activity_main)

        // Inisialisasi views
        initViews()

        // Setup click listeners
        setupClickListeners()

        // Show welcome message
        showWelcomeMessage()
    }

    private fun initViews() {
        cardMateri = findViewById(R.id.cardMateri)
        cardQuiz = findViewById(R.id.cardQuiz)
        cardVideo = findViewById(R.id.cardVideo)
        cardProfile = findViewById(R.id.cardProfile)
        cardNews = findViewById(R.id.cardNews)
        cardDownload = findViewById(R.id.cardDownload)
        cardSettings = findViewById(R.id.cardSettings)
    }

    private fun setupClickListeners() {
        // Card Materi - Navigate to MateriActivity
        cardMateri.setOnClickListener {
            Log.d(TAG, "Card Materi clicked")
            val intent = Intent(this, MateriActivity::class.java)
            startActivity(intent)
        }

        // Card Quiz - Show coming soon dialog
        cardQuiz.setOnClickListener {
            Log.d(TAG, "Card Quiz clicked")
            showComingSoonDialog("Quiz & Latihan")
        }

        // Card Video - Show coming soon dialog
        cardVideo.setOnClickListener {
            Log.d(TAG, "Card Video clicked")
            showComingSoonDialog("Video Tutorial")
        }

        // Card Profile - Show profile info
        cardProfile.setOnClickListener {
            Log.d(TAG, "Card Profile clicked")
            showProfileDialog()
        }

        // Card News - Navigate to NewsActivity
        cardNews.setOnClickListener {
            Log.d(TAG, "Card News clicked")
            if (NetworkUtils.isNetworkAvailable(this)) {
                val intent = Intent(this, NewsActivity::class.java)
                startActivity(intent)
            } else {
                showNetworkError()
            }
        }

        // Card Download - Download Kurikulum PDF
        cardDownload.setOnClickListener {
            Log.d(TAG, "Card Download clicked")
            if (NetworkUtils.isNetworkAvailable(this)) {
                startPdfDownload()
            } else {
                showNetworkError()
            }
        }

        // Card Settings - Navigate to SettingsActivity
        cardSettings.setOnClickListener {
            Log.d(TAG, "Card Settings clicked")
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showWelcomeMessage() {
        Log.d(TAG, "Showing welcome message")
        val userName = settingsManager.getUserName()
        Toast.makeText(
            this,
            "Selamat belajar, $userName!",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showComingSoonDialog(feature: String) {
        Log.d(TAG, "Showing coming soon dialog for: $feature")
        MaterialAlertDialogBuilder(this)
            .setTitle("Coming Soon")
            .setMessage("Fitur $feature akan segera hadir!")
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton("OK") { dialog, _ ->
                Log.d(TAG, "Dialog dismissed")
                dialog.dismiss()
            }
            .show()
    }

    private fun showProfileDialog() {
        Log.d(TAG, "Showing profile dialog")
        val userName = settingsManager.getUserName()
        val themeMode = settingsManager.getThemeDisplayName()
        MaterialAlertDialogBuilder(this)
            .setTitle("Profil Pengguna")
            .setMessage("""
                Nama: $userName
                Prodi: PJJ Informatika S1
                Tema: $themeMode
                Progress: 65%
                Materi Selesai: 13/20
            """.trimIndent())
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton("OK") { dialog, _ ->
                Log.d(TAG, "Profile dialog dismissed")
                dialog.dismiss()
            }
            .setNeutralButton("Pengaturan") { _, _ ->
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            .show()
    }

    private fun startPdfDownload() {
        Log.d(TAG, "Starting PDF download")
        
        // Create progress dialog
        progressDialog = ProgressDialog(this).apply {
            setTitle("Downloading")
            setMessage("Downloading Kurikulum PDF...")
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            setCancelable(false)
            max = 100
        }

        val downloadTask = DownloadPdfTask(this, object : DownloadPdfTask.DownloadListener {
            override fun onDownloadStarted() {
                progressDialog?.show()
            }

            override fun onProgressUpdate(progress: Int) {
                progressDialog?.progress = progress
            }

            override fun onDownloadComplete(file: File) {
                progressDialog?.dismiss()
                showDownloadCompleteDialog(file)
            }

            override fun onDownloadFailed(error: String) {
                progressDialog?.dismiss()
                Toast.makeText(
                    this@MainActivity,
                    "Download failed: $error",
                    Toast.LENGTH_LONG
                ).show()
            }
        })

        downloadTask.execute(KURIKULUM_URL)
    }

    private fun showDownloadCompleteDialog(file: File) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Download Complete")
            .setMessage("Kurikulum PDF has been downloaded successfully!\n\nFile: ${file.name}\nLocation: ${file.parent}")
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton("Open") { _, _ ->
                openPdfFile(file)
            }
            .setNegativeButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun openPdfFile(file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                file
            )
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            }
            
            startActivity(Intent.createChooser(intent, "Open PDF with"))
        } catch (e: Exception) {
            Log.e(TAG, "Error opening PDF", e)
            Toast.makeText(this, "No PDF reader app found", Toast.LENGTH_LONG).show()
        }
    }

    private fun showNetworkError() {
        MaterialAlertDialogBuilder(this)
            .setTitle("No Internet Connection")
            .setMessage("Please check your internet connection and try again.")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        progressDialog?.dismiss()
        Log.d(TAG, "onDestroy: Activity destroyed")
    }
}