package com.kelompok10.eeducation.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.kelompok10.eeducation.ui.main.MainActivity

/**
 * NotificationHelper - Centralized notification management
 * Handles creating notification channels and showing notifications
 */
object NotificationHelper {

    // Channel IDs
    const val CHANNEL_STUDY_TIMER = "StudyTimerChannel"
    const val CHANNEL_BATTERY_ALERT = "BatteryAlertChannel"
    const val CHANNEL_GENERAL = "GeneralChannel"
    const val CHANNEL_STUDY_REMINDER = "StudyReminderChannel"

    // Notification IDs
    const val NOTIFICATION_STUDY_TIMER = 1001
    const val NOTIFICATION_BATTERY_ALERT = 2001
    const val NOTIFICATION_GENERAL = 3001
    const val NOTIFICATION_STUDY_REMINDER = 4001

    /**
     * Create all notification channels
     * Call this when the app starts
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Study Timer Channel (Low importance - ongoing notification)
            val studyTimerChannel = NotificationChannel(
                CHANNEL_STUDY_TIMER,
                "Study Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows study timer status"
                setShowBadge(false)
            }

            // Battery Alert Channel (High importance - alerts)
            val batteryAlertChannel = NotificationChannel(
                CHANNEL_BATTERY_ALERT,
                "Battery Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when battery is low during study sessions"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }

            // General Channel (Default importance - general notifications)
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
            }

            // Study Reminder Channel (High importance - daily reminders)
            val studyReminderChannel = NotificationChannel(
                CHANNEL_STUDY_REMINDER,
                "Study Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily study reminder notifications"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(studyTimerChannel)
            notificationManager.createNotificationChannel(batteryAlertChannel)
            notificationManager.createNotificationChannel(generalChannel)
            notificationManager.createNotificationChannel(studyReminderChannel)
        }
    }

    /**
     * Show a simple notification
     */
    fun showNotification(
        context: Context,
        title: String,
        message: String,
        channelId: String = CHANNEL_GENERAL,
        notificationId: Int = NOTIFICATION_GENERAL
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    /**
     * Cancel a notification
     */
    fun cancelNotification(context: Context, notificationId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }

    /**
     * Cancel all notifications
     */
    fun cancelAllNotifications(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }
}
