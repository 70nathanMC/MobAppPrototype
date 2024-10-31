package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mobappprototype.R
import com.example.mobappprototype.databinding.ActivityQuestionsBinding
import com.example.mobappprototype.databinding.ActivityResultBinding
import com.example.mobappprototype.utils.Constants

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val totalQuestions = intent.getIntExtra(Constants.TOTAL_QUESTIONS, 0)
        val score = intent.getIntExtra(Constants.SCORE, 0)

        binding.tvScoreResult.text = "$score/$totalQuestions"

        binding.ibtnHomeFResult.setOnClickListener {
            Intent(this, StudentMainActivity::class.java).also {
                binding.layoutMainActivity.visibility = View.GONE
                binding.loadingLayout.visibility = View.VISIBLE
                startActivity(it)
                finish()
            }
        }
    }
}