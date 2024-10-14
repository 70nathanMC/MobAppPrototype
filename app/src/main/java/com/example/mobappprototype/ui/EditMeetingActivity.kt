package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobappprototype.R
import com.example.mobappprototype.databinding.ActivityEditMeetingBinding
import com.example.mobappprototype.model.MeetingForTutor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

private const val TAG = "EditMeetingActivity"
class EditMeetingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditMeetingBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditMeetingBinding.inflate(layoutInflater)

        setContentView(binding.root)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance() // Initialize auth here
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

        binding.btnSave.setOnClickListener {
            val subject = binding.spinnerSubject.selectedItem.toString() ?: ""
            val branch = binding.etBranch.text.toString() ?: ""
            val day = binding.spinnerDay.selectedItem.toString() ?: ""
            val startTimeHour = binding.tpStartTime.hour
            val startTimeMinute = binding.tpStartTime.minute
            val endTimeHour = binding.tpEndTime.hour
            val endTimeMinute = binding.tpEndTime.minute
            val formattedStartTimeHour = if (startTimeHour == 0) 12 else if (startTimeHour > 12) startTimeHour - 12 else startTimeHour
            val formattedEndTimeHour = if (endTimeHour == 0) 12 else if (endTimeHour > 12) endTimeHour - 12 else endTimeHour
            val startTimeAmPm = if (startTimeHour < 12) "AM" else "PM"
            val endTimeAmPm = if (endTimeHour < 12) "AM" else "PM"
            val startTime = String.format("%02d:%02d %s", formattedStartTimeHour, startTimeMinute, startTimeAmPm)
            val endTime = String.format("%02d:%02d %s", formattedEndTimeHour, endTimeMinute, endTimeAmPm)


            val slots = binding.etSlots.text.toString().toIntOrNull() ?: 0
            if (subject.isBlank() || branch.isBlank() || day.isBlank() ||
                startTime.isBlank() || endTime.isBlank() || slots <= 0
            ) {
                Toast.makeText(this, "Please fill in all fields correctly.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val updatedMeetingData = hashMapOf(
                "subject" to subject,
                "branch" to branch,
                "day" to day,
                "startTime" to startTime,
                "endTime" to endTime,
                "slots" to slots,
                "slotsRemaining" to slots // Update slotsRemaining whenever slots is changed
            )
            val newSubject = binding.spinnerSubject.selectedItem.toString()
            db.collection("meetings").document(meetingId)
                .update(updatedMeetingData as Map<String, Any>)
                .addOnSuccessListener {
                    Log.d("EditMeetingActivity", "Meeting updated successfully!")
                    Toast.makeText(this, "Meeting updated!", Toast.LENGTH_SHORT).show()
                    val userId = auth.currentUser?.uid ?: return@addOnSuccessListener
                    val oldSubject = meeting.subject

                    if (newSubject != oldSubject) {
                        // 1. Add tutorUID to the new subject's relatedTutors array
                        db.collection("subjects").whereEqualTo("subjectName", newSubject)
                            .get()
                            .addOnSuccessListener { documents ->
                                if (!documents.isEmpty) {
                                    // Get the first document (assuming there's only one with that subjectName)
                                    val newSubjectDocument = documents.first()
                                    newSubjectDocument.reference.update("relatedTutors", FieldValue.arrayUnion(userId))
                                        .addOnSuccessListener {
                                            Log.d(TAG, "Tutor $userId added to subject $newSubject")

                                            // 2. Check if there are other meetings with the old subject
                                            db.collection("meetings")
                                                .whereEqualTo("tutorId", userId)
                                                .whereEqualTo("subject", oldSubject)
                                                .get()
                                                .addOnSuccessListener { oldSubjectDocuments ->
                                                    if (oldSubjectDocuments.isEmpty) {
                                                        // 3. If no other meetings with the old subject, remove tutorUID from it
                                                        db.collection("subjects").whereEqualTo("subjectName", oldSubject)
                                                            .get()
                                                            .addOnSuccessListener { oldSubjectDocs ->
                                                                if (!oldSubjectDocs.isEmpty) {
                                                                    val oldSubjectDocument = oldSubjectDocs.first()
                                                                    oldSubjectDocument.reference.update("relatedTutors", FieldValue.arrayRemove(userId))
                                                                        .addOnSuccessListener {
                                                                            Log.d(TAG, "Tutor $userId removed from subject $oldSubject")
                                                                            finish()
                                                                        }
                                                                        .addOnFailureListener { e ->
                                                                            Log.w(TAG, "Error removing tutor from subject: ${e.message}")
                                                                            Toast.makeText(this, "Error removing tutor from subject: ${e.message}", Toast.LENGTH_SHORT).show()
                                                                        }
                                                                } else {
                                                                    Log.w(TAG, "Old subject document not found")
                                                                    Toast.makeText(this, "Old subject document not found", Toast.LENGTH_SHORT).show()
                                                                }
                                                            }
                                                            .addOnFailureListener { e ->
                                                                Log.w(TAG, "Error getting old subject document: ${e.message}")
                                                                Toast.makeText(this, "Error getting old subject document: ${e.message}", Toast.LENGTH_SHORT).show()
                                                            }
                                                    } else {
                                                        // There are other meetings with the old subject, so no need to remove the tutorUID
                                                        finish()
                                                    }
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.w(TAG, "Error checking for meetings with old subject: ${e.message}")
                                                    Toast.makeText(this, "Error checking for meetings with old subject: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w(TAG, "Error adding tutor to subject: ${e.message}")
                                            Toast.makeText(this, "Error adding tutor to subject: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    Log.w(TAG, "New subject document not found")
                                    Toast.makeText(this, "New subject document not found", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error getting new subject document: ${e.message}")
                                Toast.makeText(this, "Error getting new subject document: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // Subject not changed, no need to update relatedTutors
                        finish()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("EditMeetingActivity", "Error updating meeting", e)
                    Toast.makeText(this, "Error updating meeting: ${e.message}", Toast.LENGTH_SHORT).show()
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
