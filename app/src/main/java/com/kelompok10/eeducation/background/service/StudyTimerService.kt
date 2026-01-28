package com.kelompok10.eeducation.background.service

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.kelompok10.eeducation.ui.main.MainActivity
import java.util.concurrent.TimeUnit

/**
 * StudyTimerService - A foreground service that tracks study time
 * This service continues running even when the app is minimized,
 * helping students track their learning sessions
 */
class StudyTimerService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "StudyTimerChannel"
        private const val CHANNEL_NAME = "Study Timer"

        const val ACTION_START_TIMER = "ACTION_START_TIMER"
        const val ACTION_STOP_TIMER = "ACTION_STOP_TIMER"
        const val ACTION_PAUSE_TIMER = "ACTION_PAUSE_TIMER"
        const val ACTION_RESUME_TIMER = "ACTION_RESUME_TIMER"

        private const val PREFS_NAME = "StudyTimerPrefs"
        private const val KEY_TOTAL_STUDY_TIME = "total_study_time"
        private const val KEY_SESSION_START_TIME = "session_start_time"
        private const val KEY_IS_RUNNING = "is_running"
        private const val KEY_ELAPSED_TIME = "elapsed_time"
    }

    private var startTime: Long = 0L
    private var elapsedTime: Long = 0L
    private var isTimerRunning = false
    private var updateThread: Thread? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        loadTimerState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Must call startForeground immediately to avoid crash on Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                createNotification(elapsedTime),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                } else {
                    0 // No specific type for versions below Android 14
                }
            )
        } else {
            startForeground(NOTIFICATION_ID, createNotification(elapsedTime))
        }

        when (intent?.action) {
            ACTION_START_TIMER -> startTimer()
            ACTION_STOP_TIMER -> stopTimer()
            ACTION_PAUSE_TIMER -> pauseTimer()
            ACTION_RESUME_TIMER -> resumeTimer()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startTimer() {
        if (!isTimerRunning) {
            isTimerRunning = true
            startTime = SystemClock.elapsedRealtime()
            elapsedTime = 0L
            saveTimerState()

            // Update notification with running state
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, createNotification(0))

            startUpdateThread()

            // Broadcast timer started
            sendBroadcast(Intent("com.kelompok10.eeducation.STUDY_TIMER_STARTED"))
        }
    }

    private fun stopTimer() {
        if (isTimerRunning) {
            isTimerRunning = false
            val totalElapsed = elapsedTime + (SystemClock.elapsedRealtime() - startTime)

            // Save total study time
            saveTotalStudyTime(totalElapsed)

            elapsedTime = 0L
            startTime = 0L
            saveTimerState()

            // Broadcast timer stopped with total time
            val intent = Intent("com.kelompok10.eeducation.STUDY_TIMER_STOPPED")
            intent.putExtra("elapsed_time", totalElapsed)
            sendBroadcast(intent)

            stopUpdateThread()
            stopForeground(true)
            stopSelf()
        }
    }

    private fun pauseTimer() {
        if (isTimerRunning) {
            isTimerRunning = false
            elapsedTime += SystemClock.elapsedRealtime() - startTime
            saveTimerState()

            stopUpdateThread()

            // Update notification with paused state
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, createNotification(elapsedTime))

            // Broadcast timer paused
            val intent = Intent("com.kelompok10.eeducation.STUDY_TIMER_PAUSED")
            intent.putExtra("elapsed_time", elapsedTime)
            sendBroadcast(intent)
        }
    }

    private fun resumeTimer() {
        if (!isTimerRunning) {
            isTimerRunning = true
            startTime = SystemClock.elapsedRealtime()
            saveTimerState()

            startUpdateThread()

            // Broadcast timer resumed
            sendBroadcast(Intent("com.kelompok10.eeducation.STUDY_TIMER_RESUMED"))
        }
    }

    private fun startUpdateThread() {
        stopUpdateThread()
        updateThread = Thread {
            try {
                while (isTimerRunning && !Thread.currentThread().isInterrupted) {
                    val currentElapsed = elapsedTime + (SystemClock.elapsedRealtime() - startTime)

                    // Update notification every second
                    val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(NOTIFICATION_ID, createNotification(currentElapsed))

                    // Broadcast current time
                    val intent = Intent("com.kelompok10.eeducation.STUDY_TIMER_TICK")
                    intent.putExtra("elapsed_time", currentElapsed)
                    sendBroadcast(intent)

                    Thread.sleep(1000)
                }
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        updateThread?.start()
    }

    private fun stopUpdateThread() {
        updateThread?.interrupt()
        updateThread = null
    }

    private fun createNotification(elapsedMillis: Long): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeString = formatTime(elapsedMillis)
        val statusText = if (isTimerRunning) "Active" else "Paused"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Study Timer $statusText")
            .setContentText("Study time: $timeString")
            .setSmallIcon(R.drawable.ic_menu_recent_history)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows study timer status"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun formatTime(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun saveTimerState() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().apply {
            putLong(KEY_SESSION_START_TIME, startTime)
            putLong(KEY_ELAPSED_TIME, elapsedTime)
            putBoolean(KEY_IS_RUNNING, isTimerRunning)
            apply()
        }
    }

    private fun loadTimerState() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        startTime = prefs.getLong(KEY_SESSION_START_TIME, 0L)
        elapsedTime = prefs.getLong(KEY_ELAPSED_TIME, 0L)
        isTimerRunning = prefs.getBoolean(KEY_IS_RUNNING, false)
    }

    private fun saveTotalStudyTime(sessionTime: Long) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val currentTotal = prefs.getLong(KEY_TOTAL_STUDY_TIME, 0L)
        prefs.edit().putLong(KEY_TOTAL_STUDY_TIME, currentTotal + sessionTime).apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopUpdateThread()
    }
}