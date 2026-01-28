package com.kelompok10.eeducation.ui.studytracker

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.kelompok10.eeducation.R
import com.kelompok10.eeducation.background.receiver.StudyReminderReceiver
import com.kelompok10.eeducation.background.service.StudyTimerService
import com.kelompok10.eeducation.utils.NotificationHelper
import java.util.Calendar
import java.util.concurrent.TimeUnit

class StudyTrackerActivity : AppCompatActivity() {

    private lateinit var tvTimer: TextView
    private lateinit var tvTotalStudyTime: TextView
    private lateinit var tvBatteryStatus: TextView
    private lateinit var tvReminderStatus: TextView
    private lateinit var btnStart: Button
    private lateinit var btnPause: Button
    private lateinit var btnStop: Button
    private lateinit var btnSetReminder: Button

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
        updateReminderStatus() // Load reminder status
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
        tvReminderStatus = findViewById(R.id.tv_reminder_status)
        btnStart = findViewById(R.id.btn_start)
        btnPause = findViewById(R.id.btn_pause)
        btnStop = findViewById(R.id.btn_stop)
        btnSetReminder = findViewById(R.id.btn_set_reminder)
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

        btnSetReminder.setOnClickListener {
            showTimePickerDialog()
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

    // ===== Daily Study Reminder Methods =====

    private fun showTimePickerDialog() {
        val prefs = getSharedPreferences("StudyReminderPrefs", Context.MODE_PRIVATE)
        val savedHour = prefs.getInt("reminder_hour", 9)
        val savedMinute = prefs.getInt("reminder_minute", 0)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                scheduleStudyReminder(hourOfDay, minute)
            },
            savedHour,
            savedMinute,
            true // Use 24-hour format
        )

        timePickerDialog.setButton(TimePickerDialog.BUTTON_NEUTRAL, "Cancel Reminder") { _, which ->
            if (which == TimePickerDialog.BUTTON_NEUTRAL) {
                cancelStudyReminder()
            }
        }

        timePickerDialog.show()
    }

    private fun scheduleStudyReminder(hour: Int, minute: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Check if we can schedule exact alarms on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "Please enable exact alarm permission in settings", Toast.LENGTH_LONG).show()
                // Open settings to enable exact alarm permission
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                return
            }
        }

        // Create calendar for the reminder time
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // CRITICAL FIX: If the time has already passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
                Log.d(TAG, "Time has passed, scheduling for tomorrow")
            }
        }
        
        Log.d(TAG, "Scheduling alarm for: ${calendar.time}")
        Log.d(TAG, "Current time: ${Calendar.getInstance().time}")

        // Create PendingIntent for the alarm
        val intent = Intent(this, StudyReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Cancel any existing alarm first
        alarmManager.cancel(pendingIntent)

        // Schedule the alarm using setExactAndAllowWhileIdle for better reliability
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Use setExactAndAllowWhileIdle for Android 6.0+
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                Log.d(TAG, "Alarm scheduled with setExactAndAllowWhileIdle")
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                Log.d(TAG, "Alarm scheduled with setExact")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to schedule alarm", e)
            Toast.makeText(this, "Cannot schedule exact alarm. Please check settings.", Toast.LENGTH_LONG).show()
            return
        }

        // Save reminder time in SharedPreferences
        val prefs = getSharedPreferences("StudyReminderPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putInt("reminder_hour", hour)
            putInt("reminder_minute", minute)
            putBoolean("reminder_enabled", true)
            putLong("next_alarm_time", calendar.timeInMillis)
            apply()
        }

        // Update UI
        updateReminderStatus()
        
        // Show when the reminder will trigger
        val now = System.currentTimeMillis()
        val reminderText = if (calendar.timeInMillis - now < 24 * 60 * 60 * 1000) {
            "Reminder set for Today at ${String.format("%02d:%02d", hour, minute)}"
        } else {
            "Reminder set for Tomorrow at ${String.format("%02d:%02d", hour, minute)}"
        }
        
        Toast.makeText(this, reminderText, Toast.LENGTH_LONG).show()
    }

    private fun cancelStudyReminder() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Cancel the alarm
        val intent = Intent(this, StudyReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)

        // Clear saved reminder settings
        val prefs = getSharedPreferences("StudyReminderPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            remove("reminder_hour")
            remove("reminder_minute")
            putBoolean("reminder_enabled", false)
            apply()
        }

        // Update UI
        updateReminderStatus()
        Toast.makeText(this, "Daily reminder cancelled", Toast.LENGTH_SHORT).show()
    }

    private fun updateReminderStatus() {
        val prefs = getSharedPreferences("StudyReminderPrefs", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("reminder_enabled", false)

        if (isEnabled) {
            val hour = prefs.getInt("reminder_hour", 9)
            val minute = prefs.getInt("reminder_minute", 0)
            tvReminderStatus.text = "Daily reminder set for ${String.format("%02d:%02d", hour, minute)}"
        } else {
            tvReminderStatus.text = "No reminder set"
        }
    }

    companion object {
        private const val TAG = "StudyTrackerActivity"
        private const val REMINDER_REQUEST_CODE = 1001
    }
}
