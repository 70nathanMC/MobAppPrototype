package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.mobappprototype.R
import com.example.mobappprototype.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

private const val TAG = "ForgotPasswordActivity"
class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnSendCode.setOnClickListener {
            val email = binding.etEmailForgot.text.toString()
            if (email.isBlank()) {
                Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Email sent.")
                        Toast.makeText(this, "Verification email sent to $email", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Log.e(TAG, "sendPasswordResetEmail failed", task.exception)
                        Toast.makeText(this, "Failed to send reset email.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
        }

        binding.ivBackFForgotPass.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.etEmailForgot.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.etEmailForgot.setTextColor(resources.getColor(R.color.appBlack))
                binding.etEmailForgot.background = ResourcesCompat.getDrawable(resources, R.drawable.default_options_border_bg, null)

            } else {
                binding.etEmailForgot.setTextColor(resources.getColor(R.color.appGray8))
                binding.etEmailForgot.background = ResourcesCompat.getDrawable(resources, R.drawable.default_options_border_bg_gray, null)
            }
        }
    }
}