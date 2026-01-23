package com.kelompok10.eeducation.utils

import android.content.Context
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

@Suppress("DEPRECATION")
class DownloadPdfTask(
    private val context: Context,
    private val listener: DownloadListener
) : AsyncTask<String, Int, DownloadResult>() {

    interface DownloadListener {
        fun onDownloadStarted()
        fun onProgressUpdate(progress: Int)
        fun onDownloadComplete(file: File)
        fun onDownloadFailed(error: String)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        listener.onDownloadStarted()
        Log.d(TAG, "Download started")
    }

    override fun doInBackground(vararg params: String?): DownloadResult {
        val urlString = params[0] ?: return DownloadResult.Error("URL is null")
        
        try {
            // Check network connection
            if (!NetworkUtils.isNetworkAvailable(context)) {
                return DownloadResult.Error("No internet connection")
            }

            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return DownloadResult.Error("Server returned HTTP $responseCode")
            }

            val fileLength = connection.contentLength
            val inputStream: InputStream = connection.inputStream
            
            // Create downloads directory if it doesn't exist
            val downloadsDir = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                "Kurikulum"
            )
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            // Create output file
            val outputFile = File(downloadsDir, "KURIKULUM.pdf")
            val outputStream = FileOutputStream(outputFile)

            val buffer = ByteArray(4096)
            var total: Long = 0
            var count: Int

            while (inputStream.read(buffer).also { count = it } != -1) {
                if (isCancelled) {
                    inputStream.close()
                    outputStream.close()
                    return DownloadResult.Error("Download cancelled")
                }

                total += count.toLong()
                
                // Update progress
                if (fileLength > 0) {
                    val progress = ((total * 100) / fileLength).toInt()
                    publishProgress(progress)
                }

                outputStream.write(buffer, 0, count)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()
            connection.disconnect()

            Log.d(TAG, "Download completed: ${outputFile.absolutePath}")
            return DownloadResult.Success(outputFile)

        } catch (e: Exception) {
            Log.e(TAG, "Download error", e)
            return DownloadResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        values[0]?.let { 
            listener.onProgressUpdate(it)
            Log.d(TAG, "Download progress: $it%")
        }
    }

    override fun onPostExecute(result: DownloadResult) {
        super.onPostExecute(result)
        when (result) {
            is DownloadResult.Success -> {
                listener.onDownloadComplete(result.file)
                Log.d(TAG, "Download successful: ${result.file.absolutePath}")
            }
            is DownloadResult.Error -> {
                listener.onDownloadFailed(result.message)
                Log.e(TAG, "Download failed: ${result.message}")
            }
        }
    }

    companion object {
        private const val TAG = "DownloadPdfTask"
    }
}

sealed class DownloadResult {
    data class Success(val file: File) : DownloadResult()
    data class Error(val message: String) : DownloadResult()
}
