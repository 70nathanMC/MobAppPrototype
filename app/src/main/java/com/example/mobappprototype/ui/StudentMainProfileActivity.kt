package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.mobappprototype.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth

private const val TAG = "ProfileActivity"
class StudentMainProfileActivity : AppCompatActivity() {

    lateinit var binding : ActivityProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogout.setOnClickListener{
            Log.i(TAG, "User wants to logout")
            FirebaseAuth.getInstance().signOut()
            Intent(this@StudentMainProfileActivity, WelcomeActivity::class.java).also {
                startActivity(it)
            }
        }

    }
}