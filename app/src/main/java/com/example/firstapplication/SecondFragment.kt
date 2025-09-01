package com.example.firstapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstapplication.databinding.FragmentSecondBinding
import com.google.android.material.snackbar.Snackbar

/**
 * SMS 기록을 보여주는 Fragment
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    private val smsViewModel: SmsViewModel by viewModels {
        SmsViewModelFactory((activity?.application as PaymentsApplication).smsRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = SmsListAdapter()
        binding.recyclerviewSms.adapter = adapter
        binding.recyclerviewSms.layoutManager = LinearLayoutManager(context)

        smsViewModel.allSmsMessages.observe(viewLifecycleOwner) { smsMessages ->
            smsMessages?.let { adapter.submitList(it) }
        }

        binding.buttonBackToPayments.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        binding.buttonClearSms.setOnClickListener {
            smsViewModel.deleteAll()
            Snackbar.make(view, "SMS 기록이 삭제되었습니다", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}