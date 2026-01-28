package com.kelompok10.eeducation.ui.news

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kelompok10.eeducation.R
import com.kelompok10.eeducation.data.model.News
import com.kelompok10.eeducation.ui.adapter.NewsAdapter
import com.kelompok10.eeducation.ui.materi.NewsLoader
import com.kelompok10.eeducation.utils.NetworkUtils

class NewsActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<List<News>> {

    private lateinit var rvNews: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private lateinit var btnBack: TextView
    private lateinit var newsAdapter: NewsAdapter

    companion object {
        private const val NEWS_LOADER_ID = 1
        private const val NEWS_URL = "https://raw.githubusercontent.com/Rangga4869/UTS-PB-EEducationApp/refs/heads/dev-faisal/assets/news.json"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)

        initViews()
        setupRecyclerView()
        setupListeners()
        loadNews()
    }

    private fun initViews() {
        rvNews = findViewById(R.id.rvNews)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        rvNews.apply {
            layoutManager = LinearLayoutManager(this@NewsActivity)
            adapter = newsAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
    }

    private fun loadNews() {
        // Check internet connection
        if (!NetworkUtils.isNetworkAvailable(this)) {
            showError("No internet connection. Please check your network settings.")
            return
        }

        // Initialize loader
        LoaderManager.getInstance(this).initLoader(NEWS_LOADER_ID, null, this)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<News>> {
        progressBar.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE
        return NewsLoader(this, NEWS_URL)
    }

    override fun onLoadFinished(loader: Loader<List<News>>, data: List<News>?) {
        progressBar.visibility = View.GONE
        
        if (data.isNullOrEmpty()) {
            showError("No news available")
        } else {
            tvEmptyState.visibility = View.GONE
            newsAdapter.updateData(data)
            Toast.makeText(this, "Loaded ${data.size} news items", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onLoaderReset(loader: Loader<List<News>>) {
        newsAdapter.updateData(emptyList())
    }

    private fun showError(message: String) {
        tvEmptyState.apply {
            text = message
            visibility = View.VISIBLE
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
