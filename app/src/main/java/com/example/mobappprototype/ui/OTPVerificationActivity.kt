package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobappprototype.databinding.ActivityOtpverificationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider


private const val TAG = "OTPVerificationActivity"
class OTPVerificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOtpverificationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var verificationId: String // To store the verification ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpverificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        verificationId = intent.getStringExtra("verificationId") ?: ""
        val email = intent.getStringExtra("EMAIL") ?: ""

        // Set up TextWatchers for OTP EditTexts
        val otpEditTexts = arrayOf(
            binding.etOtp1,
            binding.etOtp2,
            binding.etOtp3,
            binding.etOtp4
        )
        setUpOtpInputs(otpEditTexts)

        binding.btnSendCode.setOnClickListener {
            val otp = otpEditTexts.joinToString("") { it.text.toString() }
            if (otp.length == 4) {
                // Verify OTP
                val credential = PhoneAuthProvider.getCredential(verificationId, otp)
                signInWithPhoneAuthCredential(credential, email)
            } else {
                Toast.makeText(this, "Please enter the complete OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setUpOtpInputs(otpEditTexts: Array<EditText>) {
        for (i in otpEditTexts.indices) {
            otpEditTexts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (!s.isNullOrEmpty() && i < otpEditTexts.size - 1) {
                        otpEditTexts[i + 1].requestFocus()
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential, email: String) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    Toast.makeText(this, "OTP Verification successful!", Toast.LENGTH_SHORT).show()

                    // Go back to LoginActivity
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish() // Optional: Finish the OTPVerificationActivity
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
}