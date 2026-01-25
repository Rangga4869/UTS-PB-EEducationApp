package com.kelompok10.eeducation.background.receiver

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.kelompok10.eeducation.R
import com.kelompok10.eeducation.ui.studytracker.StudyTrackerActivity
import com.kelompok10.eeducation.utils.NotificationHelper
import java.util.Calendar

/**
 * StudyReminderReceiver - Handles daily study reminder alarms
 * Triggered by AlarmManager to show "Time to Study!" notifications
 */
class StudyReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "StudyReminderReceiver triggered! Time: ${System.currentTimeMillis()}")

        // 1. Check Permission (CRITICAL for Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Notification permission NOT granted. Cannot show reminder.")
                // Even if permission is missing, we reschedule next alarm so we don't lose the cycle
                rescheduleNextReminder(context)
                return
            }
        }

        // 2. Show the notification safely
        try {
            showStudyReminderNotification(context)
            Log.d(TAG, "Notification request sent to system.")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification: ${e.message}")
        }

        // 3. Reschedule the next alarm
        rescheduleNextReminder(context)
    }

    private fun showStudyReminderNotification(context: Context) {
        val notificationIntent = Intent(context, StudyTrackerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_STUDY_REMINDER)
            // CHANGE: Use your app icon instead of system icon to ensure visibility
            .setSmallIcon(R.mipmap.ic_launcher_round) 
            .setContentTitle("⏰ Time to Study!")
            .setContentText("Don't forget your study session today!")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Heads-up notification
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NotificationHelper.NOTIFICATION_STUDY_REMINDER, notification)
    }

    private fun rescheduleNextReminder(context: Context) {
        val prefs = context.getSharedPreferences("StudyReminderPrefs", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("reminder_enabled", false)
        
        Log.d(TAG, "Rescheduling next reminder... Enabled: $isEnabled")
        
        if (!isEnabled) {
            Log.d(TAG, "Reminder is disabled, not rescheduling")
            return
        }
        
        val hour = prefs.getInt("reminder_hour", 9)
        val minute = prefs.getInt("reminder_minute", 0)
        
        // Schedule for tomorrow at the same time
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.DAY_OF_MONTH, 1) // Tomorrow
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, StudyReminderReceiver::class.java)
        
        // Ensure Request Code matches (1001)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1001, 
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            
            // Save next time for UI
            prefs.edit().apply {
                putLong("next_alarm_time", calendar.timeInMillis)
                apply()
            }
            
            Log.d(TAG, "Next reminder scheduled for: ${calendar.time}")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to reschedule alarm: Permission denied")
        }
    }

    companion object {
        private const val TAG = "StudyReminderReceiver"
    }
}
