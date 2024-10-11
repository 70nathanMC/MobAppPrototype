package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.mobappprototype.R
import com.example.mobappprototype.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

private const val TAG = "LoginActivity"
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            goMainActivity()
        }

        binding.tvRegisterNow.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.btnLoginReal.setOnClickListener {
            binding.btnLoginReal.isEnabled = false
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Email/password cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Firebase authentication check
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener{task ->
                binding.btnLoginReal.isEnabled = true
                if (task.isSuccessful) {
                    Toast.makeText(this, "Sucess!", Toast.LENGTH_SHORT).show()
                    goMainActivity()
            } else {
                Log.e(TAG, "signInWithEmail failed", task.exception)
                Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
            }
            }

        }
        binding.ivBackFLogin.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }
        binding.etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.etEmail.setTextColor(resources.getColor(R.color.appBlack))
                binding.etEmail.background = ResourcesCompat.getDrawable(resources, R.drawable.default_options_border_bg, null)

            } else {
                binding.etEmail.setTextColor(resources.getColor(R.color.appGray8))
                binding.etEmail.background = ResourcesCompat.getDrawable(resources, R.drawable.default_options_border_bg_gray, null)
            }
        }

        binding.etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.etPassword.setTextColor(resources.getColor(R.color.appBlack))
                binding.etPassword.background = ResourcesCompat.getDrawable(resources, R.drawable.default_options_border_bg, null)

            } else {
                binding.etPassword.setTextColor(resources.getColor(R.color.appGray8))
                binding.etPassword.background = ResourcesCompat.getDrawable(resources, R.drawable.default_options_border_bg_gray, null)
            }
        }
    }

    private fun goMainActivity() {
        Log.i(TAG, "goMainActivity")
        val intent = Intent(this, StudentMainActivity::class.java)
        startActivity(intent)
        finish()
    }
}