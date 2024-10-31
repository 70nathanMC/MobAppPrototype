package com.example.mobappprototype.ui

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.mobappprototype.R
import com.example.mobappprototype.databinding.ActivityWelcomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private const val TAG = "WelcomeActivity"
class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnRegister.setOnClickListener{
            Intent(this@WelcomeActivity, RegisterActivity::class.java).also {
                binding.layoutMainActivity.visibility = View.GONE
                binding.loadingLayout.visibility = View.VISIBLE
                startActivity(it)
            }
        }
        binding.btnLogin.setOnClickListener {
            binding.layoutMainActivity.visibility = View.GONE
            binding.loadingLayout.visibility = View.VISIBLE
            if (auth.currentUser != null) {
                if (isNetworkAvailable()) {
                    checkUserRole()
                } else {
                    binding.loadingLayout.visibility = View.GONE
                    binding.layoutMainActivity.visibility = View.VISIBLE
                    Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
                }
            } else {
                goLoginActivity()
            }
        }
    }
    private fun checkUserRole() {
        val user = auth.currentUser
        if (user != null) {
            Log.d(TAG, "User has already logged in")
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
                            goCreateProfileActivity()
                            Log.e(TAG, "Invalid user role, user needs to go to create profile")
                        }
                    }
                } else {
                    auth.signOut()
                    goLoginActivity()
                    Log.e(TAG, "User document not found, user needs to go to login")
                }
            }.addOnFailureListener { exception ->
                Log.e(TAG, "Error getting user document", exception)
                Toast.makeText(this, "Error: Failed to fetch user data", Toast.LENGTH_SHORT).show()
                binding.loadingLayout.visibility = View.GONE
                binding.layoutMainActivity.visibility = View.VISIBLE
            }
        } else {
            Log.e(TAG, "currentUser is null after successful login")
            Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show()
            binding.loadingLayout.visibility = View.GONE
            binding.layoutMainActivity.visibility = View.VISIBLE
        }
    }
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

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
    private fun goCreateProfileActivity() {
        Log.i(TAG, "goCreateProfileActivity")
        val intent = Intent(this, CreateProfileActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun goLoginActivity() {
        Log.i(TAG, "goLoginActivity")
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
    override fun onResume() {
        super.onResume()
        binding.layoutMainActivity.visibility = View.VISIBLE
        binding.loadingLayout.visibility = View.GONE
    }
}