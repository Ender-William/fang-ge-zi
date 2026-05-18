package com.pigeonnest.presentation.locationset

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pigeonnest.databinding.ItemLoftSelectBinding
import com.pigeonnest.domain.model.Loft

class LoftSelectDiffCallback : DiffUtil.ItemCallback<Loft>() {
    override fun areItemsTheSame(oldItem: Loft, newItem: Loft): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Loft, newItem: Loft): Boolean =
        oldItem == newItem
}

class LoftSelectAdapter(
    private val onItemClick: (Loft) -> Unit
) : ListAdapter<Loft, LoftSelectAdapter.ViewHolder>(LoftSelectDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLoftSelectBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemLoftSelectBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(loft: Loft) {
            binding.textName.text = loft.name
            binding.textInfo.text = "${loft.pigeonCount}只鸽子" +
                (loft.capacity?.let { " / 容量${it}只" } ?: "")

            binding.root.setOnClickListener { onItemClick(loft) }
        }
    }
}
