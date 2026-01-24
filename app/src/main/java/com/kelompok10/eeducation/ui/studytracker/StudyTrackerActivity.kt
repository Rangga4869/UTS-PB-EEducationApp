package com.kelompok10.eeducation.ui.studytracker

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.kelompok10.eeducation.R
import com.kelompok10.eeducation.background.service.StudyTimerService
import com.kelompok10.eeducation.utils.NotificationHelper
import java.util.concurrent.TimeUnit

class StudyTrackerActivity : AppCompatActivity() {

    private lateinit var tvTimer: TextView
    private lateinit var tvTotalStudyTime: TextView
    private lateinit var tvBatteryStatus: TextView
    private lateinit var btnStart: Button
    private lateinit var btnPause: Button
    private lateinit var btnStop: Button

    private var isTimerRunning = false
    private var isTimerPaused = false
    private var isReceiverRegistered = false

    // FIX 1: Add a Handler to update UI every second locally
    private val handler = Handler(Looper.getMainLooper())
    private val updateTimerRunnable = object : Runnable {
        override fun run() {
            if (isTimerRunning && !isTimerPaused) {
                updateTimerText() // Update the numbers
                handler.postDelayed(this, 1000) // Run again in 1 second
            }
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "Permission needed for timer notifications", Toast.LENGTH_SHORT).show()
        }
    }

    // FIX 2: Receiver now just syncs the state, doesn't rely on "TICK" broadcasts
    private val timerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                StudyTimerService.ACTION_START_TIMER,
                StudyTimerService.ACTION_RESUME_TIMER -> {
                    // Sync state from Service
                    isTimerRunning = true
                    isTimerPaused = false
                    startLocalUiLoop() // Ensure the UI counts
                    updateButtons()
                }
                StudyTimerService.ACTION_PAUSE_TIMER -> {
                    isTimerRunning = true
                    isTimerPaused = true
                    stopLocalUiLoop() // Stop counting
                    // FIX 3: Re-read the latest saved time from Prefs so it doesn't jump back
                    updateTimerText() 
                    updateButtons()
                }
                StudyTimerService.ACTION_STOP_TIMER -> {
                    isTimerRunning = false
                    isTimerPaused = false
                    stopLocalUiLoop()
                    tvTimer.text = "00:00:00"
                    updateTotalStudyTime()
                    updateButtons()
                    Toast.makeText(context, "Session Saved!", Toast.LENGTH_SHORT).show()
                }
                "com.kelompok10.eeducation.BATTERY_LOW_ALERT" -> {
                    val batteryLevel = intent.getIntExtra("battery_level", 0)
                    tvBatteryStatus.text = "⚠️ Low Battery: $batteryLevel%"
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_tracker)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initializeViews()
        setupClickListeners()
        loadTimerState() // Initial load
        checkAndRequestNotificationPermission()
        NotificationHelper.createNotificationChannels(this)
        
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { finish() }
        })
    }

    private fun initializeViews() {
        tvTimer = findViewById(R.id.tv_timer)
        tvTotalStudyTime = findViewById(R.id.tv_total_study_time)
        tvBatteryStatus = findViewById(R.id.tv_battery_status)
        btnStart = findViewById(R.id.btn_start)
        btnPause = findViewById(R.id.btn_pause)
        btnStop = findViewById(R.id.btn_stop)
    }

    // FIX 4: Buttons explicitly start/stop the UI loop for immediate feedback
    private fun setupClickListeners() {
        btnStart.setOnClickListener {
            sendCommandToService(StudyTimerService.ACTION_START_TIMER)
            isTimerRunning = true
            isTimerPaused = false
            startLocalUiLoop() // Start counting immediately!
            updateButtons()
        }

        btnPause.setOnClickListener {
            sendCommandToService(StudyTimerService.ACTION_PAUSE_TIMER)
            isTimerRunning = true
            isTimerPaused = true
            stopLocalUiLoop()
            // We wait for the Broadcast to update the final text to avoid jumps
            updateButtons()
        }

        btnStop.setOnClickListener {
            sendCommandToService(StudyTimerService.ACTION_STOP_TIMER)
            isTimerRunning = false
            stopLocalUiLoop()
            tvTimer.text = "00:00:00"
            updateButtons()
        }
    }

    // Unified helper to send commands
    private fun sendCommandToService(action: String) {
        val intent = Intent(this, StudyTimerService::class.java)
        intent.action = action
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    // FIX 5: The Math Logic
    // This function calculates time dynamically so it never freezes
    private fun updateTimerText() {
        val prefs = getSharedPreferences("StudyTimerPrefs", Context.MODE_PRIVATE)
        val sessionStartTime = prefs.getLong("session_start_time", 0L)
        val previouslyAccumulatedTime = prefs.getLong("elapsed_time", 0L)

        val displayTime: Long
        if (isTimerRunning && !isTimerPaused) {
            // If running: Time = Old Saved Time + (Now - Start Time)
            val timeSinceStart = SystemClock.elapsedRealtime() - sessionStartTime
            displayTime = previouslyAccumulatedTime + timeSinceStart
        } else {
            // If paused/stopped: Time = Just the Saved Time
            displayTime = previouslyAccumulatedTime
        }
        
        tvTimer.text = formatTime(displayTime)
    }

    private fun startLocalUiLoop() {
        handler.removeCallbacks(updateTimerRunnable) // Safety clear
        handler.post(updateTimerRunnable) // Start ticking
    }

    private fun stopLocalUiLoop() {
        handler.removeCallbacks(updateTimerRunnable)
    }

    private fun loadTimerState() {
        val prefs = getSharedPreferences("StudyTimerPrefs", Context.MODE_PRIVATE)
        isTimerRunning = prefs.getBoolean("is_running", false)
        val elapsedTime = prefs.getLong("elapsed_time", 0L)
        
        // Logic to determine if we are paused or running based on Prefs
        if (isTimerRunning) {
             isTimerPaused = false
             startLocalUiLoop()
        } else {
            if (elapsedTime > 0) {
                isTimerPaused = true
                isTimerRunning = true // Technically a "paused" session is still an active session
            } else {
                isTimerRunning = false
                isTimerPaused = false
            }
        }
        updateTimerText()
        updateTotalStudyTime()
        updateButtons()
    }

    private fun updateButtons() {
        if (!isTimerRunning && !isTimerPaused) { // Stopped
            btnStart.isEnabled = true
            btnStart.text = "Start Study Session"
            btnPause.isEnabled = false
            btnStop.isEnabled = false
        } else if (isTimerPaused) { // Paused
            btnStart.isEnabled = true
            btnStart.text = "Resume"
            btnPause.isEnabled = false
            btnStop.isEnabled = true
        } else { // Running
            btnStart.isEnabled = false
            btnPause.isEnabled = true
            btnStop.isEnabled = true
        }
    }
    
    override fun onResume() {
        super.onResume()
        loadTimerState()
        
        val filter = IntentFilter().apply {
            addAction(StudyTimerService.ACTION_START_TIMER)
            addAction(StudyTimerService.ACTION_STOP_TIMER)
            addAction(StudyTimerService.ACTION_PAUSE_TIMER)
            addAction(StudyTimerService.ACTION_RESUME_TIMER)
            addAction("com.kelompok10.eeducation.BATTERY_LOW_ALERT")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(timerReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(timerReceiver, filter)
        }
        isReceiverRegistered = true
    }

    override fun onPause() {
        super.onPause()
        stopLocalUiLoop() // Save battery when app is minimized
        if (isReceiverRegistered) {
            unregisterReceiver(timerReceiver)
            isReceiverRegistered = false
        }
    }
    
    private fun updateTotalStudyTime() {
        val prefs = getSharedPreferences("StudyTimerPrefs", Context.MODE_PRIVATE)
        val totalTime = prefs.getLong("total_study_time", 0L)
        val hours = TimeUnit.MILLISECONDS.toHours(totalTime)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(totalTime) % 60
        tvTotalStudyTime.text = "Total Study Time: ${hours}h ${minutes}m"
    }

    private fun formatTime(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
    
    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
