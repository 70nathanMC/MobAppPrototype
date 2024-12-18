package com.example.mobappprototype.ui

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.mobappprototype.R
import com.example.mobappprototype.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private const val TAG = "LoginActivity"
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        if (auth.currentUser != null) {
            if (isNetworkAvailable()) {
                binding.layoutMainActivity.visibility = View.GONE
                binding.loadingLayout.visibility = View.VISIBLE
                checkUserRole()
            } else {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvRegisterNow.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        binding.btnLoginReal.setOnClickListener {

            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Email/password cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                binding.layoutMainActivity.visibility = View.GONE
                binding.loadingLayout.visibility = View.VISIBLE

                if (isNetworkAvailable()) {
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            checkUserRole()
                            Toast.makeText(this, "Sucess!", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e(TAG, "signInWithEmail failed", task.exception)
                            Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                            binding.loadingLayout.visibility = View.GONE
                            binding.layoutMainActivity.visibility = View.VISIBLE
                        }
                    }
                } else {
                    binding.loadingLayout.visibility = View.GONE
                    binding.layoutMainActivity.visibility = View.VISIBLE
                    Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
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

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

    }

    private fun checkUserRole() {
        val user = auth.currentUser
        if (user != null) {
            val userUid = user.uid
            val usersRef = db.collection("users").document(userUid)

            usersRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val role = document.getString("role")
                    when (role) {
                        "Student" -> {
                            goStudentMainActivity()
                        }
                        "Tutor" -> {
                            goTutorMainActivity()
                        }
                        else -> {
                            Log.e(TAG, "Invalid user role, user needs to go to create profile")
                            goCreateProfileActivity()
                        }
                    }
                } else {
                    Log.e(TAG, "User document not found, user needs to create profile")
                    goCreateProfileActivity()
                }
            }.addOnFailureListener { exception ->
                Log.e(TAG, "Error getting user document", exception)
                Toast.makeText(this, "Error: Failed to fetch user data", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e(TAG, "currentUser is null after successful login")
            Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show()
        }
    }
    private fun goCreateProfileActivity() {
        Log.i(TAG, "goCreateProfileActivity")
        val intent = Intent(this, CreateProfileActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun goStudentMainActivity() {
        Log.i(TAG, "goStudentMainActivity")
        val intent = Intent(this, StudentMainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun goTutorMainActivity() {
        Log.i(TAG, "goTutorMainActivity")
        val intent = Intent(this, TutorMainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        binding.loadingLayout.visibility = View.GONE
        binding.layoutMainActivity.visibility = View.VISIBLE
    }
}