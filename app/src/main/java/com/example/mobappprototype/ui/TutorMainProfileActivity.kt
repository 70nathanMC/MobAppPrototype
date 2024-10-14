package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.mobappprototype.R
import com.example.mobappprototype.ViewModel.UserViewModel
import com.example.mobappprototype.databinding.ActivityTutorMainProfileBinding
import com.example.mobappprototype.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private const val TAG = "TutorMainProfileActivity"
class TutorMainProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTutorMainProfileBinding
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var userViewModel: UserViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityTutorMainProfileBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.layoutMainActivity.visibility = View.INVISIBLE
        binding.loadingLayout.visibility = View.VISIBLE

        auth = FirebaseAuth.getInstance()
        firestoreDb = FirebaseFirestore.getInstance()
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        // Fetch and observe the user data
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


        userViewModel.user.observe(this) { user ->
            if (user != null) {
                updateUIWithUserData(user)
                binding.loadingLayout.visibility = View.GONE
                binding.layoutMainActivity.visibility = View.VISIBLE
            }
        }
        setupClickListeners()

    }
    private fun updateUIWithUserData(user: User) {
        binding.detailTutorName.text = user.fullName
        Glide.with(this).load(user.profilePic).into(binding.detailTutorImage)
        binding.tvBio.text = user.bio
        binding.detailDegree.text = "Bachelor of Science in ${user.program}"
        // Update other UI elements if needed
    }

    private fun setupClickListeners() {
        binding.ivLogout.setOnClickListener {
            Log.i(TAG, "User wants to logout")
            val builder = AlertDialog.Builder(this@TutorMainProfileActivity)
            builder.setTitle("Log Out")
            builder.setMessage("Are you sure you want to log out?")
            builder.setPositiveButton("Logout") { dialog, _ ->
                FirebaseAuth.getInstance().signOut()
                Intent(this@TutorMainProfileActivity, WelcomeActivity::class.java).also {
                    startActivity(it)
                }
                dialog.dismiss()
            }
            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
        binding.ivEditProfile.setOnClickListener{
            Intent(this@TutorMainProfileActivity, EditProfileActivity::class.java).also {
                startActivity(it)
            }
        }
        binding.btnCreateMeeting.setOnClickListener{
            Intent(this, CreateMeetingActivity::class.java).also {
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