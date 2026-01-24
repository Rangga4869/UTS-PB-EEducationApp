package com.kelompok10.eeducation.ui.settings

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kelompok10.eeducation.R
import com.kelompok10.eeducation.utils.SettingsManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var btnBack: TextView
    private lateinit var etUserName: EditText
    private lateinit var rgThemeMode: RadioGroup
    private lateinit var rbLight: RadioButton
    private lateinit var rbDark: RadioButton
    private lateinit var rbSystem: RadioButton
    private lateinit var btnSaveSettings: Button
    private lateinit var btnResetSettings: Button
    private lateinit var tvCurrentTheme: TextView
    
    private lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize SettingsManager first to apply theme
        settingsManager = SettingsManager(this)
        
        setContentView(R.layout.activity_settings)

        // Initialize views
        initViews()

        // Load current settings
        loadSettings()

        // Setup listeners
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        etUserName = findViewById(R.id.etUserName)
        rgThemeMode = findViewById(R.id.rgThemeMode)
        rbLight = findViewById(R.id.rbLight)
        rbDark = findViewById(R.id.rbDark)
        rbSystem = findViewById(R.id.rbSystem)
        btnSaveSettings = findViewById(R.id.btnSaveSettings)
        btnResetSettings = findViewById(R.id.btnResetSettings)
        tvCurrentTheme = findViewById(R.id.tvCurrentTheme)
    }

    private fun loadSettings() {
        // Load user name
        val userName = settingsManager.getUserName()
        etUserName.setText(userName)

        // Load theme mode
        val themeMode = settingsManager.getThemeMode()
        when (themeMode) {
            SettingsManager.THEME_LIGHT -> rbLight.isChecked = true
            SettingsManager.THEME_DARK -> rbDark.isChecked = true
            SettingsManager.THEME_SYSTEM -> rbSystem.isChecked = true
        }

        // Update current theme display
        updateCurrentThemeDisplay()
    }

    private fun setupListeners() {
        // Back button
        btnBack.setOnClickListener {
            finish()
        }

        // Save settings button
        btnSaveSettings.setOnClickListener {
            saveSettings()
        }

        // Reset settings button
        btnResetSettings.setOnClickListener {
            showResetConfirmationDialog()
        }

        // Theme radio group change listener
        rgThemeMode.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbLight -> {
                    Toast.makeText(this, "Tema Terang akan diterapkan", Toast.LENGTH_SHORT).show()
                }
                R.id.rbDark -> {
                    Toast.makeText(this, "Tema Gelap akan diterapkan", Toast.LENGTH_SHORT).show()
                }
                R.id.rbSystem -> {
                    Toast.makeText(this, "Tema mengikuti sistem", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveSettings() {
        // Get user name
        val userName = etUserName.text.toString().trim()

        // Validate user name
        if (userName.isEmpty()) {
            etUserName.error = "Nama tidak boleh kosong"
            etUserName.requestFocus()
            return
        }

        // Save user name
        settingsManager.saveUserName(userName)

        // Get and save theme mode
        val themeMode = when (rgThemeMode.checkedRadioButtonId) {
            R.id.rbLight -> SettingsManager.THEME_LIGHT
            R.id.rbDark -> SettingsManager.THEME_DARK
            R.id.rbSystem -> SettingsManager.THEME_SYSTEM
            else -> SettingsManager.THEME_SYSTEM
        }

        // Check if theme changed
        val currentTheme = settingsManager.getThemeMode()
        val themeChanged = currentTheme != themeMode

        // Save theme
        settingsManager.saveThemeMode(themeMode)

        // Show success message
        Toast.makeText(this, "Pengaturan berhasil disimpan!", Toast.LENGTH_SHORT).show()

        // Apply theme if changed
        if (themeChanged) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Tema Diubah")
                .setMessage("Tema akan diterapkan sekarang. Aplikasi akan me-refresh tampilan.")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("OK") { _, _ ->
                    applyThemeAndRecreate(themeMode)
                }
                .setCancelable(false)
                .show()
        } else {
            // Just finish if no theme change
            finish()
        }
    }

    private fun applyThemeAndRecreate(themeMode: String) {
        settingsManager.applyTheme(themeMode)
        // Recreate activity to apply theme immediately
        recreate()
        updateCurrentThemeDisplay()
    }

    private fun showResetConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Reset Pengaturan")
            .setMessage("Apakah Anda yakin ingin mereset semua pengaturan ke default?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Reset") { _, _ ->
                resetSettings()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun resetSettings() {
        // Clear all settings
        settingsManager.clearSettings()

        // Reload default settings
        loadSettings()

        // Apply system theme
        settingsManager.applyTheme()

        Toast.makeText(this, "Pengaturan telah direset ke default", Toast.LENGTH_SHORT).show()

        // Recreate to apply changes
        recreate()
    }

    private fun updateCurrentThemeDisplay() {
        val themeName = settingsManager.getThemeDisplayName()
        tvCurrentTheme.text = "Tema Aktif: $themeName"
    }
}
