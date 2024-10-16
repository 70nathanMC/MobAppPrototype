package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.mobappprototype.R
import com.example.mobappprototype.ViewModel.UserViewModel
import com.example.mobappprototype.databinding.ActivityStudentMainBinding
import com.example.mobappprototype.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private const val TAG = "MainActivity"

class StudentMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentMainBinding
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.layoutMainActivity.visibility = View.GONE
        binding.loadingLayout.visibility = View.VISIBLE

        auth = FirebaseAuth.getInstance()
        firestoreDb = FirebaseFirestore.getInstance()
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        val userUID = auth.currentUser?.uid
        if (userUID != null) {
            val userRef = firestoreDb.collection("users").document(userUID)
            userRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(User::class.java)
                    if (user != null) {
                        userViewModel.setUser(user)
                    }
                } else {
                    Log.d(TAG, "User document does not exist")
                }
            }
        }

        // Observe the User LiveData to update the UI when data is ready
        userViewModel.user.observe(this) { user ->
            if (user != null) {
                updateUIWithUserData(user)
                binding.loadingLayout.visibility = View.GONE // Hide loading indicator
                binding.layoutMainActivity.visibility = View.VISIBLE
            }
        }
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.ivGenMath.setOnClickListener {
            Intent(this@StudentMainActivity, QuizActivity::class.java).also {
                it.putExtra("SUBJECT_NAME", "General Mathematics")
                startActivity(it)
            }
        }
        binding.ivPhysics.setOnClickListener{
            Intent(this@StudentMainActivity, QuizActivity::class.java).also {
                it.putExtra("SUBJECT_NAME", "Physics")
                startActivity(it)
            }
        }
        binding.ivCalculus.setOnClickListener{
            Intent(this@StudentMainActivity, QuizActivity::class.java).also {
                it.putExtra("SUBJECT_NAME", "Calculus")
                startActivity(it)
            }
        }
        binding.ivScience.setOnClickListener{
            Intent(this@StudentMainActivity, QuizActivity::class.java).also {
                it.putExtra("SUBJECT_NAME", "Science")
                startActivity(it)
            }
        }
        binding.ivHistory.setOnClickListener{
            Intent(this@StudentMainActivity, QuizActivity::class.java).also {
                it.putExtra("SUBJECT_NAME", "History")
                startActivity(it)
            }
        }
        binding.ivLiterature.setOnClickListener{
            Intent(this@StudentMainActivity, QuizActivity::class.java).also {
                it.putExtra("SUBJECT_NAME", "Literature")
                startActivity(it)
            }
        }
        binding.ivStatistics.setOnClickListener{
            Intent(this@StudentMainActivity, QuizActivity::class.java).also {
                it.putExtra("SUBJECT_NAME", "Statistics")
                startActivity(it)
            }
        }
        binding.ivPhilosophy.setOnClickListener{
            Intent(this@StudentMainActivity, QuizActivity::class.java).also {
                it.putExtra("SUBJECT_NAME", "Philosophy")
                startActivity(it)
            }
        }
        binding.ivJumpToMeetings.setOnClickListener{
            Intent(this@StudentMainActivity, TutorSchedAndSubsListActivity::class.java).also {
                startActivity(it)
            }
        }
        binding.ivReadyToLearn.setOnClickListener{
            Intent(this@StudentMainActivity, TutorSearchActivity::class.java).also {
                startActivity(it)
            }
        }
        binding.btnFindTutor.setOnClickListener {
            Intent(this@StudentMainActivity, TutorSearchActivity::class.java).also {
                startActivity(it)
            }
        }
        binding.ivUserImageDashboard.setOnClickListener{
            Intent(this@StudentMainActivity, StudentMainProfileActivity::class.java).also {
                startActivity(it)
            }
        }
        binding.bottomNavigationBar.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    val intent = Intent(this, StudentMainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.messages -> {
                    val intent = Intent(this, InboxActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.profile -> {
                    val intent = Intent(this, StudentMainProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
    private fun updateUIWithUserData(user: User) {
        binding.tvUserFirstNameDashboard.text = user.firstName
        Glide.with(this).load(user.profilePic).into(binding.ivUserImageDashboard)
        // Update other UI elements if needed
    }
}
