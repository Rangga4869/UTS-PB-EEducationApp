package com.kelompok10.eeducation.background.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kelompok10.eeducation.data.local.AppDatabase
import com.kelompok10.eeducation.data.local.Materi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * SyllabusSyncWorker - Background worker for syncing syllabus/materi data
 * Runs periodically at night when device is idle and connected to Wi-Fi
 */
class SyllabusSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val database = AppDatabase.getDatabase(context)
    private val materiDao = database.materiDao()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting nightly syllabus sync...")

            // Fetch materi from GitHub Raw (or your server)
            val materiList = fetchMateriFromServer()

            if (materiList.isNotEmpty()) {
                // Clear old data and insert new data
                materiDao.deleteAll()
                materiDao.insertAll(materiList)
                
                Log.d(TAG, "Syllabus sync completed successfully. ${materiList.size} items synced.")
                Result.success()
            } else {
                Log.w(TAG, "No materi data found on server")
                Result.success() // Still success, just no data
            }
        } catch (e: Exception) {
            Log.e(TAG, "Syllabus sync failed: ${e.message}", e)
            // Retry if failed
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private fun fetchMateriFromServer(): List<Materi> {
        return try {
            // Replace with your actual GitHub Raw URL or API endpoint
            val url = URL("https://raw.githubusercontent.com/Rangga4869/UTS-PB-EEducationApp/refs/heads/dev-faisal/app/src/main/assets/materi_awal.json")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("Accept", "application/json")
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.use { it.readText() }
                
                // Parse JSON to List<Materi>
                val gson = Gson()
                val type = object : TypeToken<List<Materi>>() {}.type
                val materiList: List<Materi> = gson.fromJson(response, type)
                
                Log.d(TAG, "Fetched ${materiList.size} materi items from server")
                materiList
            } else {
                Log.e(TAG, "Server returned error code: $responseCode")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching materi from server: ${e.message}", e)
            emptyList()
        }
    }

    companion object {
        private const val TAG = "SyllabusSyncWorker"
        const val WORK_NAME = "nightly_syllabus_sync"
    }
}
