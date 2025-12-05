package com.kelompok10.eeducation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MateriAdapter(
    private val materiList: List<Materi>,
    private val onItemClick: (Materi) -> Unit
) : RecyclerView.Adapter<MateriAdapter.MateriViewHolder>() {

    inner class MateriViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvIcon: TextView = itemView.findViewById(R.id.tvIcon)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        fun bind(materi: Materi) {
            tvIcon.text = materi.icon
            tvTitle.text = materi.title
            tvDescription.text = materi.description
            tvDuration.text = "⏱️ ${materi.duration}"

            // Show checkmark if completed
            tvStatus.visibility = if (materi.isCompleted) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                onItemClick(materi)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MateriViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_materi, parent, false)
        return MateriViewHolder(view)
    }

    override fun onBindViewHolder(holder: MateriViewHolder, position: Int) {
        holder.bind(materiList[position])
    }

    override fun getItemCount(): Int = materiList.size
}