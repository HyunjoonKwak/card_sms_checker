package com.example.firstapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.firstapplication.databinding.ActivityPatternDetailBinding

class PatternDetailActivity : ComponentActivity() {

    private lateinit var binding: ActivityPatternDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatternDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup back button
        binding.buttonBack.setOnClickListener {
            finish()
        }

        // Get pattern data from intent
        val patternName = intent.getStringExtra("pattern_name") ?: ""
        val patternCardPattern = intent.getStringExtra("pattern_card_pattern") ?: ""
        val patternAmountPattern = intent.getStringExtra("pattern_amount_pattern") ?: ""
        val patternDescription = intent.getStringExtra("pattern_description") ?: ""

        // Display pattern details
        binding.textviewPatternName.text = patternName
        binding.textviewCardPattern.text = patternCardPattern
        binding.textviewAmountPattern.text = patternAmountPattern
        binding.textviewDescription.text = patternDescription.ifEmpty { "설명 없음" }
    }
}