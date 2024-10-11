package com.example.mobappprototype.ui

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobappprototype.databinding.ActivityEditMeetingBinding
import com.example.mobappprototype.model.MeetingForTutor
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class EditMeetingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditMeetingBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditMeetingBinding.inflate(layoutInflater)

        setContentView(binding.root)
        db = FirebaseFirestore.getInstance()
        Log.d("EditMeetingActivity", "Was this run? Before val meeting = intent.getParcelableExtra")
        val meeting = intent.getParcelableExtra<MeetingForTutor>("meeting")
            ?: throw IllegalArgumentException("Meeting data is missing")
        val meetingId = intent.getStringExtra("meetingId")
            ?: throw IllegalArgumentException("Meeting ID is missing")
        Log.d("EditMeetingActivity", "Meeting ID: $meetingId")
        // Populate views with meeting data
        fetchSubjectsFromFirestore(binding.spinnerSubject, meeting.subject)
        binding.etBranch.setText(meeting.branch)
        setSpinnerDaySelection(binding.spinnerDay, meeting.day)
        setTimePickerTime(binding.tpStartTime, meeting.startTime)
        setTimePickerTime(binding.tpEndTime, meeting.endTime)
        binding.etSlots.setText(String.format(Locale.getDefault(), "%d", meeting.slots))
        Log.d("EditMeetingActivity", "Was this run? before binding.btnSave.setOnClickListener")

        binding.btnSave.setOnClickListener {
            Log.d("EditMeetingActivity", "1")
            val subject = binding.spinnerSubject.selectedItem.toString() ?: ""
            Log.d("EditMeetingActivity", "1.11")
            val branch = binding.etBranch.text.toString() ?: ""
            val day = binding.spinnerDay.selectedItem.toString() ?: ""
            Log.d("EditMeetingActivity", "1.1")
            val startTimeHour = binding.tpStartTime.hour
            val startTimeMinute = binding.tpStartTime.minute
            val endTimeHour = binding.tpEndTime.hour
            val endTimeMinute = binding.tpEndTime.minute
            Log.d("EditMeetingActivity", "1.2")
            val startTimeAmPm = if (binding.tpStartTime.hour < 12) "AM" else "PM"
            val endTimeAmPm = if (binding.tpEndTime.hour < 12) "AM" else "PM"
            val startTime = String.format("%02d:%02d %s", startTimeHour, startTimeMinute, startTimeAmPm)
            val endTime = String.format("%02d:%02d %s", endTimeHour, endTimeMinute, endTimeAmPm)


            Log.d("EditMeetingActivity", "2")
            val slots = binding.etSlots.text.toString().toIntOrNull() ?: 0
            Log.d("EditMeetingActivity", "3")
            if (subject.isBlank() || branch.isBlank() || day.isBlank() ||
                startTime.isBlank() || endTime.isBlank() || slots <= 0
            ) {
                Toast.makeText(this, "Please fill in all fields correctly.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Log.d("EditMeetingActivity", "Was this run? Before val updatedMeetingData")
            val updatedMeetingData = hashMapOf(
                "subject" to subject,
                "branch" to branch,
                "day" to day,
                "startTime" to startTime,
                "endTime" to endTime,
                "slots" to slots,
                "slotsRemaining" to slots // Update slotsRemaining whenever slots is changed
            )
            Log.d("EditMeetingActivity", "Was this run? Before db.collection to update meeting document in Firestore")
            // Update meeting document in Firestore
            db.collection("meetings").document(meetingId)
                .update(updatedMeetingData as Map<String, Any>)
                .addOnSuccessListener {
                    Log.d("EditMeetingActivity", "Meeting updated successfully!")
                    Toast.makeText(this, "Meeting updated!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.e("EditMeetingActivity", "Error updating meeting", e)
                    Toast.makeText(this, "Error updating meeting: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    private fun fetchSubjectsFromFirestore(spinner: Spinner, selectedSubject: String) {
        db.collection("subjects")
            .get()
            .addOnSuccessListener { documents ->
                val subjectList = mutableListOf<String>()
                for (document in documents) {
                    val subjectName = document.getString("subjectName")
                    subjectName?.let { subjectList.add(it) }
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, subjectList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter

                // Set the spinner selection
                val selectedIndex = subjectList.indexOf(selectedSubject)
                if (selectedIndex != -1) {
                    spinner.setSelection(selectedIndex)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching subjects: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setSpinnerDaySelection(spinner: Spinner, selectedDay: String) {
        val adapter = spinner.adapter
        if (adapter is ArrayAdapter<*>) {
            val dayList = (0..<adapter.count).map { adapter.getItem(it) as String } // Get the list of days as strings
            val selectedIndex = dayList.indexOf(selectedDay) // Find the index in the string list
            if (selectedIndex != -1) {
                spinner.setSelection(selectedIndex)
            }
        }
    }

    private fun setTimePickerTime(timePicker: android.widget.TimePicker, time: String) {
        val (hour, minute) = time.split(":").map { it.toIntOrNull() ?: 0 }
        timePicker.hour = hour
        timePicker.minute = minute
    }
}
