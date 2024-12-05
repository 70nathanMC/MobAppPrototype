package com.example.mobappprototype.ui

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.TouchDelegate
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.mobappprototype.Adapter.TutorProfilePagerAdapter
import com.example.mobappprototype.R
import com.example.mobappprototype.ViewModel.UserViewModel
import com.example.mobappprototype.databinding.ActivityTutorProfileBinding
import com.example.mobappprototype.fragments.RatingBottomSheetFragment
import com.example.mobappprototype.model.User
import com.google.android.material.tabs.TabLayoutMediator
import com.google.api.Authentication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private const val TAG = "TutorProfileActivity"
class TutorProfileActivity : AppCompatActivity() {
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var binding: ActivityTutorProfileBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "onCreate() called")
        binding.layoutMainActivity.visibility = View.GONE
        binding.loadingLayout.visibility = View.VISIBLE

        firestoreDb = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val tutorUid = intent.getStringExtra("TUTOR_UID")
        Log.d(TAG, "Received tutor UID: $tutorUid")

        if (tutorUid != null) {
            fetchTutorDataAndPopulateUI(tutorUid)
        } else {
            Log.e(TAG, "Tutor UID not found in Intent")
            Toast.makeText(this, "Error: Tutor not found", Toast.LENGTH_SHORT).show()
        }

        binding.linearLayout6.post {
            val rect = Rect()
            binding.btnHomeFTutorProfile.getHitRect(rect)
            rect.inset(-50, -50) // Expand the touch area by 50 pixels on each side
            binding.linearLayout6.touchDelegate = TouchDelegate(rect, binding.btnHomeFTutorProfile)
        }

        setupClickListeners()
        binding.bottomNavigationBar.selectedItemId = -1
    }

    private fun fetchTutorDataAndPopulateUI(tutorUid: String) {
        val tutorDocRef = firestoreDb.collection("users").document(tutorUid)

        tutorDocRef
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    binding.tvTutorName.text = document.getString("fullName")
                    val program = document.getString("program")
                    binding.tvTutorProgram.text = "Bachelor of Science in $program"
                    val bio = document.getString("bio") ?: ""
                    binding.tvTutorRating.text = document.getDouble("overallRating").toString()
                    binding.tvFeedbackCount.text = document.getLong("feedbackAmount")?.toString()

                    tutorDocRef.addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e(TAG, "Error listening for tutor data updates", error)
                            return@addSnapshotListener
                        }

                        if (snapshot != null && snapshot.exists()) {
                            // Update tvTutorRating and tvFeedbackCount
                            binding.tvTutorRating.text = snapshot.getDouble("overallRating").toString()
                            binding.tvFeedbackCount.text = snapshot.getLong("feedbackAmount")?.toString()
                        } else {
                            Log.d(TAG, "Tutor document does not exist")
                        }
                    }

                    val viewPager = binding.viewPager
                    val tabLayout = binding.tabLayout

                    val pagerAdapter = TutorProfilePagerAdapter(this, bio) // Pass bio here
                    viewPager.adapter = pagerAdapter

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
                    binding.loadingLayout.visibility = View.GONE
                    binding.layoutMainActivity.visibility = View.VISIBLE
                } else {
                    Log.e(TAG, "Tutor document not found")
                    Toast.makeText(this, "Error: Tutor data not found", Toast.LENGTH_SHORT).show()
                    binding.loadingLayout.visibility = View.GONE
                    binding.layoutMainActivity.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting tutor document", exception)
                Toast.makeText(this, "Error: Failed to fetch tutor data", Toast.LENGTH_SHORT).show()
                binding.loadingLayout.visibility = View.GONE
                binding.layoutMainActivity.visibility = View.VISIBLE
            }
    }
    private fun setupClickListeners() {

        binding.btnBook.setOnClickListener {
            Intent(this, TutorSchedAndSubsListActivity::class.java).also {
                binding.layoutMainActivity.visibility = View.GONE
                binding.loadingLayout.visibility = View.VISIBLE
                val tutorUid = intent.getStringExtra("TUTOR_UID")
                it.putExtra("TUTOR_UID", tutorUid)
                Log.d(TAG, "Tutor UID being passed to TutorSchedAndSubsListActivity: $tutorUid")
                startActivity(it)
            }
        }

        binding.btnHomeFTutorProfile.setOnClickListener {
            Intent(this, TutorListActivity::class.java).also {
                binding.layoutMainActivity.visibility = View.GONE
                binding.loadingLayout.visibility = View.VISIBLE
                startActivity(it)
            }
        }

        binding.ivRatingIcon.setOnClickListener {
            val tutorUid = intent.getStringExtra("TUTOR_UID")
            if (tutorUid != null) {
                checkForExistingReview(tutorUid)
            }
        }


        binding.bottomNavigationBar.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    // Handle Home item click
                    val intent = Intent(this, StudentMainActivity::class.java)
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
                    val intent = Intent(this, StudentMainProfileActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun checkForExistingReview(tutorUid: String) {
        val currentUserUid = auth.currentUser?.uid ?: return

        firestoreDb.collection("reviews")
            .whereEqualTo("reviewerUID", currentUserUid)
            .whereEqualTo("tutorUID", tutorUid)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // User has an existing review
                    val reviewDocument = documents.first() // Assuming only one review per user per tutor
                    val overallRating = reviewDocument.getDouble("overallRating")?.toFloat() ?: 0f
                    val comment = reviewDocument.getString("comment") ?: ""
                    val reviewDocumentId = documents.first().id
                    Log.d(TAG, "Existing review found with ID: $reviewDocumentId")

                    val ratingBottomSheet = RatingBottomSheetFragment()
                    val bundle = Bundle()
                    bundle.putString("TUTOR_UID", intent.getStringExtra("TUTOR_UID"))
                    bundle.putString("REVIEW_ID", reviewDocumentId) // Pass the review ID
                    bundle.putFloat("EXISTING_RATING", overallRating)
                    bundle.putString("EXISTING_COMMENT", comment)
                    ratingBottomSheet.arguments = bundle
                    ratingBottomSheet.show(supportFragmentManager, "ratingBottomSheet")

                } else {
                    val ratingBottomSheet = RatingBottomSheetFragment()
                    val bundle = Bundle()
                    bundle.putString("TUTOR_UID", tutorUid)
                    ratingBottomSheet.arguments = bundle
                    ratingBottomSheet.show(supportFragmentManager, "ratingBottomSheet")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error checking for existing review", exception)
            }
    }

    override fun onResume() {
        super.onResume()
        val tutorUid = intent.getStringExtra("TUTOR_UID")
        Log.d(TAG, "Received tutor UID onResume: $tutorUid")
        if (tutorUid != null) {
            fetchTutorDataAndPopulateUI(tutorUid) // Call here instead
        } else {
            // Handle the case where tutorUid is null
            Log.e(TAG, "Tutor UID not found in Intent")
            Toast.makeText(this, "Error: Tutor not found", Toast.LENGTH_SHORT).show()
            finish()
        }

    }
}