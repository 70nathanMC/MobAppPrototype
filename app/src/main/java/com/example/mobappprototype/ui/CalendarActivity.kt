package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.mobappprototype.databinding.ActivityCalendarBinding
import com.example.mobappprototype.model.MeetingData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobappprototype.Adapter.CalendarMeetingAdapter
import com.example.mobappprototype.Adapter.MeetingAdapter
import com.example.mobappprototype.R

class CalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarBinding
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var meetingAdapter: CalendarMeetingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvMeetingsForDate.visibility = View.GONE
        binding.tvNoMeetings.visibility = View.GONE
        binding.loadingLayout.visibility = View.VISIBLE

        firestoreDb = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            fetchMeetingsForDate(selectedDate)
        }
        meetingAdapter = CalendarMeetingAdapter(listOf())
        binding.rvMeetingsForDate.adapter = meetingAdapter
        binding.rvMeetingsForDate.layoutManager = LinearLayoutManager(this)

        val currentDate = Calendar.getInstance()
        binding.calendarView.date = currentDate.timeInMillis
        fetchMeetingsForDate(currentDate)

        binding.bottomNavigationBar.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    val intent = Intent(this, StudentMainActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.messages -> {
                    val intent = Intent(this, InboxActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.profile -> {
                    val intent = Intent(this, StudentMainProfileActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchMeetingsForDate(selectedDate: Calendar) {
        Log.d(
            "CalendarActivity",
            "Fetching meetings for date: ${selectedDate.time}"
        )

        binding.rvMeetingsForDate.visibility = View.GONE
        binding.tvNoMeetings.visibility = View.GONE
        binding.loadingLayout.visibility = View.VISIBLE

        val userUID = auth.currentUser?.uid
        if (userUID != null) {
            firestoreDb.collection("studentMeetings").document(userUID)
                .get()
                .addOnSuccessListener { studentMeetingsDocument ->
                    if (studentMeetingsDocument.exists()) {
                        val meetingIds = studentMeetingsDocument.get("meetingIds") as? List<String>
                        Log.d(
                            "CalendarActivity",
                            "Meeting IDs: $meetingIds"
                        )
                        if (meetingIds != null) {
                            fetchMeetingsByIds(meetingIds, selectedDate)
                        } else {
                            Log.d("CalendarActivity", "No meetings found for user $userUID")
                            binding.rvMeetingsForDate.visibility = View.GONE
                            binding.loadingLayout.visibility = View.GONE
                            binding.tvNoMeetings.visibility = View.VISIBLE
                        }
                    } else {
                        Log.d("CalendarActivity", "No meetings found for user $userUID")
                        binding.rvMeetingsForDate.visibility = View.GONE
                        binding.loadingLayout.visibility = View.GONE
                        binding.tvNoMeetings.visibility = View.VISIBLE
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("CalendarActivity", "Error fetching meeting IDs", exception)
                }
        }
    }

    private fun fetchMeetingsByIds(meetingIds: List<String>, selectedDate: Calendar) {
        val meetingsForDate = mutableListOf<MeetingData>()
        val selectedDayOfWeek = selectedDate.get(Calendar.DAY_OF_WEEK)
        var meetingsFetched = 0

        for (meetingId in meetingIds) {
            Log.d("CalendarActivity", "Fetching meeting with ID: $meetingId")

            firestoreDb.collection("meetings").document(meetingId)
                .get()
                .addOnSuccessListener { meetingDocument ->
                    if (meetingDocument.exists()) {
                        val meetingData = meetingDocument.toObject(MeetingData::class.java)
                        meetingData?.let {
                            val meetingDayOfWeek = getDayOfWeekFromString(it.day)

                            // Check if the meeting is on the selectedDayOfWeek
                            if (meetingDayOfWeek == selectedDayOfWeek) {
                                // Calculate upcomingDate relative to selectedDate
                                val meetingCalendar = Calendar.getInstance()
                                meetingCalendar.time = selectedDate.time
                                meetingCalendar.set(Calendar.DAY_OF_WEEK, meetingDayOfWeek)
                                if (meetingCalendar.before(selectedDate)) {
                                    meetingCalendar.add(Calendar.DAY_OF_MONTH, 7)
                                }
                                it.upcomingDate = meetingCalendar.time
                                it.id = meetingDocument.id // Set the ID here

                                meetingsForDate.add(it)
                            }
                        }
                    } else {
                        Log.e("CalendarActivity", "Meeting document with ID $meetingId does not exist")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("CalendarActivity", "Error fetching meeting with ID $meetingId", exception)
                }
                .addOnCompleteListener {
                    meetingsFetched++
                    if (meetingsFetched == meetingIds.size) {
                        meetingAdapter.updateMeetings(meetingsForDate)
                        if (meetingsForDate.isEmpty()) {
                            binding.rvMeetingsForDate.visibility = View.GONE
                            binding.tvNoMeetings.visibility = View.VISIBLE
                        } else {
                            binding.tvNoMeetings.visibility = View.GONE
                            binding.rvMeetingsForDate.visibility = View.VISIBLE
                        }
                        binding.loadingLayout.visibility = View.GONE
                    }
                }
        }
    }

    private fun getDayOfWeekFromString(day: String): Int {
        return when (day.lowercase()) {
            "sunday" -> Calendar.SUNDAY
            "monday" -> Calendar.MONDAY
            "tuesday" -> Calendar.TUESDAY
            "wednesday" -> Calendar.WEDNESDAY
            "thursday" -> Calendar.THURSDAY
            "friday" -> Calendar.FRIDAY
            "saturday" -> Calendar.SATURDAY
            else -> throw IllegalArgumentException("Invalid day string: $day")
        }
    }
}