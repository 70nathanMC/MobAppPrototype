package com.example.mobappprototype.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mobappprototype.R
import com.example.mobappprototype.databinding.ActivityQuizQuestBinding

class QuizQuestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuizQuestBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityQuizQuestBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


    }
}