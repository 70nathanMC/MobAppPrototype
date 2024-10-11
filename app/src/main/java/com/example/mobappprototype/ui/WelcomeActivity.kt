package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mobappprototype.R

class WelcomeActivity : AppCompatActivity() {
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var btnGoToCreateProfile: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
        btnGoToCreateProfile = findViewById(R.id.btnGoToCreateProfile)

        btnLogin.setOnClickListener{
            Intent(this@WelcomeActivity, LoginActivity::class.java).also {
                startActivity(it)
            }
        }
        btnRegister.setOnClickListener{
            Intent(this@WelcomeActivity, RegisterActivity::class.java).also {
                startActivity(it)
            }
        }
        btnGoToCreateProfile.setOnClickListener {
            Intent(this@WelcomeActivity, CreateProfileActivity::class.java).also {
                startActivity(it)
            }
        }
    }
}