package com.example.firstapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModelProvider
import com.example.firstapplication.databinding.ActivitySettingsBinding
import com.example.firstapplication.db.SmsPattern
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var smsPermissionLauncher: ActivityResultLauncher<String>

    private val createCsvLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let { exportData(it, "CSV") }
    }
    
    private val createJsonLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { exportData(it, "JSON") }
    }
    
    private val createTxtLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        uri?.let { exportData(it, "TXT") }
    }
    
    private val createExcelLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        uri?.let { exportData(it, "EXCEL") }
    }
    
    private val createPdfLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let { exportData(it, "PDF") }
    }
    
    private val createMdLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/markdown")
    ) { uri ->
        uri?.let { exportData(it, "MD") }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // SMS 권한 요청 런처 초기화
        smsPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // 권한이 승인되면 SMS 파싱 실행
                performSmsReparse(clearExistingData = true)
            } else {
                Toast.makeText(this, "SMS 읽기 권한이 거부되어 기능을 사용할 수 없습니다", Toast.LENGTH_LONG).show()
            }
        }

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
            showFileFormatSelectionDialog()
        }
        
        binding.buttonReparseSms.setOnClickListener {
            clearTestDataAndReparseSms()
        }
        
        // 패턴 최적화 - 46개를 5개로 줄임
        settingsViewModel.resetPatternsToOptimizedSet()
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


    private fun showFileFormatSelectionDialog() {
        val formats = arrayOf("CSV", "JSON", "TXT", "Excel (XLSX)", "PDF", "Markdown (MD)")
        val descriptions = arrayOf(
            "엑셀에서 열기 가능한 표 형태",
            "개발용 데이터 형식",
            "간단한 텍스트 파일",
            "마이크로소프트 엑셀 파일",
            "인쇄용 PDF 문서",
            "GitHub 스타일 마크다운 문서"
        )
        
        AlertDialog.Builder(this)
            .setTitle("내보낼 파일 형식 선택")
            .setItems(formats) { _, which ->
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                when (which) {
                    0 -> createCsvLauncher.launch("payment_data_$timestamp.csv")
                    1 -> createJsonLauncher.launch("payment_data_$timestamp.json")
                    2 -> createTxtLauncher.launch("payment_data_$timestamp.txt")
                    3 -> createExcelLauncher.launch("payment_data_$timestamp.xlsx")
                    4 -> createPdfLauncher.launch("payment_data_$timestamp.pdf")
                    5 -> createMdLauncher.launch("payment_data_$timestamp.md")
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun exportData(uri: Uri, format: String) {
        settingsViewModel.exportToFile(uri, contentResolver, format) { success ->
            runOnUiThread {
                if (success) {
                    Toast.makeText(this, "$format 파일이 성공적으로 내보내졌습니다", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "내보내기 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun clearTestDataAndReparseSms() {
        AlertDialog.Builder(this)
            .setTitle("데이터 초기화 및 SMS 파싱")
            .setMessage("기존의 모든 결제 데이터를 삭제하고 SMS에서 새로 파싱한 데이터만 남깁니다.\n\n이 작업은 되돌릴 수 없습니다. 계속하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                reparseExistingSms(clearExistingData = true)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun reparseExistingSms(clearExistingData: Boolean = false) {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED -> {
                // 권한이 이미 있으면 바로 실행
                performSmsReparse(clearExistingData)
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS) -> {
                // 권한 요청 이유 설명
                AlertDialog.Builder(this)
                    .setTitle("SMS 읽기 권한 필요")
                    .setMessage("기존 SMS에서 결제 내역을 찾기 위해 SMS 읽기 권한이 필요합니다.\n\n이 권한은 오직 카드 결제 관련 SMS를 찾아 파싱하는 용도로만 사용됩니다.")
                    .setPositiveButton("권한 허용") { _, _ ->
                        smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
                    }
                    .setNegativeButton("취소", null)
                    .show()
            }
            else -> {
                // 권한 요청
                smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
            }
        }
    }

    private fun performSmsReparse(clearExistingData: Boolean = false) {
        val messageText = if (clearExistingData) "기존 데이터를 삭제하고 SMS 파싱을 시작합니다..." else "기존 SMS 파싱을 시작합니다..."
        Toast.makeText(this, messageText, Toast.LENGTH_SHORT).show()
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val application = applicationContext as PaymentsApplication
                val smsRepository = application.smsRepository
                val database = application.database
                val parsingService = SmsParsingService(database)
                
                // 기존 데이터 삭제 요청 시 모든 결제 데이터 삭제
                if (clearExistingData) {
                    Log.d("SettingsActivity", "Clearing all existing payment data...")
                    database.paymentDao().deleteAll()
                    Log.d("SettingsActivity", "All existing payment data cleared")
                    
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@SettingsActivity, "기존 결제 데이터가 모두 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                
                val smsMessages = readSmsMessages()
                var processedCount = 0
                var paymentCount = 0
                
                for (sms in smsMessages) {
                    // SMS 메시지를 데이터베이스에 저장 (중복 무시)
                    try {
                        smsRepository.insert(sms)
                    } catch (e: Exception) {
                        // 중복일 경우 무시
                    }
                    
                    // 카드 결제 관련 SMS인지 확인하고 파싱
                    if (sms.messageBody.contains("카드")) {
                        parsingService.parsePaymentSms(sms.messageBody) { result ->
                            if (result != null) {
                                paymentCount++
                                Log.d("SettingsActivity", "Payment parsed: ${result.cardName} - ${result.amount}원 (${result.merchant})")
                            }
                        }
                    }
                    processedCount++
                }
                
                withContext(Dispatchers.Main) {
                    val resultMessage = if (clearExistingData) {
                        "데이터 초기화 완료!\n$processedCount 개의 SMS를 처리했습니다.\n$paymentCount 개의 새로운 결제 내역을 발견했습니다."
                    } else {
                        "$processedCount 개의 SMS를 처리했습니다.\n$paymentCount 개의 결제 내역을 발견했습니다."
                    }
                    Toast.makeText(this@SettingsActivity, resultMessage, Toast.LENGTH_LONG).show()
                    Log.i("SettingsActivity", "SMS parsing completed: $processedCount SMS processed, $paymentCount payments found, clearExistingData: $clearExistingData")
                    
                    // 데이터가 초기화되었으면 MainActivity에 알림
                    if (clearExistingData) {
                        setResult(RESULT_OK)
                    }
                }
                
            } catch (e: Exception) {
                Log.e("SettingsActivity", "Error parsing SMS messages", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SettingsActivity, "SMS 파싱 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun readSmsMessages(): List<com.example.firstapplication.db.SmsMessage> {
        val smsMessages = mutableListOf<com.example.firstapplication.db.SmsMessage>()
        val uri: Uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf("address", "body", "date")
        
        try {
            val cursor: Cursor? = contentResolver.query(uri, projection, null, null, "date DESC LIMIT 1000")
            cursor?.use {
                val addressIndex = it.getColumnIndexOrThrow("address")
                val bodyIndex = it.getColumnIndexOrThrow("body")
                val dateIndex = it.getColumnIndexOrThrow("date")
                
                while (it.moveToNext()) {
                    val sender = it.getString(addressIndex) ?: ""
                    val body = it.getString(bodyIndex) ?: ""
                    val date = Date(it.getLong(dateIndex))
                    
                    Log.d("SettingsActivity", "SMS from $sender: $body")
                    
                    // 모든 SMS를 보되, 카드 관련 키워드가 있는지 확인
                    smsMessages.add(
                        com.example.firstapplication.db.SmsMessage(
                            sender = sender,
                            messageBody = body,
                            receivedDate = date,
                            isProcessed = false
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("SettingsActivity", "Error reading SMS messages", e)
        }
        
        Log.d("SettingsActivity", "Found ${smsMessages.size} SMS messages")
        return smsMessages
    }

}