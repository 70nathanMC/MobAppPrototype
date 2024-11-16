package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.mobappprototype.R
import com.example.mobappprototype.ViewModel.UserViewModel
import com.example.mobappprototype.databinding.ActivityCreateMeetingBinding
import com.example.mobappprototype.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.reflect.Array.set
import java.util.Calendar

private const val TAG = "CreateMeetingActivity"
class CreateMeetingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateMeetingBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var actvDay: AutoCompleteTextView
    private lateinit var actvSubjectName: AutoCompleteTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCreateMeetingBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        supportActionBar?.hide()
        val daysList = resources.getStringArray(R.array.days_of_week)
        val arrayAdapter1 = ArrayAdapter(this, R.layout.dropdown_item, daysList)
        actvDay = findViewById(R.id.actvDay)
        actvDay.setAdapter(arrayAdapter1)
        actvDay.dropDownVerticalOffset = actvDay.height

        actvSubjectName = findViewById(R.id.actvSubjectName)
        fetchSubjectsFromFirestore(actvSubjectName)

        binding.btnCreateMeeting.setOnClickListener {

            val subject = binding.actvSubjectName.text.toString().trim()
            val branch = binding.etBranch.text.toString().trim()
            val day = binding.actvDay.text.toString().trim()
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
            val meetingSite = binding.etMeetingSite.text.toString().trim()

            if (subject.isBlank() || branch.isBlank() || day.isBlank() ||
                startTime.isBlank() || endTime.isBlank() || slots <= 0) {
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

                        val meetingData = hashMapOf(
                            "tutorId" to userId,
                            "subject" to subject,
                            "branch" to branch,
                            "day" to day,
                            "startTime" to startTime,
                            "endTime" to endTime,
                            "slots" to slots,
                            "slotsRemaining" to slots, // Initially, slotsRemaining is equal to total slots
                            "participants" to listOf(userId),
                            "startTimeTimestamp" to com.google.firebase.Timestamp(startTimeCalendar.time),
                            "endTimeTimestamp" to com.google.firebase.Timestamp(endTimeCalendar.time),
                            "date" to com.google.firebase.Timestamp(dateCalendar.time),
                            "tutorFullName" to userFullName,
                            "tutorProfilePic" to profilePic,
                            "meetingSite" to meetingSite
                        )

                        db.collection("meetings")
                            .add(meetingData)
                            .addOnSuccessListener { documentReference ->
                                Toast.makeText(this, "Meeting created!", Toast.LENGTH_SHORT).show()

                                val meetingId = documentReference.id
                                db.collection("users").document(userId)
                                    .collection("tutorData") // Access the subcollection
                                    .document("data") // You can use a single document to hold the data
                                    .update("meetings", FieldValue.arrayUnion(meetingId))
                                    .addOnSuccessListener {
                                        Log.d("CreateMeetingActivity", "Meeting ID added to tutor's document: $meetingId")
                                        db.collection("subjects").whereEqualTo("subjectName", subject)
                                            .get()
                                            .addOnSuccessListener { subjectDocuments ->
                                                if (!subjectDocuments.isEmpty) {
                                                    val subjectDocument = subjectDocuments.first() // Get the first document with the matching subjectName
                                                    subjectDocument.reference.update("relatedTutors", FieldValue.arrayUnion(userId))
                                                        .addOnSuccessListener {
                                                            Log.d(TAG, "Tutor $userId added to subject $subject")
                                                            finish()
                                                        }
                                                        .addOnFailureListener { e ->
                                                            Log.w(TAG, "Error adding tutor to subject: ${e.message}")
                                                            Toast.makeText(this, "Error adding tutor to subject: ${e.message}", Toast.LENGTH_SHORT).show()
                                                        }
                                                } else {
                                                    Log.w(TAG, "Subject document not found for $subject")
                                                    Toast.makeText(this, "Subject document not found for $subject", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Log.w(TAG, "Error fetching subject document: ${e.message}")
                                                Toast.makeText(this, "Error fetching subject document: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Error updating tutor's meetings: ${e.message}", Toast.LENGTH_SHORT).show()
                                        Log.d(TAG, "Error updating tutor's meetings: ${e.message}")
                                    }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error updating tutor's meetings: ${e.message}", Toast.LENGTH_SHORT).show()
                                Log.d(TAG, "Error updating tutor's meetings2: ${e.message}")
                            }
                    }
                }
            }
        }
        binding.ivBackFCreateMeeting.setOnClickListener {
            val intent = Intent(this, TutorMainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun fetchSubjectsFromFirestore(autoCompleteTextView: AutoCompleteTextView) {
        db.collection("subjects")
            .get()
            .addOnSuccessListener { documents ->
                val subjectList = mutableListOf<String>()
                for (document in documents) {
                    val subjectName = document.getString("subjectName")
                    subjectName?.let { subjectList.add(it) }
                }
                val adapter = ArrayAdapter(this, R.layout.dropdown_item, subjectList) // Use the same dropdown_item layout
                autoCompleteTextView.setAdapter(adapter)
            }
            .addOnFailureListener { exception ->
                // Handle any errors that occurred while fetching the subjects
                Toast.makeText(this, "Error fetching subjects: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Error updating tutor's fetching subjects: ${exception.message}")
            }
    }
}