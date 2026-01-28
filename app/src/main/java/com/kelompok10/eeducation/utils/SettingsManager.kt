package com.kelompok10.eeducation.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

/**
 * SettingsManager - Manages all application settings using SharedPreferences
 * Handles user preferences like username and theme mode
 */
class SettingsManager(context: Context) {

    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "eeducation_settings"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_THEME_MODE = "theme_mode"
        
        // Theme mode constants
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val THEME_SYSTEM = "system"
        
        // Default values
        private const val DEFAULT_USER_NAME = "Mahasiswa IF703"
        private const val DEFAULT_THEME = THEME_SYSTEM
    }

    /**
     * Save user name to SharedPreferences
     */
    fun saveUserName(name: String) {
        sharedPreferences.edit().apply {
            putString(KEY_USER_NAME, name)
            apply()
        }
    }

    /**
     * Get user name from SharedPreferences
     */
    fun getUserName(): String {
        return sharedPreferences.getString(KEY_USER_NAME, DEFAULT_USER_NAME) ?: DEFAULT_USER_NAME
    }

    /**
     * Save theme mode to SharedPreferences
     */
    fun saveThemeMode(themeMode: String) {
        sharedPreferences.edit().apply {
            putString(KEY_THEME_MODE, themeMode)
            apply()
        }
    }

    /**
     * Get theme mode from SharedPreferences
     */
    fun getThemeMode(): String {
        return sharedPreferences.getString(KEY_THEME_MODE, DEFAULT_THEME) ?: DEFAULT_THEME
    }

    /**
     * Apply theme to the application
     */
    fun applyTheme() {
        val themeMode = getThemeMode()
        when (themeMode) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    /**
     * Apply theme with specific mode
     */
    fun applyTheme(themeMode: String) {
        saveThemeMode(themeMode)
        applyTheme()
    }

    /**
     * Check if this is first launch
     */
    fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean("first_launch", true)
    }

    /**
     * Mark first launch as complete
     */
    fun setFirstLaunchComplete() {
        sharedPreferences.edit().apply {
            putBoolean("first_launch", false)
            apply()
        }
    }

    /**
     * Clear all settings (useful for logout or reset)
     */
    fun clearSettings() {
        sharedPreferences.edit().clear().apply()
    }

    /**
     * Get theme display name for UI
     */
    fun getThemeDisplayName(): String {
        return when (getThemeMode()) {
            THEME_LIGHT -> "Terang"
            THEME_DARK -> "Gelap"
            THEME_SYSTEM -> "Sistem"
            else -> "Sistem"
        }
    }
}
