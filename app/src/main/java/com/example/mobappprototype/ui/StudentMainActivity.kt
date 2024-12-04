package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import com.google.firebase.messaging.FirebaseMessaging

private const val TAG = "MainActivity"

class StudentMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentMainBinding
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var userViewModel: UserViewModel
    private lateinit var meetingAdapter: MeetingAdapter
    private var dotsCount: Int = 0
    private lateinit var dots: Array<ImageView?>

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
                fetchThisWeeksMeetings()
                updateFcmToken()
            }
        }
        setupClickListeners()
//        setupHorizontalScrollView()
        binding.bottomNavigationBar.selectedItemId = R.id.home
    }

    private fun fetchThisWeeksMeetings() {
        val userUID = auth.currentUser?.uid
        if (userUID != null) {
            firestoreDb.collection("studentMeetings").document(userUID)
                .get()
                .addOnSuccessListener { studentMeetingsDocument ->
                    if (studentMeetingsDocument.exists()) {
                        val meetingIds = studentMeetingsDocument.get("meetingIds") as? List<String>
                        if (meetingIds != null) {
                            // Calculate the date range for the next 7 days
                            val calendar = Calendar.getInstance()
                            calendar.add(Calendar.DAY_OF_MONTH, 7) // Add 7 days to get the end date
                            val endDate = calendar.time

                            val weekMeetings = mutableListOf<MeetingData>()

                            // First query: Get meeting documents
                            firestoreDb.collection("meetings")
                                .whereIn(FieldPath.documentId(), meetingIds)
                                .get()
                                .addOnSuccessListener { meetingsQuerySnapshot ->

                                    // Second query: Filter by date in memory and calculate upcoming date
                                    for (meetingDocument in meetingsQuerySnapshot) {
                                        val meetingData = meetingDocument.toObject(MeetingData::class.java)
                                        meetingData.id = meetingDocument.id

                                        try {
                                            // Calculate the upcoming meeting date
                                            val meetingDayOfWeek = when (meetingData.day.lowercase()) {
                                                "monday" -> Calendar.MONDAY
                                                "tuesday" -> Calendar.TUESDAY
                                                "wednesday" -> Calendar.WEDNESDAY
                                                "thursday" -> Calendar.THURSDAY
                                                "friday" -> Calendar.FRIDAY
                                                "saturday" -> Calendar.SATURDAY
                                                "sunday" -> Calendar.SUNDAY
                                                else -> throw IllegalArgumentException("Invalid day of the week: ${meetingData.day}")
                                            }

                                            val todayCalendar = Calendar.getInstance()
                                            val todayDayOfWeek = todayCalendar.get(Calendar.DAY_OF_WEEK)
                                            val daysUntilMeetingDay = if (meetingDayOfWeek >= todayDayOfWeek) {
                                                meetingDayOfWeek - todayDayOfWeek
                                            } else {
                                                meetingDayOfWeek - todayDayOfWeek + 7
                                            }
                                            todayCalendar.add(Calendar.DAY_OF_MONTH, daysUntilMeetingDay)
                                            meetingData.upcomingDate = todayCalendar.time

                                            if (meetingData.date.toDate() <= endDate) {
                                                weekMeetings.add(meetingData)
                                            }
                                        } catch (e: IllegalArgumentException) {
                                            Log.e(TAG, "Error calculating meeting date: ${e.message}")
                                        }
                                    }

                                    // Sort meetings by upcomingDate
                                    weekMeetings.sortBy { it.upcomingDate }

                                    if (weekMeetings.isEmpty()) {
                                        binding.tvNoMeetings.visibility = View.VISIBLE
                                        binding.rvMeetingsToday.visibility = View.GONE
                                    } else {
                                        binding.tvNoMeetings.visibility = View.GONE
                                        binding.rvMeetingsToday.visibility = View.VISIBLE
                                    }

                                    binding.loadingLayout.visibility = View.VISIBLE
                                    meetingAdapter.meetings = weekMeetings
                                    meetingAdapter.notifyDataSetChanged()
                                    binding.loadingLayout.visibility = View.GONE
                                    binding.layoutMainActivity.visibility = View.VISIBLE
                                }
                                .addOnFailureListener { exception ->
                                    Log.w(TAG, "Error getting meetings: ", exception)
                                }
                        }
                    }
                }
        }
    }

    private fun updateDots(currentPage: Int) {
        for (i in 0 until dotsCount) {
            dots[i]?.setImageDrawable(resources.getDrawable(R.drawable.ic_non_active_dot))
        }
        dots[currentPage]?.setImageDrawable(resources.getDrawable(R.drawable.ic_active_dot))
    }

    private fun setupClickListeners() {
        binding.btnFindTutor.setOnClickListener {
            Intent(this@StudentMainActivity, TutorSearchActivity::class.java).also {
                binding.layoutMainActivity.visibility = View.GONE
                binding.loadingLayout.visibility = View.VISIBLE
                startActivity(it)
            }
        }
        binding.ivUserImageDashboard.setOnClickListener{
            Intent(this@StudentMainActivity, StudentMainProfileActivity::class.java).also {
                binding.layoutMainActivity.visibility = View.GONE
                binding.loadingLayout.visibility = View.VISIBLE
                startActivity(it)
            }
        }
        binding.bottomNavigationBar.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
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
            binding.layoutMainActivity.visibility = View.GONE
            binding.loadingLayout.visibility = View.VISIBLE
            startActivity(intent)
        }
        binding.tvAgendaForToday.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            binding.layoutMainActivity.visibility = View.GONE
            binding.loadingLayout.visibility = View.VISIBLE
            startActivity(intent)
        }
    }
    private fun updateUIWithUserData(user: User) {
        binding.tvUserFirstNameDashboard.text = user.firstName
        Glide.with(this).load(user.profilePic).into(binding.ivUserImageDashboard)
    }

    private fun updateFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            val userId = auth.currentUser?.uid
            if (userId != null && token != null) {
                firestoreDb.collection("users").document(userId)
                    .update("fcmToken", token)
                    .addOnSuccessListener {
                        Log.d(TAG, "FCM token updated successfully")
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Error updating FCM token", exception)
                    }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.layoutMainActivity.visibility = View.GONE
        binding.loadingLayout.visibility = View.VISIBLE
        fetchThisWeeksMeetings()
        binding.bottomNavigationBar.selectedItemId = R.id.home
    }
}
