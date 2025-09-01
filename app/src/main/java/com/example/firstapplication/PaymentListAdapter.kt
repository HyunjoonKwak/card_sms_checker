package com.example.firstapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapplication.db.CardPayment
import java.text.SimpleDateFormat
import java.util.Locale

class PaymentListAdapter : ListAdapter<CardPayment, PaymentListAdapter.PaymentViewHolder>(PaymentsComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        return PaymentViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardNameItemView: TextView = itemView.findViewById(R.id.textview_card_name)
        private val amountItemView: TextView = itemView.findViewById(R.id.textview_amount)
        private val dateItemView: TextView = itemView.findViewById(R.id.textview_payment_date)

        fun bind(payment: CardPayment) {
            cardNameItemView.text = payment.cardName
            amountItemView.text = String.format("%,d원", payment.amount.toInt())
            val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
            dateItemView.text = sdf.format(payment.paymentDate)
        }

        companion object {
            fun create(parent: ViewGroup): PaymentViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_payment, parent, false)
                return PaymentViewHolder(view)
            }
        }
    }

    class PaymentsComparator : DiffUtil.ItemCallback<CardPayment>() {
        override fun areItemsTheSame(oldItem: CardPayment, newItem: CardPayment): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: CardPayment, newItem: CardPayment): Boolean {
            return oldItem.id == newItem.id
        }
    }
}
