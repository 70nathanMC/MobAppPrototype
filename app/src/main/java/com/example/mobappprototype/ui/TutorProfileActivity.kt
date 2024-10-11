package com.example.mobappprototype.ui

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.mobappprototype.R
import com.example.mobappprototype.databinding.ActivityTutorProfileBinding

class TutorProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTutorProfileBinding
    private lateinit var ibtnHomeFFindTutorProfile: ImageView
    private lateinit var btnBook: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ibtnHomeFFindTutorProfile = findViewById(R.id.ibtnHomeFFindTutorProfile)
        btnBook = findViewById(R.id.btnBook)

        val intent = this.intent
        if (intent != null) {
            val tutorName = intent.getStringExtra("tutorName")
            var degree = intent.getStringExtra("degree")
            val tutorImage = intent.getIntExtra("image", R.drawable.jamesdp)
//            val tutorDesc = intent.getIntExtra("tutorDesc", R.string.tutor_about1)
            val rating = intent.getFloatExtra("rating", 4.5F)
//            val tutorStrength = intent.getIntExtra("tutorStrength", R.string.strength1)
//            val tutorSchedule = intent.getIntExtra("tutorSchedule", R.string.schedule1)
            val resources: Resources = resources
            var bachelorString = resources.getString(R.string.bachelor)
            bachelorString = "$bachelorString "
            degree = "$bachelorString$degree"
            binding.detailTutorName.text = tutorName
            binding.detailDegree.text = degree
            binding.tvTutorRating.text = rating.toString()
//            binding.detailDesc.setText(tutorDesc)
            binding.detailTutorImage.setImageResource(tutorImage)
//            binding.detailStrength.setText(tutorStrength)
//            binding.detailSchedule.setText(tutorSchedule)
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