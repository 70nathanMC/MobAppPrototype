package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mobappprototype.databinding.ActivityPasswordChangedConfirmBinding

class PasswordChangedConfirmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPasswordChangedConfirmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordChangedConfirmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnResetPassword.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}