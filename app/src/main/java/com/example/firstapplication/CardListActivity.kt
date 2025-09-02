package com.example.firstapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstapplication.databinding.ActivityCardListBinding
import com.example.firstapplication.db.CardBillingCycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CardListActivity : ComponentActivity() {

    private lateinit var binding: ActivityCardListBinding
    private lateinit var cardListAdapter: CardListAdapter
    private lateinit var billingCycleViewModel: BillingCycleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup back button
        binding.buttonBack.setOnClickListener {
            finish()
        }

        val repository = (application as PaymentsApplication).repository
        billingCycleViewModel = ViewModelProvider(
            this,
            BillingCycleViewModelFactory(repository)
        )[BillingCycleViewModel::class.java]

        setupRecyclerView()
        observeData()
    }

    private fun setupRecyclerView() {
        cardListAdapter = CardListAdapter(
            onCardDeleteClick = { card ->
                showDeleteConfirmation(card)
            },
            onActiveToggleClick = { card ->
                toggleCardActiveStatus(card)
            }
        )
        binding.recyclerviewCards.adapter = cardListAdapter
        binding.recyclerviewCards.layoutManager = LinearLayoutManager(this)
    }

    private fun toggleCardActiveStatus(card: CardBillingCycle) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val updatedCard = card.copy(isActive = !card.isActive)
                billingCycleViewModel.update(updatedCard)
                
                withContext(Dispatchers.Main) {
                    if (!isFinishing) {
                        val statusText = if (updatedCard.isActive) "활성화" else "비활성화"
                        Toast.makeText(this@CardListActivity, "카드가 ${statusText}되었습니다", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (!isFinishing) {
                        Toast.makeText(this@CardListActivity, "상태 변경 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun observeData() {
        billingCycleViewModel.allBillingCycles.observe(this) { cards ->
            cards?.let { 
                cardListAdapter.submitList(it)
                binding.textviewCardCount.text = "${it.size}개의 카드가 등록되어 있습니다"
            }
        }
    }

    private fun showDeleteConfirmation(card: CardBillingCycle) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("카드 정보 삭제")
            .setMessage("'${card.cardName}' 카드 정보를 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                deleteCard(card)
            }
            .setNegativeButton("취소", null)
            .show()
    }
    
    private fun deleteCard(card: CardBillingCycle) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                billingCycleViewModel.delete(card)
                
                withContext(Dispatchers.Main) {
                    if (!isFinishing) {
                        Toast.makeText(this@CardListActivity, "카드 정보가 삭제되었습니다", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (!isFinishing) {
                        Toast.makeText(this@CardListActivity, "삭제 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}