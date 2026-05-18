package com.pigeonnest.presentation.pigeonlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pigeonnest.R
import com.pigeonnest.data.file.PhotoStorageManager
import com.pigeonnest.databinding.ItemPigeonBinding
import com.pigeonnest.domain.model.Gender
import com.pigeonnest.domain.model.Pigeon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PigeonDiffCallback : DiffUtil.ItemCallback<Pigeon>() {
    override fun areItemsTheSame(oldItem: Pigeon, newItem: Pigeon): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Pigeon, newItem: Pigeon): Boolean =
        oldItem == newItem
}

class PigeonListAdapter(
    private val onItemClick: (Pigeon) -> Unit,
    private val onItemLongClick: (Pigeon) -> Unit
) : ListAdapter<Pigeon, PigeonListAdapter.ViewHolder>(PigeonDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPigeonBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemPigeonBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pigeon: Pigeon) {
            binding.textName.text = pigeon.name
            binding.textRingNumber.text = "脚环号: ${pigeon.ringNumber}"

            val infoParts = mutableListOf<String>()
            pigeon.color?.let { infoParts.add(it) }
            infoParts.add(pigeon.gender.displayName)
            pigeon.birthDate?.let {
                val age = calculateAge(it)
                infoParts.add("${age}岁")
            }
            binding.textInfo.text = infoParts.joinToString(" · ")

            binding.textLocation.text = if (pigeon.loft != null) {
                "${pigeon.loft.name}${pigeon.cageNumber?.let { "-$it" } ?: ""}"
            } else {
                "未分配鸽舍"
            }

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
            binding.root.setOnLongClickListener {
                onItemLongClick(pigeon)
                true
            }
        }

        private fun calculateAge(birthDateMillis: Long): Int {
            val birthYear = SimpleDateFormat("yyyy", Locale.CHINA).format(Date(birthDateMillis)).toInt()
            val currentYear = SimpleDateFormat("yyyy", Locale.CHINA).format(Date()).toInt()
            return maxOf(0, currentYear - birthYear)
        }
    }
}
