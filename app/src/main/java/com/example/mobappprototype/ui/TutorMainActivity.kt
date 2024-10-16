package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobappprototype.Adapter.MeetingsAdapter
import com.example.mobappprototype.R
import com.example.mobappprototype.ViewModel.UserViewModel
import com.example.mobappprototype.databinding.ActivityTutorMainBinding
import com.example.mobappprototype.model.MeetingForTutor
import com.example.mobappprototype.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

private const val TAG = "TutorMainActivity"
class TutorMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTutorMainBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var meetingsAdapter: MeetingsAdapter
    private lateinit var rvMeetings: RecyclerView
    private lateinit var userViewModel: UserViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityTutorMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.layoutMainActivity.visibility = View.GONE
        binding.loadingLayout.visibility = View.VISIBLE

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]


        val userUID = auth.currentUser?.uid
        if (userUID != null) {
            val userRef = db.collection("users").document(userUID)
            userRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(User::class.java)
                    if (user != null) {
                        userViewModel.setUser(user)
                    }
                } else {
                    Log.d(TAG, "User document does not exist")
                    goCreateProfileActivity()
                }
            }
        }

        userViewModel.user.observe(this) { user ->
            if (user != null) {
                updateUIWithUserData(user)
                fetchMeetings()
                binding.loadingLayout.visibility = View.GONE
                binding.layoutMainActivity.visibility = View.VISIBLE
            }
        }


        binding.btnCreateMeeting.setOnClickListener {
            val intent = Intent(this, CreateMeetingActivity::class.java)
            startActivity(intent)
        }
        binding.ivUserImageDashboard.setOnClickListener{
            val intent = Intent(this, TutorMainProfileActivity::class.java)
            startActivity(intent)
        }

        rvMeetings = findViewById(R.id.rvMeetings)
        rvMeetings.layoutManager = LinearLayoutManager(this)
        meetingsAdapter = MeetingsAdapter(emptyList())
        rvMeetings.adapter = meetingsAdapter


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
    private fun updateUIWithUserData(user: User) {
        binding.tvUserFirstNameDashboard.text = user.firstName
        Glide.with(this).load(user.profilePic).into(binding.ivUserImageDashboard)
        // Update other UI elements if needed
    }

    private fun goCreateProfileActivity() {
        Log.i(TAG, "goCreateProfileActivity")
        val intent = Intent(this, CreateProfileActivity::class.java)
        startActivity(intent)
        finish()
    }
    override fun onResume() {
        super.onResume()
        fetchMeetings()
    }
    private fun fetchMeetings() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .collection("tutorData")
            .document("data")
            .get()
            .addOnSuccessListener { document ->
                val meetingIds = document.get("meetings") as? List<String> ?: emptyList()

                // Initialize RecyclerView and adapter only once
                if (!::meetingsAdapter.isInitialized) { // Check if already initialized
                    rvMeetings = findViewById(R.id.rvMeetings)
                    rvMeetings.layoutManager = LinearLayoutManager(this)
                    meetingsAdapter = MeetingsAdapter(emptyList())
                    rvMeetings.adapter = meetingsAdapter
                }

                if (meetingIds.isEmpty()) {
                    meetingsAdapter.meetings = emptyList() // Update the adapter with an empty list
                    meetingsAdapter.notifyDataSetChanged() // Notify the adapter of the change
                    return@addOnSuccessListener
                }

                Log.d("TutorMainActivity", "Meeting IDs: $meetingIds")

                val query = db.collection("meetings")
                    .whereIn(FieldPath.documentId(), meetingIds)

                query.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Toast.makeText(this, "Error fetching meetings: ${error.message}", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    val meetings = mutableListOf<MeetingForTutor>()
                    for (meetingDocument in snapshot!!.documents) {
                        val meeting = meetingDocument.toObject(MeetingForTutor::class.java)
                        val meetingWithId = meeting!!.copy(id = meetingDocument.id)
                        meetings.add(meetingWithId)
                    }

                    meetingsAdapter.meetings = meetings
                    meetingsAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching meetings: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}