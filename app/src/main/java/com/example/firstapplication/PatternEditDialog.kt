package com.example.firstapplication

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.example.firstapplication.databinding.DialogPatternEditBinding
import com.example.firstapplication.db.SmsPattern

class PatternEditDialog(
    private val context: Context,
    private val onSave: (name: String, cardPattern: String, amountPattern: String, description: String) -> Unit
) {
    
    private var binding: DialogPatternEditBinding = DialogPatternEditBinding.inflate(LayoutInflater.from(context))
    private var dialog: AlertDialog? = null
    
    fun setPattern(pattern: SmsPattern) {
        binding.edittextPatternName.setText(pattern.name)
        binding.edittextCardPattern.setText(pattern.cardNamePattern)
        binding.edittextAmountPattern.setText(pattern.amountPattern)
        binding.edittextDescription.setText(pattern.description)
    }
    
    fun show() {
        dialog = AlertDialog.Builder(context)
            .setTitle("SMS 패턴 편집")
            .setView(binding.root)
            .setPositiveButton("저장") { _, _ ->
                val name = binding.edittextPatternName.text.toString().trim()
                val cardPattern = binding.edittextCardPattern.text.toString().trim()
                val amountPattern = binding.edittextAmountPattern.text.toString().trim()
                val description = binding.edittextDescription.text.toString().trim()
                
                if (name.isNotEmpty() && cardPattern.isNotEmpty() && amountPattern.isNotEmpty()) {
                    onSave(name, cardPattern, amountPattern, description)
                }
            }
            .setNegativeButton("취소", null)
            .create()
            
        dialog?.show()
    }
}