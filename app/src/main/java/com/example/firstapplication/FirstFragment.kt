package com.example.firstapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstapplication.databinding.FragmentFirstBinding
import com.example.firstapplication.db.CardPayment
import android.content.Intent
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private lateinit var billingCardSummaryAdapter: BillingCardSummaryAdapter
    
    // 페이지네이션 관련 변수들
    private val pageSize = 10
    private var currentPage = 0
    private var isLoading = false
    private var hasMoreData = true
    private val currentPayments = mutableListOf<CardPayment>()
    private lateinit var adapter: PaymentListAdapter

    private val paymentViewModel: PaymentViewModel by viewModels {
        PaymentViewModelFactory((activity?.application as PaymentsApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PaymentListAdapter()
        binding.recyclerviewPayments.adapter = adapter
        binding.recyclerviewPayments.layoutManager = LinearLayoutManager(context)
        
        billingCardSummaryAdapter = BillingCardSummaryAdapter()
        binding.recyclerviewCardSummary.adapter = billingCardSummaryAdapter
        binding.recyclerviewCardSummary.layoutManager = LinearLayoutManager(context)

        // 최근 결제 내역 초기 로드
        loadCurrentMonthPayments(loadMore = false)
        
        // 월별, 연간 총계 로드
        loadPaymentSummary()

        binding.buttonToggleCardSummary.setOnClickListener {
            val cardSummaryView = binding.cardviewCardSummary
            val recentPaymentsView = binding.cardviewRecentPayments
            
            if (cardSummaryView.visibility == View.VISIBLE) {
                cardSummaryView.visibility = View.GONE
            } else {
                cardSummaryView.visibility = View.VISIBLE
                recentPaymentsView.visibility = View.GONE
                loadBillingCycleSummaries()
            }
        }

        binding.buttonToggleRecentPayments.setOnClickListener {
            val cardSummaryView = binding.cardviewCardSummary
            val recentPaymentsView = binding.cardviewRecentPayments
            
            if (recentPaymentsView.visibility == View.VISIBLE) {
                recentPaymentsView.visibility = View.GONE
            } else {
                recentPaymentsView.visibility = View.VISIBLE
                cardSummaryView.visibility = View.GONE
            }
        }

        binding.buttonViewSms.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }


        binding.buttonSettings.setOnClickListener {
            val intent = Intent(activity, SettingsActivity::class.java)
            startActivity(intent)
        }
        
        binding.buttonLoadMorePayments.setOnClickListener {
            loadCurrentMonthPayments(loadMore = true)
        }

        requestSmsPermission()
    }

    private fun loadBillingCycleSummaries() {
        CoroutineScope(Dispatchers.IO).launch {
            val repository = (activity?.application as PaymentsApplication).repository
            val allPayments = repository.allPayments.value ?: emptyList()
            
            if (allPayments.isNotEmpty()) {
                // 이번달 결제만 필터링
                val calendar = java.util.Calendar.getInstance()
                val currentYear = calendar.get(java.util.Calendar.YEAR)
                val currentMonth = calendar.get(java.util.Calendar.MONTH)
                
                val thisMonthPayments = allPayments.filter { payment ->
                    val paymentCalendar = java.util.Calendar.getInstance()
                    paymentCalendar.time = payment.paymentDate
                    paymentCalendar.get(java.util.Calendar.YEAR) == currentYear &&
                    paymentCalendar.get(java.util.Calendar.MONTH) == currentMonth
                }
                
                val cardSummaries = thisMonthPayments.groupBy { it.cardName }
                    .map { (cardName, payments) ->
                        com.example.firstapplication.db.CardBillingSummary(
                            cardName = cardName,
                            totalAmount = payments.sumOf { it.amount },
                            transactionCount = payments.size,
                            bankName = null,
                            billingDay = null,
                            cutoffDay = null
                        )
                    }.sortedByDescending { it.totalAmount }
                
                withContext(Dispatchers.Main) {
                    billingCardSummaryAdapter.submitList(cardSummaries)
                }
            }
        }
    }

    private fun loadCurrentMonthPayments(loadMore: Boolean) {
        if (isLoading) return
        
        isLoading = true
        
        lifecycleScope.launch {
            try {
                val calendar = java.util.Calendar.getInstance()
                val yearMonth = String.format("%04d-%02d", 
                    calendar.get(java.util.Calendar.YEAR), 
                    calendar.get(java.util.Calendar.MONTH) + 1)
                
                val repository = (activity?.application as PaymentsApplication).repository
                
                if (!loadMore) {
                    // 첫 로드 시 초기화
                    currentPage = 0
                    currentPayments.clear()
                }
                
                val offset = currentPage * pageSize
                val newPayments = repository.getCurrentMonthPayments(yearMonth, pageSize, offset)
                
                if (newPayments.isNotEmpty()) {
                    currentPayments.addAll(newPayments)
                    adapter.submitList(currentPayments.toList())
                    currentPage++
                    hasMoreData = newPayments.size == pageSize
                } else {
                    hasMoreData = false
                }
                
                // 더 보기 버튼 표시/숨김
                withContext(Dispatchers.Main) {
                    binding.buttonLoadMorePayments.visibility = if (hasMoreData) View.VISIBLE else View.GONE
                }
                
            } catch (e: Exception) {
                // 에러 처리
                withContext(Dispatchers.Main) {
                    binding.buttonLoadMorePayments.visibility = View.GONE
                }
            } finally {
                isLoading = false
            }
        }
    }

    private fun loadPaymentSummary() {
        lifecycleScope.launch {
            try {
                val calendar = Calendar.getInstance()
                val yearMonth = String.format("%04d-%02d", 
                    calendar.get(Calendar.YEAR), 
                    calendar.get(Calendar.MONTH) + 1)
                
                val repository = (activity?.application as PaymentsApplication).repository
                val allPayments = repository.allPayments.value ?: emptyList()
                
                val yearMonthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
                val currentYearMonth = yearMonthFormat.format(calendar.time)
                val currentYear = yearFormat.format(calendar.time)

                val monthTotal = allPayments.filter { payment ->
                    yearMonthFormat.format(payment.paymentDate) == currentYearMonth
                }.sumOf { payment -> payment.amount }

                val yearTotal = allPayments.filter { payment ->
                    yearFormat.format(payment.paymentDate) == currentYear
                }.sumOf { payment -> payment.amount }

                val currentMonthText = SimpleDateFormat("M월", Locale.KOREA).format(calendar.time)
                val currentYearText = SimpleDateFormat("yyyy년", Locale.KOREA).format(calendar.time)
                
                binding.textviewMonthTotal.text = String.format("%s 총 결제: %,d원", currentMonthText, monthTotal.toInt())
                binding.textviewYearTotal.text = String.format("%s 총 결제: %,d원", currentYearText, yearTotal.toInt())
                
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }

    private fun requestSmsPermission() {
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            }

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECEIVE_SMS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
