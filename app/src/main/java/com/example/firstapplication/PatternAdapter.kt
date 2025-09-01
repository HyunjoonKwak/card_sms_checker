package com.example.firstapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapplication.databinding.ListItemPatternBinding
import com.example.firstapplication.db.SmsPattern

class PatternAdapter(
    private val onPatternClick: (SmsPattern) -> Unit
) : ListAdapter<SmsPattern, PatternAdapter.PatternViewHolder>(PatternComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatternViewHolder {
        return PatternViewHolder(
            ListItemPatternBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onPatternClick
        )
    }

    override fun onBindViewHolder(holder: PatternViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class PatternViewHolder(
        private val binding: ListItemPatternBinding,
        private val onPatternClick: (SmsPattern) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(pattern: SmsPattern) {
            binding.textviewPatternName.text = pattern.name
            binding.textviewPatternDescription.text = pattern.description.ifEmpty { "설명 없음" }
            binding.textviewCardPattern.text = "카드명: ${pattern.cardNamePattern}"
            binding.textviewAmountPattern.text = "금액: ${pattern.amountPattern}"
            binding.switchPatternActive.isChecked = pattern.isActive
            
            binding.root.setOnClickListener {
                onPatternClick(pattern)
            }
            
            binding.switchPatternActive.setOnCheckedChangeListener { _, isChecked ->
                // This would need to be handled by the parent to update the database
                // For now, we'll just update the UI
            }
        }
    }

    class PatternComparator : DiffUtil.ItemCallback<SmsPattern>() {
        override fun areItemsTheSame(oldItem: SmsPattern, newItem: SmsPattern): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SmsPattern, newItem: SmsPattern): Boolean {
            return oldItem == newItem
        }
    }
}