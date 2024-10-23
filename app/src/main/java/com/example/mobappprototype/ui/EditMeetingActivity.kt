package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobappprototype.R
import com.example.mobappprototype.databinding.ActivityEditMeetingBinding
import com.example.mobappprototype.model.MeetingForTutor
import com.example.mobappprototype.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale

private const val TAG = "EditMeetingActivity"
class EditMeetingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditMeetingBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var actvDay: AutoCompleteTextView
    private lateinit var actvSubjectName: AutoCompleteTextView

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
        val daysList = resources.getStringArray(R.array.days_of_week)
        val arrayAdapter = ArrayAdapter(this, R.layout.dropdown_item, daysList)
        actvDay = findViewById(R.id.actvDay)
        actvDay.setAdapter(arrayAdapter)
        actvDay.setText(meeting.day, false)
        actvDay.dropDownVerticalOffset = actvDay.height

        actvSubjectName = findViewById(R.id.actvSubjectName)
        fetchSubjectsFromFirestore(actvSubjectName, meeting)


        binding.etBranch.setText(meeting.branch)
        setTimePickerTime(binding.tpStartTime, meeting.startTime)
        setTimePickerTime(binding.tpEndTime, meeting.endTime)
        binding.etSlots.setText(String.format(Locale.getDefault(), "%d", meeting.slots))

        binding.ivBackFEditMeeting.setOnClickListener {
            val intent = Intent(this, TutorMainActivity::class.java)
            startActivity(intent)
        }

        binding.btnSave.setOnClickListener {
            val subject = binding.actvSubjectName.text.toString() ?: ""
            val branch = binding.etBranch.text.toString() ?: ""
            val day = binding.actvDay.text.toString() ?: ""
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
            val meetingLink = binding.etMeetingLink.text.toString().trim()


            val slots = binding.etSlots.text.toString().toIntOrNull() ?: 0
            if (subject.isBlank() || branch.isBlank() || day.isBlank() ||
                startTime.isBlank() || endTime.isBlank() || slots <= 0
            ) {
                Toast.makeText(this, "Please fill in all fields correctly.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val startTimeCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, startTimeHour)
                set(Calendar.MINUTE, startTimeMinute)
            }

            val endTimeCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, endTimeHour)
                set(Calendar.MINUTE, endTimeMinute)
            }

            val dayOfWeek = when (day) {
                "Monday" -> Calendar.MONDAY
                "Tuesday" -> Calendar.TUESDAY
                "Wednesday" -> Calendar.WEDNESDAY
                "Thursday" -> Calendar.THURSDAY
                "Friday" -> Calendar.FRIDAY
                "Saturday" -> Calendar.SATURDAY
                "Sunday" -> Calendar.SUNDAY
                else -> throw IllegalArgumentException("Invalid day of the week: $day")
            }

            val dateCalendar = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, dayOfWeek)
            }

            val userId = auth.currentUser?.uid ?: return@setOnClickListener
            db.collection("users").document(userId)
            if (userId != null) {
                val userRef = db.collection("users").document(userId)
                userRef.get().addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject(User::class.java)
                        val userFullName = user?.fullName.toString()
                        val profilePic = user?.profilePic.toString()

                        val updatedMeetingData = hashMapOf(
                            "subject" to subject,
                            "branch" to branch,
                            "day" to day,
                            "startTime" to startTime,
                            "endTime" to endTime,
                            "slots" to slots,
                            "slotsRemaining" to slots,
                            "startTimeTimestamp" to com.google.firebase.Timestamp(startTimeCalendar.time),
                            "endTimeTimestamp" to com.google.firebase.Timestamp(endTimeCalendar.time),
                            "date" to com.google.firebase.Timestamp(dateCalendar.time),
                            "tutorFullName" to userFullName,
                            "tutorProfilePic" to profilePic,
                            "meetingLink" to meetingLink
                        )

                        val newSubject = binding.actvSubjectName.text.toString()
                        db.collection("meetings").document(meetingId)
                            .update(updatedMeetingData as Map<String, Any>)
                            .addOnSuccessListener {
                                Log.d("EditMeetingActivity", "Meeting updated successfully!")
                                Toast.makeText(this, "Meeting updated!", Toast.LENGTH_SHORT).show()
                                val userId = auth.currentUser?.uid ?: return@addOnSuccessListener
                                val oldSubject = meeting.subject

                                db.collection("users").document(userId)
                                    .collection("tutorData")
                                    .document("data")
                                    .get()
                                    .addOnSuccessListener { documentSnapshot ->
                                        if (documentSnapshot.exists()) {
                                            val meetingsArray = documentSnapshot.get("meetings") as? List<String> ?: emptyList()

                                            // 2. Check if the meetingId is already in the meetings array
                                            if (!meetingsArray.contains(meetingId)) {
                                                // 3. If not present, add the meetingId to the array
                                                documentSnapshot.reference.update("meetings", FieldValue.arrayUnion(meetingId))
                                                    .addOnSuccessListener {
                                                        Log.d(TAG, "Meeting ID added to tutor's document: $meetingId")
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Log.w(TAG, "Error adding meeting ID to tutor's document: ${e.message}")
                                                    }
                                            } else {
                                                Log.d(TAG, "Meeting ID already exists in tutor's document: $meetingId")
                                            }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(TAG, "Error getting tutor's data document: ${e.message}")
                                    }

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
                                    finish()
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("EditMeetingActivity", "Error updating meeting", e)
                                Toast.makeText(this, "Error updating meeting: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
        }

    }
    private fun fetchSubjectsFromFirestore(autoCompleteTextView: AutoCompleteTextView, meeting: MeetingForTutor) {
        db.collection("subjects")
            .get()
            .addOnSuccessListener { documents ->
                val subjectList = mutableListOf<String>()
                for (document in documents) {
                    val subjectName = document.getString("subjectName")
                    subjectName?.let { subjectList.add(it) }
                }
                val adapter = ArrayAdapter(this, R.layout.dropdown_item, subjectList)
                autoCompleteTextView.setAdapter(adapter)

                val selectedIndex = subjectList.indexOf(meeting.subject)
                if (selectedIndex != -1) {
                    autoCompleteTextView.setText(subjectList[selectedIndex], false)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching subjects: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setTimePickerTime(timePicker: android.widget.TimePicker, time: String) {
        val (hour, minute) = time.split(":").map { it.toIntOrNull() ?: 0 }
        timePicker.hour = hour
        timePicker.minute = minute
    }
}
