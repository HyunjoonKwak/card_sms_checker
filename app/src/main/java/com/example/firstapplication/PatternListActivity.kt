package com.example.firstapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstapplication.databinding.ActivityPatternListBinding
import com.example.firstapplication.db.SmsPattern

class PatternListActivity : ComponentActivity() {

    private lateinit var binding: ActivityPatternListBinding
    private lateinit var patternListAdapter: PatternListAdapter
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatternListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup back button
        binding.buttonBack.setOnClickListener {
            finish()
        }

        val repository = (application as PaymentsApplication).repository
        val smsRepository = (application as PaymentsApplication).smsRepository
        val database = (application as PaymentsApplication).database

        settingsViewModel = ViewModelProvider(
            this,
            SettingsViewModelFactory(repository, smsRepository, database.smsPatternDao())
        )[SettingsViewModel::class.java]

        setupRecyclerView()
        observeData()
        loadDefaultPatterns()
    }

    private fun setupRecyclerView() {
        patternListAdapter = PatternListAdapter(
            onPatternClick = { pattern ->
                showPatternDetail(pattern)
            },
            onPatternLongClick = { pattern ->
                showDeleteConfirmation(pattern)
            },
            onPatternDeleteClick = { pattern ->
                showDeleteConfirmation(pattern)
            }
        )
        binding.recyclerviewPatterns.adapter = patternListAdapter
        binding.recyclerviewPatterns.layoutManager = LinearLayoutManager(this)
    }

    private fun showPatternDetail(pattern: SmsPattern) {
        val intent = Intent(this, PatternDetailActivity::class.java).apply {
            putExtra("pattern_id", pattern.id)
            putExtra("pattern_name", pattern.name)
            putExtra("pattern_card_pattern", pattern.cardNamePattern)
            putExtra("pattern_amount_pattern", pattern.amountPattern)
            putExtra("pattern_description", pattern.description)
        }
        startActivity(intent)
    }

    private fun observeData() {
        settingsViewModel.allPatterns.observe(this) { patterns ->
            patterns?.let { 
                patternListAdapter.submitList(it)
                binding.textviewPatternCount.text = "${it.size}개의 패턴이 등록되어 있습니다"
            }
        }
    }

    private fun loadDefaultPatterns() {
        settingsViewModel.addDefaultPatternsIfEmpty()
    }

    private fun showDeleteConfirmation(pattern: SmsPattern) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("패턴 삭제")
            .setMessage("'${pattern.name}' 패턴을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                settingsViewModel.delete(pattern)
                Toast.makeText(this, "패턴이 삭제되었습니다", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("취소", null)
            .show()
    }
}