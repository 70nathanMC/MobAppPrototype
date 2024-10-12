package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.mobappprototype.databinding.ActivityTutorProfileBinding
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
        Log.d(TAG, "Received tutor UID: $tutorUid") // Add this log

        if (tutorUid != null) {
            fetchTutorDataAndPopulateUI(tutorUid)
        } else {
            Log.e(TAG, "Tutor UID not found in Intent")
            Toast.makeText(this, "Error: Tutor not found", Toast.LENGTH_SHORT).show()
        }

        binding.btnBook.setOnClickListener {
            Intent(this, TutorSchedAndSubsListActivity::class.java).also {
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
    }
    private fun fetchTutorDataAndPopulateUI(tutorUid: String) {
        firestoreDb.collection("users").document(tutorUid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Populate UI elements with tutor data
                    binding.tvTutorName.text = document.getString("fullName")
                    val program = document.getString("program")
                    binding.tvTutorProgram.text = "Bachelor of Science in $program"
                    binding.tvBio.text = document.getString("bio")

                    // Load profile image using Glide/Picasso
                    val profilePicUrl = document.getString("profilePic")
                    if (!profilePicUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(profilePicUrl)
                            .into(binding.sivTutorProfilePic)
                    }

                    // ... (fetch and populate other UI elements like rating, feedback, etc.) ...
                } else {
                    // Handle the case where the tutor document does not exist
                    Log.e(TAG, "Tutor document not found")
                    Toast.makeText(this, "Error: Tutor data not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors that occur while fetching the tutor document
                Log.e(TAG, "Error getting tutor document", exception)
                Toast.makeText(this, "Error: Failed to fetch tutor data", Toast.LENGTH_SHORT).show()
            }
    }
}