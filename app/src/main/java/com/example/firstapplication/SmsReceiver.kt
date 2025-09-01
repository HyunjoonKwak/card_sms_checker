package com.example.firstapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.firstapplication.db.AppDatabase
import com.example.firstapplication.db.CardPayment
import com.example.firstapplication.db.SmsMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Date
import java.util.regex.Pattern

class SmsReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (message in messages) {
                val msgBody = message.messageBody
                val sender = message.originatingAddress
                Log.d("SmsReceiver", "Sender: $sender, Message: $msgBody")

                val database = AppDatabase.getDatabase(context)
                
                // Store SMS message in database
                val smsMessage = SmsMessage(
                    sender = sender ?: "Unknown",
                    messageBody = msgBody,
                    receivedDate = Date()
                )
                
                scope.launch {
                    try {
                        database.smsDao().insert(smsMessage)
                        Log.d("SmsReceiver", "SMS saved to database successfully")
                    } catch (e: Exception) {
                        Log.e("SmsReceiver", "Failed to save SMS to database", e)
                    }
                }

                // Enhanced parsing using SmsParsingService
                if (msgBody.contains("카드") && (msgBody.contains("결제") || msgBody.contains("승인") || msgBody.contains("사용"))) {
                    val parsingService = SmsParsingService(database)
                    parsingService.parsePaymentSms(msgBody) { result ->
                        if (result != null) {
                            Log.d("SmsReceiver", "Payment parsed successfully: ${result.cardName} - ${result.amount}원 (confidence: ${result.confidence})")
                        } else {
                            Log.w("SmsReceiver", "Failed to parse payment from SMS: $msgBody")
                        }
                    }
                }
            }
        }
    }

}
