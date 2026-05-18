package com.pigeonnest.presentation.loftmanage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pigeonnest.databinding.ItemLoftBinding
import com.pigeonnest.domain.model.Loft

class LoftDiffCallback : DiffUtil.ItemCallback<Loft>() {
    override fun areItemsTheSame(oldItem: Loft, newItem: Loft): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Loft, newItem: Loft): Boolean =
        oldItem == newItem
}

class LoftListAdapter(
    private val onItemClick: (Loft) -> Unit,
    private val onItemLongClick: (Loft) -> Unit
) : ListAdapter<Loft, LoftListAdapter.ViewHolder>(LoftDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLoftBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemLoftBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(loft: Loft) {
            binding.textName.text = loft.name
            binding.textInfo.text = "${loft.pigeonCount}只鸽子" +
                (loft.capacity?.let { " / 容量${it}只" } ?: "")

            binding.root.setOnClickListener { onItemClick(loft) }
            binding.root.setOnLongClickListener {
                onItemLongClick(loft)
                true
            }
        }
    }
}
