package com.kelompok10.eeducation
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var cardMateri: CardView
    private lateinit var cardQuiz: CardView
    private lateinit var cardVideo: CardView
    private lateinit var cardProfile: CardView

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Activity started")
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
    }

    private fun showWelcomeMessage() {
        Log.d(TAG, "Showing welcome message")
        Toast.makeText(
            this,
            "Selamat belajar di E-Education App!",
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
        MaterialAlertDialogBuilder(this)
            .setTitle("Profil Pengguna")
            .setMessage("""
                Nama: Mahasiswa IF703
                Prodi: PJJ Informatika S1
                Progress: 65%
                Materi Selesai: 13/20
            """.trimIndent())
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton("OK") { dialog, _ ->
                Log.d(TAG, "Profile dialog dismissed")
                dialog.dismiss()
            }
            .show()
    }

    override fun onBackPressed() {
        Log.d(TAG, "Back button pressed")
        MaterialAlertDialogBuilder(this)
            .setTitle("Keluar Aplikasi")
            .setMessage("Apakah Anda yakin ingin keluar?")
            .setPositiveButton("Ya") { _, _ ->
                Log.d(TAG, "User confirmed exit")
                super.onBackPressed()
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                Log.d(TAG, "User cancelled exit")
                dialog.dismiss()
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Activity resumed")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: Activity paused")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Activity destroyed")
    }
}