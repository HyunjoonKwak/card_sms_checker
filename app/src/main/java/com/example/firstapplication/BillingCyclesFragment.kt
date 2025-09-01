package com.example.firstapplication

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstapplication.databinding.FragmentBillingCyclesBinding
import com.example.firstapplication.db.CardBillingCycle
import com.example.firstapplication.db.DefaultBillingCycles
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BillingCyclesFragment : Fragment() {

    private var _binding: FragmentBillingCyclesBinding? = null
    private val binding get() = _binding!!

    private val billingCycleViewModel: BillingCycleViewModel by viewModels {
        BillingCycleViewModelFactory((activity?.application as PaymentsApplication).repository)
    }

    private lateinit var adapter: BillingCycleAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBillingCyclesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = BillingCycleAdapter(
            onEditClick = { billingCycle ->
                editBillingCycle(billingCycle)
            },
            onDeleteClick = { billingCycle ->
                deleteBillingCycle(billingCycle)
            },
            onActiveToggle = { billingCycle, isActive ->
                toggleBillingCycle(billingCycle, isActive)
            }
        )

        binding.recyclerviewBillingCycles.adapter = adapter
        binding.recyclerviewBillingCycles.layoutManager = LinearLayoutManager(context)

        billingCycleViewModel.allBillingCycles.observe(viewLifecycleOwner) { cycles ->
            cycles?.let {
                adapter.submitList(it)
                if (it.isEmpty()) {
                    initializeDefaultBillingCycles()
                }
            }
        }

        binding.buttonAddCard.setOnClickListener {
            addBillingCycle()
        }

        binding.edittextBillingDay.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                updateCutoffDay()
            }
        }
    }

    private fun initializeDefaultBillingCycles() {
        CoroutineScope(Dispatchers.IO).launch {
            billingCycleViewModel.insertDefaultBillingCycles()
        }
    }

    private fun addBillingCycle() {
        val cardName = binding.edittextCardName.text.toString().trim()
        val bankName = binding.edittextBankName.text.toString().trim()
        val billingDayText = binding.edittextBillingDay.text.toString().trim()
        val cutoffDayText = binding.edittextCutoffDay.text.toString().trim()

        if (cardName.isEmpty() || bankName.isEmpty() || billingDayText.isEmpty()) {
            Toast.makeText(context, "카드명, 은행명, 결제일을 모두 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        val billingDay = billingDayText.toIntOrNull()
        if (billingDay == null || billingDay < 1 || billingDay > 31) {
            Toast.makeText(context, "결제일은 1-31 사이의 숫자여야 합니다", Toast.LENGTH_SHORT).show()
            return
        }

        val cutoffDay = if (cutoffDayText.isEmpty()) {
            if (billingDay <= 7) billingDay + 23 else billingDay - 7
        } else {
            cutoffDayText.toIntOrNull() ?: return
        }

        val billingCycle = CardBillingCycle(
            cardName = cardName,
            bankName = bankName,
            billingDay = billingDay,
            cutoffDay = cutoffDay,
            isActive = true
        )

        CoroutineScope(Dispatchers.IO).launch {
            billingCycleViewModel.insert(billingCycle)
        }

        clearInputs()
        Toast.makeText(context, "카드가 추가되었습니다", Toast.LENGTH_SHORT).show()
    }

    private fun editBillingCycle(billingCycle: CardBillingCycle) {
        binding.edittextCardName.setText(billingCycle.cardName)
        binding.edittextBankName.setText(billingCycle.bankName)
        binding.edittextBillingDay.setText(billingCycle.billingDay.toString())
        binding.edittextCutoffDay.setText(billingCycle.cutoffDay.toString())
    }

    private fun deleteBillingCycle(billingCycle: CardBillingCycle) {
        AlertDialog.Builder(requireContext())
            .setTitle("카드 삭제")
            .setMessage("${billingCycle.cardName}를 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    billingCycleViewModel.delete(billingCycle)
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun toggleBillingCycle(billingCycle: CardBillingCycle, isActive: Boolean) {
        val updatedCycle = billingCycle.copy(isActive = isActive)
        CoroutineScope(Dispatchers.IO).launch {
            billingCycleViewModel.update(updatedCycle)
        }
    }

    private fun updateCutoffDay() {
        val billingDayText = binding.edittextBillingDay.text.toString().trim()
        val billingDay = billingDayText.toIntOrNull()
        if (billingDay != null && billingDay in 1..31) {
            val cutoffDay = if (billingDay <= 7) billingDay + 23 else billingDay - 7
            binding.edittextCutoffDay.setText(cutoffDay.toString())
        }
    }

    private fun clearInputs() {
        binding.edittextCardName.text?.clear()
        binding.edittextBankName.text?.clear()
        binding.edittextBillingDay.text?.clear()
        binding.edittextCutoffDay.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}