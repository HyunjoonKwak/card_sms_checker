package com.example.firstapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapplication.databinding.ListItemPatternSimpleBinding
import com.example.firstapplication.db.SmsPattern

class PatternListAdapter(
    private val onPatternClick: (SmsPattern) -> Unit,
    private val onPatternLongClick: (SmsPattern) -> Unit,
    private val onPatternDeleteClick: (SmsPattern) -> Unit
) : ListAdapter<SmsPattern, PatternListAdapter.PatternViewHolder>(PatternComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatternViewHolder {
        return PatternViewHolder(
            ListItemPatternSimpleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PatternViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    inner class PatternViewHolder(private val binding: ListItemPatternSimpleBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(pattern: SmsPattern) {
            binding.textviewPatternName.text = pattern.name
            binding.textviewPatternDescription.text = pattern.description ?: "사용자 추가 패턴"
            
            binding.root.setOnClickListener {
                onPatternClick(pattern)
            }
            
            binding.root.setOnLongClickListener {
                onPatternLongClick(pattern)
                true
            }
            
            binding.buttonDeletePattern.setOnClickListener {
                onPatternDeleteClick(pattern)
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