package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobappprototype.databinding.ActivityCreateNewPasswordBinding
import com.google.firebase.auth.FirebaseAuth

private const val TAG = "CreateNewPasswordActivity"

class CreateNewPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateNewPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateNewPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val email = intent.getStringExtra("EMAIL") ?: ""

        binding.btnResetPassword.setOnClickListener {
            val newPassword = binding.etPasswordRegister.text.toString()
            val confirmPassword = binding.etConfirmPasswordRegister.text.toString()

            if (newPassword.isBlank() || confirmPassword.isBlank()) {
                Toast.makeText(this, "Passwords cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Update the password without relying on auth.currentUser
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Password reset email sent successfully")
                        Toast.makeText(this, "Password reset email sent!", Toast.LENGTH_SHORT).show()

                        // Start PasswordChangedConfirmActivity
                        val intent = Intent(this, PasswordChangedConfirmActivity::class.java)
                        startActivity(intent)
                        finish() // Optional: Finish this activity
                    } else {
                        Log.e(TAG, "Error sending password reset email", task.exception)
                        Toast.makeText(this, "Error sending password reset email.", Toast.LENGTH_SHORT).show()
                        // Handle the error appropriately (e.g., show a more specific error message)
                    }
                }
        }

        binding.ivBackFNewPassword.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}