package com.example.mobappprototype.ui

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobappprototype.databinding.ActivityCreateMeetingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class CreateMeetingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateMeetingBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCreateMeetingBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        fetchSubjectsFromFirestore(binding.spinnerSubject) // Call the function to populate the Spinner

        binding.btnCreate.setOnClickListener {
            val subject = binding.spinnerSubject.selectedItem.toString()
            val branch = binding.etBranch.text.toString()
            val day = binding.spinnerDay.selectedItem.toString()
            val startTimeHour = binding.tpStartTime.hour
            val startTimeMinute = binding.tpStartTime.minute
            val endTimeHour = binding.tpEndTime.hour
            val endTimeMinute = binding.tpEndTime.minute
            val startTimeAmPm = if (binding.tpStartTime.hour < 12) "AM" else "PM"
            val endTimeAmPm = if (binding.tpEndTime.hour < 12) "AM" else "PM"

            val startTime = String.format("%02d:%02d %s", startTimeHour, startTimeMinute, startTimeAmPm)
            val endTime = String.format("%02d:%02d %s", endTimeHour, endTimeMinute, endTimeAmPm)
            val slots = binding.etSlots.text.toString().toIntOrNull() ?: 0 // Default to 0 if invalid

            if (subject.isBlank() || branch.isBlank() || day.isBlank() ||
                startTime.isBlank() || endTime.isBlank() || slots <= 0) {
                Toast.makeText(this, "Please fill in all fields correctly.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = auth.currentUser?.uid ?: return@setOnClickListener

            val meetingData = hashMapOf(
                "tutorId" to userId,
                "subject" to subject,
                "branch" to branch,
                "day" to day,
                "startTime" to startTime,
                "endTime" to endTime,
                "slots" to slots,
                "slotsRemaining" to slots, // Initially, slotsRemaining is equal to total slots
                "participants" to listOf(userId) // Initially, the tutor is a participant
            )

            db.collection("meetings")
                .add(meetingData)
                .addOnSuccessListener { documentReference ->
                    // Meeting created successfully
                    Toast.makeText(this, "Meeting created!", Toast.LENGTH_SHORT).show()

                    // Update the tutor's document with the meeting ID
                    val meetingId = documentReference.id
                    db.collection("users").document(userId)
                        .collection("tutorData") // Access the subcollection
                        .document("data") // You can use a single document to hold the data
                        .update("meetings", FieldValue.arrayUnion(meetingId))
                        .addOnSuccessListener {
                            Log.d("CreateMeetingActivity", "Meeting ID added to tutor's document: $meetingId")
                            finish() // Optionally close the activity
                        }
                        .addOnFailureListener { e ->
                            // Handle the error (e.g., show a Toast)
                            Toast.makeText(this, "Error updating tutor's meetings: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    // Handle the error (e.g., show a Toast)
                    Toast.makeText(this, "Error creating meeting: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    private fun fetchSubjectsFromFirestore(spinner: Spinner) {
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
            }
            .addOnFailureListener { exception ->
                // Handle any errors that occurred while fetching the subjects
                Toast.makeText(this, "Error fetching subjects: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}