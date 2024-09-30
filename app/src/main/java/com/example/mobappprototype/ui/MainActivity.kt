package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mobappprototype.R

class MainActivity : AppCompatActivity() {
    private lateinit var btnFindTutor: Button
    private lateinit var tvView: TextView
    private lateinit var ibtnQuizMath: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        btnFindTutor = findViewById(R.id.btnFindTutor)
        tvView = findViewById(R.id.tvUserName)
        ibtnQuizMath = findViewById(R.id.ibtnQuizMath)

        ibtnQuizMath.setOnClickListener{
            Intent(this@MainActivity, QuestionsActivity::class.java).also {
                startActivity(it)
            }
        }

        btnFindTutor.setOnClickListener {
            Intent(this@MainActivity, TutorSearchActivity::class.java).also {
                startActivity(it)
            }
        }
    }
}