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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstapplication.databinding.FragmentFirstBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

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

        val adapter = PaymentListAdapter()
        binding.recyclerviewPayments.adapter = adapter
        binding.recyclerviewPayments.layoutManager = LinearLayoutManager(context)

        paymentViewModel.allPayments.observe(viewLifecycleOwner) { payments ->
            payments?.let {
                adapter.submitList(it)

                val calendar = Calendar.getInstance()
                val yearMonthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
                val currentYearMonth = yearMonthFormat.format(calendar.time)
                val currentYear = yearFormat.format(calendar.time)

                val monthTotal = it.filter { payment ->
                    yearMonthFormat.format(payment.paymentDate) == currentYearMonth
                }.sumOf { payment -> payment.amount }

                val yearTotal = it.filter { payment ->
                    yearFormat.format(payment.paymentDate) == currentYear
                }.sumOf { payment -> payment.amount }

                binding.textviewMonthTotal.text = String.format("이번 달 총 결제 예정액: %,.0f원", monthTotal)
                binding.textviewYearTotal.text = String.format("올해 총 결제 예정액: %,.0f원", yearTotal)
            }
        }

        binding.buttonViewSms.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        requestSmsPermission()
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
