package com.kelompok10.eeducation.utils

import android.content.Context
import android.util.Log
import androidx.work.*
import com.kelompok10.eeducation.background.worker.SyllabusSyncWorker
import java.util.concurrent.TimeUnit

/**
 * SyncScheduler - Schedules periodic background sync jobs
 */
object SyncScheduler {

    private const val TAG = "SyncScheduler"
    private const val TESTING_MODE = false // Set to false for production

    /**
     * Schedule nightly syllabus sync
     * 
     * TESTING MODE: Runs 1 minute after scheduling (for immediate testing)
     * PRODUCTION MODE: Runs every 24 hours when device is idle at night
     */
    fun scheduleNightlySyllabusSync(context: Context) {
        if (TESTING_MODE) {
            scheduleTestSync(context)
        } else {
            scheduleProductionSync(context)
        }
    }

    /**
     * TEST VERSION - Runs 1 minute after scheduling for immediate testing
     */
    private fun scheduleTestSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Any network (easier testing)
            .build()

        // OneTime work with 1 minute delay
        val testWorkRequest = OneTimeWorkRequestBuilder<SyllabusSyncWorker>()
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.MINUTES) // Run after 1 minute
            .addTag("syllabus_sync_test")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            SyllabusSyncWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE, // Replace existing
            testWorkRequest
        )

        Log.d(TAG, "🧪 TEST MODE: Syllabus sync scheduled to run in 1 minute")
    }

    /**
     * PRODUCTION VERSION - Runs every 24 hours at night when device is idle
     */
    private fun scheduleProductionSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED) // Wi-Fi only
            .setRequiresDeviceIdle(true) // Only when device is idle
            .setRequiresBatteryNotLow(true) // Don't drain battery
            .build()

        val syncWorkRequest = PeriodicWorkRequestBuilder<SyllabusSyncWorker>(
            24, TimeUnit.HOURS, // Repeat interval
            4, TimeUnit.HOURS   // Flex interval
        )
            .setConstraints(constraints)
            .addTag("syllabus_sync")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SyllabusSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )

        Log.d(TAG, "✅ PRODUCTION MODE: Nightly syllabus sync scheduled")
    }

    /**
     * Cancel the nightly sync
     */
    fun cancelNightlySyllabusSync(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(SyllabusSyncWorker.WORK_NAME)
        Log.d(TAG, "Nightly syllabus sync cancelled")
    }

    /**
     * Trigger immediate sync (for testing or manual refresh)
     */
    fun triggerImmediateSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Any network
            .build()

        val immediateWorkRequest = OneTimeWorkRequestBuilder<SyllabusSyncWorker>()
            .setConstraints(constraints)
            .addTag("manual_sync")
            .build()

        WorkManager.getInstance(context).enqueue(immediateWorkRequest)
        Log.d(TAG, "🚀 Immediate sync triggered")
    }

    /**
     * Check if sync is currently scheduled
     */
    fun isSyncScheduled(context: Context, callback: (Boolean) -> Unit) {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(SyllabusSyncWorker.WORK_NAME)

        workInfos.get().let { infos ->
            val isScheduled = infos.any { 
                it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING 
            }
            callback(isScheduled)
        }
    }
}
