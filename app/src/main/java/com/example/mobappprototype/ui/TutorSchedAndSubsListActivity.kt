package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.mobappprototype.Adapter.MeetingDataAdapter
import com.example.mobappprototype.R
import com.example.mobappprototype.ViewModel.UserViewModel
import com.example.mobappprototype.databinding.ActivityTutorSchedAndSubsListBinding
import com.example.mobappprototype.model.MeetingData
import com.example.mobappprototype.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private const val TAG = "TutorSchedAndSubsListActivity"
class TutorSchedAndSubsListActivity : AppCompatActivity() {

    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var binding: ActivityTutorSchedAndSubsListBinding
    private lateinit var meetingDataAdapter: MeetingDataAdapter
    private val meetingList = mutableListOf<MeetingData>()
    private lateinit var auth: FirebaseAuth
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorSchedAndSubsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestoreDb = FirebaseFirestore.getInstance()
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        meetingDataAdapter = MeetingDataAdapter(meetingList)
        binding.rvMeetings.layoutManager = LinearLayoutManager(this)
        binding.rvMeetings.adapter = meetingDataAdapter

        binding.bottomNavigationBar.selectedItemId = -1

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

        val tutorUid = intent.getStringExtra("TUTOR_UID")
        Log.d(TAG, "Tutor UID retrieved from TutorListActivity: $tutorUid")
        if (tutorUid != null) {
            Log.d(TAG, "Received tutor UID: $tutorUid")
            fetchMeetings(tutorUid)
            fetchTutorName(tutorUid)
        } else {
            Log.e(TAG, "Tutor UID not found in Intent")
        }
        binding.ivUserProfile.setOnClickListener{
            Intent(this, StudentMainProfileActivity::class.java).also {
                startActivity(it)
            }
        }

        binding.ibtnHomeFTutorSchedAndSubsList.setOnClickListener {
            Intent(this, StudentMainActivity::class.java).also {
                startActivity(it)
            }
        }

        binding.bottomNavigationBar.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    // Handle Home item click
                    val intent = Intent(this, StudentMainActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.messages -> {
                    // Handle Messages item click
                    val intent = Intent(this, InboxActivity::class.java)
                    startActivity(intent)
                    finish()
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

    private fun updateUIWithUserData(user: User) {
        Glide.with(this).load(user.profilePic).into(binding.ivUserProfile)
        // Update other UI elements if needed
    }
    private fun fetchMeetings(tutorUid: String) {
        Log.d(TAG, "Fetching meetings for tutor UID: $tutorUid")
        firestoreDb.collection("meetings").whereEqualTo("tutorId", tutorUid)
            .get()
            .addOnSuccessListener { documents ->
                Log.d(TAG, "Number of meeting documents retrieved: ${documents.size()}")
                for (document in documents) {
                    val meeting = MeetingData(
                        id = document.id,
                        subject = document.getString("subject") ?: "",
                        branch = document.getString("branch") ?: "",
                        day = document.getString("day") ?: "",
                        startTime = document.getString("startTime") ?: "",
                        endTime = document.getString("endTime") ?: "",
                        slots = document.getLong("slots")?.toInt() ?: 0,
                        slotsRemaining = document.getLong("slotsRemaining")?.toInt() ?: 0,
                        participants = document.get("participants") as? List<String> ?: emptyList(),
                        tutorId = document.getString("tutorId") ?: ""
                    )
                    meetingList.add(meeting)
                }
                meetingDataAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error fetching meetings: ", exception)
            }
    }
    private fun fetchTutorName(tutorUid: String) {
        firestoreDb.collection("users").document(tutorUid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val tutorName = document.getString("fullName") ?: ""
                    binding.tvTutorNameTitle.text = tutorName
                } else {
                    Log.e(TAG, "Tutor document not found")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting tutor document", exception)
            }
    }
}