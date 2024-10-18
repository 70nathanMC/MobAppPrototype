package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.mobappprototype.Adapter.MeetingAdapter
import com.example.mobappprototype.R
import com.example.mobappprototype.ViewModel.UserViewModel
import com.example.mobappprototype.databinding.ActivityStudentMainBinding
import com.example.mobappprototype.model.MeetingData
import com.example.mobappprototype.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

private const val TAG = "MainActivity"

class StudentMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentMainBinding
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var userViewModel: UserViewModel
    private lateinit var meetingAdapter: MeetingAdapter

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

        meetingAdapter = MeetingAdapter(listOf())
        binding.rvMeetingsToday.adapter = meetingAdapter
        binding.rvMeetingsToday.layoutManager = LinearLayoutManager(this)

        // Observe the User LiveData to update the UI when data is ready
        userViewModel.user.observe(this) { user ->
            if (user != null) {
                updateUIWithUserData(user)
                fetchTodaysMeetings()
                binding.loadingLayout.visibility = View.GONE
                binding.layoutMainActivity.visibility = View.VISIBLE
            }
        }
        setupClickListeners()
    }

    private fun fetchTodaysMeetings() {
        val userUID = auth.currentUser?.uid
        if (userUID != null) {
            firestoreDb.collection("studentMeetings").document(userUID)
                .get()
                .addOnSuccessListener { studentMeetingsDocument ->
                    if (studentMeetingsDocument.exists()) {
                        val meetingIds = studentMeetingsDocument.get("meetingIds") as? List<String>
                        if (meetingIds != null) {
                            val today = Calendar.getInstance()
                            val todaysMeetings = mutableListOf<MeetingData>()
                            for (meetingId in meetingIds) {
                                firestoreDb.collection("meetings").document(meetingId)
                                    .addSnapshotListener { meetingDocumentSnapshot, error ->
                                        if (error != null) {
                                            Log.w(TAG, "Listen failed.", error)
                                            return@addSnapshotListener
                                        }

                                        if (meetingDocumentSnapshot != null && meetingDocumentSnapshot.exists()) {
                                            val meetingData = meetingDocumentSnapshot.toObject(MeetingData::class.java)
                                            if (meetingData != null) {
                                                meetingData.id = meetingDocumentSnapshot.id
                                                val meetingDate = meetingData.date.toDate()
                                                val meetingCalendar = Calendar.getInstance().apply {
                                                    time = meetingDate
                                                }
                                                if (today.get(Calendar.YEAR) == meetingCalendar.get(Calendar.YEAR) &&
                                                    today.get(Calendar.MONTH) == meetingCalendar.get(Calendar.MONTH) &&
                                                    today.get(Calendar.DAY_OF_MONTH) == meetingCalendar.get(Calendar.DAY_OF_MONTH)
                                                ) {
                                                    // Update the meeting in the list or add it if it doesn't exist
                                                    val existingMeetingIndex = todaysMeetings.indexOfFirst { it.id == meetingData.id }
                                                    if (existingMeetingIndex != -1) {
                                                        todaysMeetings[existingMeetingIndex] = meetingData
                                                    } else {
                                                        todaysMeetings.add(meetingData)
                                                    }
                                                    meetingAdapter.meetings =
                                                        todaysMeetings.toList().toMutableList()
                                                    meetingAdapter.notifyDataSetChanged()
                                                } else {
                                                    // Remove the meeting from the list if it's no longer today
                                                    val existingMeetingIndex = todaysMeetings.indexOfFirst { it.id == meetingData.id }
                                                    if (existingMeetingIndex != -1) {
                                                        todaysMeetings.removeAt(existingMeetingIndex)
                                                        meetingAdapter.meetings =
                                                            todaysMeetings.toList().toMutableList()
                                                        meetingAdapter.notifyDataSetChanged()
                                                    }
                                                }
                                            }
                                        } else {
                                            Log.d(TAG, "Current data: null")
                                        }
                                    }
                            }
                        }
                    }
                }
        }
    }

    private fun setupClickListeners() {
        binding.ivGenMath.setOnClickListener {
            Intent(this@StudentMainActivity, QuizActivity::class.java).also {
                it.putExtra("SUBJECT_NAME", "Gen Math")
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
        binding.ivArrowNext.setOnClickListener{
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }
        binding.tvAgendaForToday.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }
    }
    private fun updateUIWithUserData(user: User) {
        binding.tvUserFirstNameDashboard.text = user.firstName
        Glide.with(this).load(user.profilePic).into(binding.ivUserImageDashboard)
        // Update other UI elements if needed
    }
}
