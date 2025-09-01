package com.example.firstapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapplication.databinding.ListItemCardSummaryBinding

data class CardSummary(
    val cardName: String,
    val totalAmount: Double,
    val transactionCount: Int
)

class CardSummaryAdapter : ListAdapter<CardSummary, CardSummaryAdapter.CardSummaryViewHolder>(CardSummaryComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardSummaryViewHolder {
        return CardSummaryViewHolder(
            ListItemCardSummaryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CardSummaryViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class CardSummaryViewHolder(private val binding: ListItemCardSummaryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cardSummary: CardSummary) {
            binding.textviewCardName.text = cardSummary.cardName
            binding.textviewTotalAmount.text = String.format("%,d원", cardSummary.totalAmount.toInt())
            binding.textviewCount.text = "(${cardSummary.transactionCount}건)"
        }
    }

    class CardSummaryComparator : DiffUtil.ItemCallback<CardSummary>() {
        override fun areItemsTheSame(oldItem: CardSummary, newItem: CardSummary): Boolean {
            return oldItem.cardName == newItem.cardName
        }

        override fun areContentsTheSame(oldItem: CardSummary, newItem: CardSummary): Boolean {
            return oldItem == newItem
        }
    }
}