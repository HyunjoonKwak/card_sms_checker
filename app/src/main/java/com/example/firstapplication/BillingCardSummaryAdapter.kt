package com.example.firstapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapplication.databinding.ListItemCardSummaryBinding
import com.example.firstapplication.db.CardBillingSummary

class BillingCardSummaryAdapter : ListAdapter<CardBillingSummary, BillingCardSummaryAdapter.BillingCardSummaryViewHolder>(BillingCardSummaryComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillingCardSummaryViewHolder {
        return BillingCardSummaryViewHolder(
            ListItemCardSummaryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BillingCardSummaryViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class BillingCardSummaryViewHolder(private val binding: ListItemCardSummaryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cardSummary: CardBillingSummary) {
            binding.textviewCardName.text = cardSummary.cardName
            binding.textviewBankName.text = cardSummary.bankName ?: "알 수 없음"
            binding.textviewTotalAmount.text = String.format("%,d원", cardSummary.totalAmount.toInt())
            binding.textviewCount.text = "(${cardSummary.transactionCount}건)"
            
            val billingInfo = if (cardSummary.billingDay != null && cardSummary.cutoffDay != null) {
                "결제일: ${cardSummary.billingDay}일 | 마감일: ${cardSummary.cutoffDay}일"
            } else {
                "결제 정보 없음"
            }
            binding.textviewBillingInfo.text = billingInfo
        }
    }

    class BillingCardSummaryComparator : DiffUtil.ItemCallback<CardBillingSummary>() {
        override fun areItemsTheSame(oldItem: CardBillingSummary, newItem: CardBillingSummary): Boolean {
            return oldItem.cardName == newItem.cardName
        }

        override fun areContentsTheSame(oldItem: CardBillingSummary, newItem: CardBillingSummary): Boolean {
            return oldItem == newItem
        }
    }
}