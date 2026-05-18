package com.pigeonnest.presentation.familylist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pigeonnest.databinding.ItemFamilyBinding
import com.pigeonnest.domain.model.FamilyGroup

class FamilyDiffCallback : DiffUtil.ItemCallback<FamilyGroup>() {
    override fun areItemsTheSame(oldItem: FamilyGroup, newItem: FamilyGroup): Boolean =
        oldItem.rootPigeonId == newItem.rootPigeonId

    override fun areContentsTheSame(oldItem: FamilyGroup, newItem: FamilyGroup): Boolean =
        oldItem == newItem
}

class FamilyListAdapter(
    private val onItemClick: (FamilyGroup) -> Unit,
    private val onEditClick: (FamilyGroup) -> Unit
) : ListAdapter<FamilyGroup, FamilyListAdapter.ViewHolder>(FamilyDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFamilyBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemFamilyBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(family: FamilyGroup) {
            binding.textFamilyName.text = family.displayName
            binding.textColors.text = "品种: ${family.colorDisplay}"
            binding.textStats.text = "共 ${family.totalCount} 羽 · 雄 ${family.maleCount} · 雌 ${family.femaleCount}"
            binding.root.setOnClickListener { onItemClick(family) }
            binding.buttonEdit.setOnClickListener { onEditClick(family) }
        }
    }
}
