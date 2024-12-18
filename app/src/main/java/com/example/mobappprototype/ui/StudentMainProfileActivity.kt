package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.mobappprototype.Adapter.StudentProfilePagerAdapter
import com.example.mobappprototype.Adapter.TutorProfilePagerAdapter
import com.example.mobappprototype.R
import com.example.mobappprototype.ViewModel.UserViewModel
import com.example.mobappprototype.databinding.ActivityStudentProfileBinding
import com.example.mobappprototype.model.User
import com.google.android.material.tabs.TabLayoutMediator
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
                    binding.loadingLayout.visibility = View.GONE
                    binding.layoutMainActivity.visibility = View.VISIBLE
                }
            }
        }
        userViewModel.user.observe(this) { user ->
            if (user != null) {
                updateUIWithUserData(user)
                fetchTutorDataAndPopulateUI(userUID.toString())
            }
        }
        setupClickListeners()
        binding.bottomNavigationBar.selectedItemId = R.id.profile
    }

    private fun fetchTutorDataAndPopulateUI(tutorUid: String) {
        firestoreDb.collection("users").document(tutorUid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val bio = document.getString("bio") ?: ""

                    val viewPager = binding.viewPager
                    val tabLayout = binding.tabLayout

                    val pagerAdapter = StudentProfilePagerAdapter(this, bio) // Pass bio here
                    binding.viewPager.adapter = pagerAdapter
                    TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                        tab.text = when (position) {
                            0 -> "ABOUT"
                            1 -> "WEAKNESSES"
                            else -> null
                        }
                    }.attach()
                } else {
                    // Handle the case where the tutor document does not exist
                    Log.e(TAG, "Tutor document not found")
                    Toast.makeText(this, "Error: Tutor data not found", Toast.LENGTH_SHORT).show()
                }
                binding.loadingLayout.visibility = View.GONE
                binding.layoutMainActivity.visibility = View.VISIBLE
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting tutor document", exception)
                Toast.makeText(this, "Error: Failed to fetch tutor data", Toast.LENGTH_SHORT).show()
                binding.loadingLayout.visibility = View.GONE
                binding.layoutMainActivity.visibility = View.VISIBLE
            }
    }

    private fun updateUIWithUserData(user: User) {
        binding.tvStudentFullName.text = user.fullName
        Glide.with(this).load(user.profilePic).into(binding.sivStudentProfilePic)
        binding.tvStudentDegree.text = "Bachelor of Science in ${user.program}"
    }
    private fun setupClickListeners() {
        binding.ivLogout.setOnClickListener {
            Log.i(TAG, "User wants to logout")
            val builder = AlertDialog.Builder(this@StudentMainProfileActivity)
            builder.setTitle("Log Out")
            builder.setMessage("Are you sure you want to log out?")
            builder.setPositiveButton("Logout") { dialog, _ ->
                auth.signOut()
                Intent(this@StudentMainProfileActivity, WelcomeActivity::class.java).also {
                    binding.layoutMainActivity.visibility = View.GONE
                    binding.loadingLayout.visibility = View.VISIBLE
                    startActivity(it)
                    finish()
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
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigationBar.selectedItemId = R.id.profile
    }
}