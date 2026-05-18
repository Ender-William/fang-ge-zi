package com.pigeonnest.presentation.pigeonlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.pigeonnest.R
import com.pigeonnest.data.file.PhotoStorageManager
import com.pigeonnest.databinding.ItemRecentPigeonBinding
import com.pigeonnest.domain.model.Gender
import com.pigeonnest.domain.model.Pigeon

class RecentPigeonAdapter(
    private val onItemClick: (Pigeon) -> Unit
) : RecyclerView.Adapter<RecentPigeonAdapter.ViewHolder>() {

    private var items: List<Pigeon> = emptyList()

    fun submitList(list: List<Pigeon>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentPigeonBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(
        private val binding: ItemRecentPigeonBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pigeon: Pigeon) {
            binding.textName.text = pigeon.name
            binding.textRing.text = pigeon.ringNumber

            val genderColor = when (pigeon.gender) {
                Gender.MALE -> ContextCompat.getColor(binding.root.context, R.color.male_blue)
                Gender.FEMALE -> ContextCompat.getColor(binding.root.context, R.color.female_pink)
                else -> ContextCompat.getColor(binding.root.context, R.color.neutral_gray)
            }
            binding.imageAvatar.setBorderColor(genderColor)

            PhotoStorageManager(binding.root.context).loadPhoto(
                binding.imageAvatar,
                pigeon.photoPath
            )

            binding.root.setOnClickListener { onItemClick(pigeon) }
        }
    }
}
