package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mobappprototype.R
import com.example.mobappprototype.databinding.ActivityTutorListBinding
import com.example.mobappprototype.databinding.ActivityTutorProfileBinding
import com.google.android.material.button.MaterialButton

class TutorProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTutorProfileBinding
    private lateinit var ibtnHomeFFindTutorProfile: ImageButton
    private lateinit var btnBook: MaterialButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ibtnHomeFFindTutorProfile = findViewById(R.id.ibtnHomeFFindTutorProfile)
        btnBook = findViewById(R.id.btnBook)

        val intent = this.intent
        if (intent != null) {
            val tutorName = intent.getStringExtra("tutorName")
            val degree = intent.getStringExtra("degree")
            val ratingDesc = intent.getStringExtra("ratingDesc")
            val tutorImage = intent.getIntExtra("image", R.drawable.james)
            val tutorDesc = intent.getIntExtra("tutorDesc", R.string.tutor_about1)
//            val rating = intent.getFloatExtra("rating", 4.5F)
            val tutorStrength = intent.getIntExtra("tutorStrength", R.string.strength1)
            val tutorSchedule = intent.getIntExtra("tutorSchedule", R.string.schedule1)
            binding.detailTutorName.text = tutorName
            binding.detailDegree.text = degree
//            binding.detailRating.rating = rating
            binding.detailRating.text = ratingDesc
            binding.detailDesc.setText(tutorDesc)
            binding.detailImage.setImageResource(tutorImage)
            binding.detailStrength.setText(tutorStrength)
            binding.detailSchedule.setText(tutorSchedule)
        }

        btnBook.setOnClickListener {
            Intent(this, TutorSchedAndSubsListActivity::class.java).also {
                startActivity(it)
            }
        }

        ibtnHomeFFindTutorProfile.setOnClickListener {
            Intent(this, TutorListActivity::class.java).also {
                startActivity(it)
            }
        }


    }
}