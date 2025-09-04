package com.example.firstapplication

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.firstapplication.databinding.ActivityMainBinding
import com.example.firstapplication.db.SmsMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 액션바 제목 가운데 정렬
        supportActionBar?.let { actionBar ->
            actionBar.setDisplayShowTitleEnabled(false)
            actionBar.setDisplayShowCustomEnabled(true)
            
            val customView = layoutInflater.inflate(R.layout.action_bar_title, null)
            val params = androidx.appcompat.app.ActionBar.LayoutParams(
                androidx.appcompat.app.ActionBar.LayoutParams.MATCH_PARENT,
                androidx.appcompat.app.ActionBar.LayoutParams.MATCH_PARENT
            )
            params.gravity = android.view.Gravity.CENTER
            actionBar.setCustomView(customView, params)
        }
        
        // 첫 실행 시 기존 SMS 파싱
        Log.d("MainActivity", "First run check: ${isFirstRun()}")
        if (isFirstRun()) {
            Log.d("MainActivity", "Starting first run SMS parsing")
            parseExistingSmsMessages()
            markFirstRunCompleted()
        } else {
            Log.d("MainActivity", "Not first run, skipping SMS parsing")
        }
    }

    private fun isFirstRun(): Boolean {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        return prefs.getBoolean("is_first_run", true)
    }

    private fun markFirstRunCompleted() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        prefs.edit().putBoolean("is_first_run", false).apply()
    }

    private fun parseExistingSmsMessages() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MainActivity", "SMS read permission not granted")
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val smsMessages = readSmsMessages()
                val application = applicationContext as PaymentsApplication
                val smsRepository = application.smsRepository
                val database = application.database
                val parsingService = SmsParsingService(database)
                
                var processedCount = 0
                var paymentCount = 0
                
                for (sms in smsMessages) {
                    // SMS 메시지를 데이터베이스에 저장
                    smsRepository.insert(sms)
                    
                    // 카드 결제 관련 SMS인지 확인하고 파싱
                    if (sms.messageBody.contains("카드")) {
                        parsingService.parsePaymentSms(sms.messageBody) { result ->
                            if (result != null) {
                                paymentCount++
                                Log.d("MainActivity", "Payment parsed from existing SMS: ${result.cardName} - ${result.amount}원")
                            }
                        }
                    }
                    processedCount++
                }
                
                withContext(Dispatchers.Main) {
                    Log.i("MainActivity", "Processed $processedCount existing SMS messages, found $paymentCount payments")
                }
                
            } catch (e: Exception) {
                Log.e("MainActivity", "Error parsing existing SMS messages", e)
            }
        }
    }

    private fun readSmsMessages(): List<SmsMessage> {
        val smsMessages = mutableListOf<SmsMessage>()
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
                    
                    // 카드 관련 SMS만 필터링 (성능 최적화)
                    if (body.contains("카드") && (body.contains("결제") || body.contains("승인") || body.contains("사용"))) {
                        smsMessages.add(
                            SmsMessage(
                                sender = sender,
                                messageBody = body,
                                receivedDate = date,
                                isProcessed = false
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error reading SMS messages", e)
        }
        
        return smsMessages
    }

    fun refreshFirstFragment() {
        // FragmentContainerView에서 FirstFragment를 찾아서 데이터를 새로고침
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
        val currentFragment = navHostFragment?.childFragmentManager?.fragments?.firstOrNull()
        if (currentFragment is FirstFragment) {
            currentFragment.refreshData()
        }
    }
}