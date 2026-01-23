package com.kelompok10.eeducation.ui.materi

import android.content.Context
import android.util.Log
import androidx.loader.content.AsyncTaskLoader
import com.kelompok10.eeducation.data.model.News
import com.kelompok10.eeducation.utils.NetworkUtils
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class NewsLoader(context: Context, private val url: String) : AsyncTaskLoader<List<News>>(context) {
    
    private var cachedData: List<News>? = null
    
    override fun onStartLoading() {
        if (cachedData != null) {
            // Deliver cached data immediately
            deliverResult(cachedData)
        } else {
            // Force load if no cached data
            forceLoad()
        }
    }

    override fun loadInBackground(): List<News>? {
        // Check network connection first
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.e(TAG, "No internet connection")
            return null
        }

        var connection: HttpURLConnection? = null
        var reader: BufferedReader? = null
        
        try {
            val urlConnection = URL(url)
            connection = urlConnection.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "HTTP error code: $responseCode")
                return null
            }

            // Read response
            reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?
            
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }

            // Parse JSON
            val jsonArray = JSONArray(response.toString())
            val newsList = mutableListOf<News>()
            
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val news = News.fromJson(jsonObject)
                newsList.add(news)
            }

            Log.d(TAG, "Successfully loaded ${newsList.size} news items")
            return newsList

        } catch (e: Exception) {
            Log.e(TAG, "Error loading news", e)
            return null
        } finally {
            reader?.close()
            connection?.disconnect()
        }
    }

    override fun deliverResult(data: List<News>?) {
        cachedData = data
        if (isStarted) {
            super.deliverResult(data)
        }
    }

    override fun onStopLoading() {
        // Attempt to cancel the current load task if possible
        cancelLoad()
    }

    override fun onReset() {
        super.onReset()
        onStopLoading()
        cachedData = null
    }

    companion object {
        private const val TAG = "NewsLoader"
    }
}