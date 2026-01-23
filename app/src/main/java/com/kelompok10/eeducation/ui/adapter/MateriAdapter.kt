package com.kelompok10.eeducation.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kelompok10.eeducation.R
import com.kelompok10.eeducation.data.local.Materi

class MateriAdapter(
    private val onItemClick: (Materi) -> Unit
) : RecyclerView.Adapter<MateriAdapter.MateriViewHolder>() {

    private var materiList = listOf<Materi>()

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

    fun updateData(newMateriList: List<Materi>) {
        val diffCallback = MateriDiffCallback(materiList, newMateriList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        materiList = newMateriList
        diffResult.dispatchUpdatesTo(this)
    }

    class MateriDiffCallback(
        private val oldList: List<Materi>,
        private val newList: List<Materi>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}