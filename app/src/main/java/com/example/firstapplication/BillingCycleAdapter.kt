package com.example.firstapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapplication.databinding.ListItemBillingCycleBinding
import com.example.firstapplication.db.CardBillingCycle

class BillingCycleAdapter(
    private val onEditClick: (CardBillingCycle) -> Unit,
    private val onDeleteClick: (CardBillingCycle) -> Unit,
    private val onActiveToggle: (CardBillingCycle, Boolean) -> Unit
) : ListAdapter<CardBillingCycle, BillingCycleAdapter.BillingCycleViewHolder>(BillingCycleComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillingCycleViewHolder {
        return BillingCycleViewHolder(
            ListItemBillingCycleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BillingCycleViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    inner class BillingCycleViewHolder(private val binding: ListItemBillingCycleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(billingCycle: CardBillingCycle) {
            binding.textviewCardName.text = billingCycle.cardName
            binding.textviewBankName.text = billingCycle.bankName
            binding.textviewBillingDay.text = "결제일: ${billingCycle.billingDay}일"
            binding.textviewCutoffDay.text = "마감일: ${billingCycle.cutoffDay}일"
            binding.switchActive.isChecked = billingCycle.isActive

            binding.buttonEdit.setOnClickListener {
                onEditClick(billingCycle)
            }

            binding.buttonDelete.setOnClickListener {
                onDeleteClick(billingCycle)
            }

            binding.switchActive.setOnCheckedChangeListener { _, isChecked ->
                onActiveToggle(billingCycle, isChecked)
            }
        }
    }

    class BillingCycleComparator : DiffUtil.ItemCallback<CardBillingCycle>() {
        override fun areItemsTheSame(oldItem: CardBillingCycle, newItem: CardBillingCycle): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CardBillingCycle, newItem: CardBillingCycle): Boolean {
            return oldItem == newItem
        }
    }
}