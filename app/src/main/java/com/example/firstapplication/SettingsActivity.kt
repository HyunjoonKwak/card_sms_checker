package com.example.firstapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstapplication.databinding.ActivitySettingsBinding
import com.example.firstapplication.db.SmsPattern
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var patternAdapter: PatternAdapter
    private lateinit var settingsViewModel: SettingsViewModel

    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let { exportData(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "설정"

        val repository = (application as PaymentsApplication).repository
        val smsRepository = (application as PaymentsApplication).smsRepository
        val database = (application as PaymentsApplication).database

        settingsViewModel = ViewModelProvider(
            this,
            SettingsViewModelFactory(repository, smsRepository, database.smsPatternDao())
        )[SettingsViewModel::class.java]

        setupRecyclerView()
        setupButtons()
        observeData()
        loadDefaultPatterns()
    }

    private fun setupRecyclerView() {
        patternAdapter = PatternAdapter { pattern ->
            showEditPatternDialog(pattern)
        }
        binding.recyclerviewPatterns.adapter = patternAdapter
        binding.recyclerviewPatterns.layoutManager = LinearLayoutManager(this)
    }

    private fun setupButtons() {
        binding.buttonAddPattern.setOnClickListener {
            showAddPatternDialog()
        }

        binding.buttonExportData.setOnClickListener {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            createDocumentLauncher.launch("payment_data_$timestamp.csv")
        }

        binding.buttonBillingCycles.setOnClickListener {
            startActivity(Intent(this, BillingCycleActivity::class.java))
        }
    }

    private fun observeData() {
        settingsViewModel.allPatterns.observe(this) { patterns ->
            patterns?.let { patternAdapter.submitList(it) }
        }
    }

    private fun loadDefaultPatterns() {
        settingsViewModel.addDefaultPatternsIfEmpty()
    }

    private fun showAddPatternDialog() {
        showPatternDialog(null)
    }

    private fun showEditPatternDialog(pattern: SmsPattern) {
        showPatternDialog(pattern)
    }

    private fun showPatternDialog(existingPattern: SmsPattern?) {
        val dialog = PatternEditDialog(this) { name, cardPattern, amountPattern, description ->
            if (existingPattern != null) {
                val updatedPattern = existingPattern.copy(
                    name = name,
                    cardNamePattern = cardPattern,
                    amountPattern = amountPattern,
                    description = description
                )
                settingsViewModel.update(updatedPattern)
            } else {
                val newPattern = SmsPattern(
                    name = name,
                    cardNamePattern = cardPattern,
                    amountPattern = amountPattern,
                    description = description
                )
                settingsViewModel.insert(newPattern)
            }
        }
        
        if (existingPattern != null) {
            dialog.setPattern(existingPattern)
        }
        
        dialog.show()
    }

    private fun exportData(uri: Uri) {
        settingsViewModel.exportToCSV(uri, contentResolver) { success ->
            runOnUiThread {
                if (success) {
                    Toast.makeText(this, "데이터가 성공적으로 내보내졌습니다", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "내보내기 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}