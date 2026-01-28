package com.kelompok10.eeducation.background.receiver

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.kelompok10.eeducation.ui.main.MainActivity

/**
 * BatteryLevelReceiver - Monitors battery level changes
 * Alerts students when battery is below 15% to suggest taking a break
 */
class BatteryLevelReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "BatteryAlertChannel"
        private const val CHANNEL_NAME = "Battery Alerts"
        private const val NOTIFICATION_ID = 2001

        private const val LOW_BATTERY_THRESHOLD = 15
        private const val PREFS_NAME = "BatteryAlertPrefs"
        private const val KEY_LAST_ALERT_TIME = "last_alert_time"
        private const val ALERT_COOLDOWN_MS = 5 * 60 * 1000L // 5 minutes
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BATTERY_CHANGED -> {
                handleBatteryChanged(context, intent)
            }
            Intent.ACTION_BATTERY_LOW -> {
                handleBatteryLow(context)
            }
        }
    }

    private fun handleBatteryChanged(context: Context, intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = level * 100 / scale.toFloat()

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

        // Only alert if battery is low, not charging, and cooldown period has passed
        if (batteryPct <= LOW_BATTERY_THRESHOLD && !isCharging) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val lastAlertTime = prefs.getLong(KEY_LAST_ALERT_TIME, 0L)
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastAlertTime > ALERT_COOLDOWN_MS) {
                showLowBatteryNotification(context, batteryPct.toInt())

                // Save alert time
                prefs.edit().putLong(KEY_LAST_ALERT_TIME, currentTime).apply()

                // Broadcast battery low event
                val broadcastIntent = Intent("com.kelompok10.eeducation.BATTERY_LOW_ALERT")
                broadcastIntent.putExtra("battery_level", batteryPct.toInt())
                context.sendBroadcast(broadcastIntent)
            }
        }
    }

    private fun handleBatteryLow(context: Context) {
        showLowBatteryNotification(context, LOW_BATTERY_THRESHOLD)
    }

    private fun showLowBatteryNotification(context: Context, batteryLevel: Int) {
        createNotificationChannel(context)

        val intent = createMainActivityIntent(context)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dialog_alert)
            .setContentTitle("⚡ Low Battery Alert!")
            .setContentText("Battery at $batteryLevel%. Time to take a break and charge your device!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                .bigText("Your battery is at $batteryLevel%. Consider taking a study break and charging your device. Remember: taking breaks helps improve focus and retention! 📚"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createMainActivityIntent(context: Context): Intent {
        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when battery is low during study sessions"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}