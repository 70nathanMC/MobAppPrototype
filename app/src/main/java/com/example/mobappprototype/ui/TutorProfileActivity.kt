package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.mobappprototype.Adapter.TutorProfilePagerAdapter
import com.example.mobappprototype.R
import com.example.mobappprototype.databinding.ActivityTutorProfileBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.api.Authentication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private const val TAG = "TutorProfileActivity"
class TutorProfileActivity : AppCompatActivity() {
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var binding: ActivityTutorProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestoreDb = FirebaseFirestore.getInstance()

        val tutorUid = intent.getStringExtra("TUTOR_UID")
        Log.d(TAG, "Received tutor UID: $tutorUid")

        if (tutorUid != null) {
            fetchTutorDataAndPopulateUI(tutorUid)
        } else {
            Log.e(TAG, "Tutor UID not found in Intent")
            Toast.makeText(this, "Error: Tutor not found", Toast.LENGTH_SHORT).show()
        }
        setupClickListeners()
    }
    private fun fetchTutorDataAndPopulateUI(tutorUid: String) {
        firestoreDb.collection("users").document(tutorUid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    binding.tvTutorName.text = document.getString("fullName")
                    val program = document.getString("program")
                    binding.tvTutorProgram.text = "Bachelor of Science in $program"
                    val bio = document.getString("bio") ?: ""

                    val viewPager = binding.viewPager
                    val tabLayout = binding.tabLayout

                    val pagerAdapter = TutorProfilePagerAdapter(this, bio) // Pass bio here
                    binding.viewPager.adapter = pagerAdapter
                    TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                        tab.text = when (position) {
                            0 -> "ABOUT"
                            1 -> "STRENGTH"
                            2 -> "SCHEDULE"
                            3 -> "REVIEWS"
                            else -> null
                        }
                    }.attach()

                    val profilePicUrl = document.getString("profilePic")
                    if (!profilePicUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(profilePicUrl)
                            .into(binding.sivTutorProfilePic)
                    }

                    // ... (fetch and populate other UI elements like rating, feedback, etc.) ...
                } else {
                    Log.e(TAG, "Tutor document not found")
                    Toast.makeText(this, "Error: Tutor data not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting tutor document", exception)
                Toast.makeText(this, "Error: Failed to fetch tutor data", Toast.LENGTH_SHORT).show()
            }
    }
    private fun setupClickListeners() {

        binding.btnBook.setOnClickListener {
            Intent(this, TutorSchedAndSubsListActivity::class.java).also {
                val tutorUid = intent.getStringExtra("TUTOR_UID")
                it.putExtra("TUTOR_UID", tutorUid)
                Log.d(TAG, "Tutor UID being passed to TutorSchedAndSubsListActivity: $tutorUid")
                startActivity(it)
            }
        }

        binding.btnHomeFTutorProfile.setOnClickListener {
            Intent(this, TutorListActivity::class.java).also {
                startActivity(it)
            }
        }

        binding.bottomNavigationBar.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    // Handle Home item click
                    val intent = Intent(this, TutorMainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.messages -> {
                    // Handle Messages item click
                    val intent = Intent(this, InboxActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.profile -> {
                    // Handle Profile item click
                    val intent = Intent(this, TutorMainProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}