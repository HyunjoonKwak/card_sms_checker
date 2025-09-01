package com.example.firstapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapplication.databinding.ListItemSmsBinding
import com.example.firstapplication.db.SmsMessage
import java.text.SimpleDateFormat
import java.util.Locale

class SmsListAdapter : ListAdapter<SmsMessage, SmsListAdapter.SmsViewHolder>(SmsComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmsViewHolder {
        return SmsViewHolder(
            ListItemSmsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SmsViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class SmsViewHolder(private val binding: ListItemSmsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(smsMessage: SmsMessage) {
            val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
            
            binding.textviewSender.text = smsMessage.sender
            binding.textviewMessage.text = smsMessage.messageBody
            binding.textviewDate.text = dateFormat.format(smsMessage.receivedDate)
            
            // Mark processed SMS with different color
            if (smsMessage.isProcessed) {
                binding.root.alpha = 0.6f
            } else {
                binding.root.alpha = 1.0f
            }
        }
    }

    class SmsComparator : DiffUtil.ItemCallback<SmsMessage>() {
        override fun areItemsTheSame(oldItem: SmsMessage, newItem: SmsMessage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SmsMessage, newItem: SmsMessage): Boolean {
            return oldItem == newItem
        }
    }
}