package com.example.firstapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapplication.databinding.ListItemCardInfoBinding
import com.example.firstapplication.db.CardBillingCycle

class CardListAdapter(
    private val onCardDeleteClick: (CardBillingCycle) -> Unit,
    private val onActiveToggleClick: (CardBillingCycle) -> Unit
) : ListAdapter<CardBillingCycle, CardListAdapter.CardViewHolder>(CardComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        return CardViewHolder(
            ListItemCardInfoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    inner class CardViewHolder(private val binding: ListItemCardInfoBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(card: CardBillingCycle) {
            binding.textviewCardName.text = card.cardName
            binding.textviewBankName.text = card.bankName
            binding.textviewBillingDay.text = "결제일: ${card.billingDay}일"
            binding.textviewCutoffDay.text = "마감일: ${card.cutoffDay}일"
            binding.textviewIsActive.text = if (card.isActive) "활성화" else "비활성화"
            binding.textviewIsActive.setTextColor(
                if (card.isActive) 
                    binding.root.context.getColor(R.color.primaryColor)
                else 
                    binding.root.context.getColor(android.R.color.darker_gray)
            )
            
            binding.buttonDeleteCard.setOnClickListener {
                onCardDeleteClick(card)
            }
            
            binding.textviewIsActive.setOnClickListener {
                onActiveToggleClick(card)
            }
        }
    }

    class CardComparator : DiffUtil.ItemCallback<CardBillingCycle>() {
        override fun areItemsTheSame(oldItem: CardBillingCycle, newItem: CardBillingCycle): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CardBillingCycle, newItem: CardBillingCycle): Boolean {
            return oldItem == newItem
        }
    }
}