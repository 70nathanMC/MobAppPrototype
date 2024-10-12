package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.mobappprototype.ViewModel.UserViewModel
import com.example.mobappprototype.databinding.ActivityStudentProfileBinding
import com.example.mobappprototype.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private const val TAG = "ProfileActivity"
class StudentMainProfileActivity : AppCompatActivity() {
    lateinit var binding : ActivityStudentProfileBinding
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var userViewModel: UserViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        userViewModel.user.observe(this) { user ->
            if (user != null) {
                updateUIWithUserData(user)
            }
        }
        setupClickListeners()
    }
    private fun updateUIWithUserData(user: User) {
        binding.tvStudentFullName.text = user.fullName
        Glide.with(this).load(user.profilePic).into(binding.sivStudentProfilePic)
        binding.tvBio.text = user.bio
        binding.tvStudentDegree.text = "Bachelor of Science in ${user.program}"
        // Update other UI elements if needed
    }
    private fun setupClickListeners() {
        binding.ivLogout.setOnClickListener{
            auth.signOut()
            Intent(this, WelcomeActivity::class.java).also {
                startActivity(it)
            }
        }
        binding.ivLogout.setOnClickListener {
            Log.i(TAG, "User wants to logout")
            val builder = AlertDialog.Builder(this@StudentMainProfileActivity)
            builder.setTitle("Log Out")
            builder.setMessage("Are you sure you want to log out?")
            builder.setPositiveButton("Logout") { dialog, _ ->
                FirebaseAuth.getInstance().signOut()
                Intent(this@StudentMainProfileActivity, WelcomeActivity::class.java).also {
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
            Intent(this@StudentMainProfileActivity, EditProfileActivity::class.java).also {
                startActivity(it)
            }
        }
        binding.btnFindTutorFProfile.setOnClickListener{
            Intent(this, TutorListActivity::class.java).also {
                startActivity(it)
            }
        }
    }
}