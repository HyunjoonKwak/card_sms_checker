package com.example.firstapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.firstapplication.databinding.ActivitySettingsBinding
import com.example.firstapplication.db.SmsPattern
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {

    private lateinit var binding: ActivitySettingsBinding
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

        setupButtons()
    }


    private fun setupButtons() {
        binding.buttonAddPattern.setOnClickListener {
            addSimplePattern()
        }

        binding.buttonViewPatterns.setOnClickListener {
            val intent = Intent(this, PatternListActivity::class.java)
            startActivity(intent)
        }

        binding.buttonSaveCard.setOnClickListener {
            saveCardInformation()
        }

        binding.buttonViewCards.setOnClickListener {
            val intent = Intent(this, CardListActivity::class.java)
            startActivity(intent)
        }

        binding.buttonExportData.setOnClickListener {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            createDocumentLauncher.launch("payment_data_$timestamp.csv")
        }
    }

    private fun addSimplePattern() {
        val patternText = binding.edittextPattern.text.toString().trim()
        if (patternText.isEmpty()) {
            Toast.makeText(this, "패턴을 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        val newPattern = SmsPattern(
            name = patternText,
            cardNamePattern = patternText,
            amountPattern = "\\d{1,3}(,\\d{3})*원",
            description = "사용자 추가 패턴"
        )
        
        settingsViewModel.insert(newPattern)
        binding.edittextPattern.text.clear()
        Toast.makeText(this, "패턴이 추가되었습니다", Toast.LENGTH_SHORT).show()
    }

    private fun saveCardInformation() {
        val cardName = binding.edittextCardName.text.toString().trim()
        val bankName = binding.edittextBankName.text.toString().trim()
        val billingDayText = binding.edittextBillingDay.text.toString().trim()
        val cutoffDayText = binding.edittextCutoffDay.text.toString().trim()

        if (cardName.isEmpty()) {
            Toast.makeText(this, "카드명을 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        val billingDay = billingDayText.toIntOrNull()
        val cutoffDay = cutoffDayText.toIntOrNull()

        if (billingDay == null || billingDay < 1 || billingDay > 31) {
            Toast.makeText(this, "올바른 결제일을 입력해주세요 (1-31)", Toast.LENGTH_SHORT).show()
            return
        }

        if (cutoffDay == null || cutoffDay < 1 || cutoffDay > 31) {
            Toast.makeText(this, "올바른 마감일을 입력해주세요 (1-31)", Toast.LENGTH_SHORT).show()
            return
        }

        val billingCycle = com.example.firstapplication.db.CardBillingCycle(
            cardName = cardName,
            bankName = if (bankName.isNotEmpty()) bankName else "미지정",
            billingDay = billingDay,
            cutoffDay = cutoffDay,
            isActive = true
        )

        // Save billing cycle
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val database = (application as PaymentsApplication).database
                database.cardBillingCycleDao().insert(billingCycle)
                
                runOnUiThread {
                    Toast.makeText(this@SettingsActivity, "카드 정보가 저장되었습니다", Toast.LENGTH_SHORT).show()
                    clearCardFields()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@SettingsActivity, "저장 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun clearCardFields() {
        binding.edittextCardName.text.clear()
        binding.edittextBankName.text.clear()
        binding.edittextBillingDay.text.clear()
        binding.edittextCutoffDay.text.clear()
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

}