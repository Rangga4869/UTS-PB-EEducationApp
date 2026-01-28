package com.kelompok10.eeducation.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kelompok10.eeducation.R
import com.kelompok10.eeducation.data.model.News

class NewsAdapter : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    private var newsList: List<News> = emptyList()

    fun updateData(news: List<News>) {
        newsList = news
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(newsList[position])
    }

    override fun getItemCount(): Int = newsList.size

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvNewsTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvNewsDescription)
        private val tvDate: TextView = itemView.findViewById(R.id.tvNewsDate)

        fun bind(news: News) {
            tvTitle.text = news.judul
            tvDescription.text = news.ringkasan
            tvDate.text = "${news.tanggal} • ${news.penulis}"
        }
    }
}
