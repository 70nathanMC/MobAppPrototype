package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobappprototype.Adapter.MeetingsAdapter
import com.example.mobappprototype.R
import com.example.mobappprototype.databinding.ActivityTutorSchedAndSubsListBinding
import com.example.mobappprototype.model.Meeting
import com.example.mobappprototype.model.MeetingData
import com.google.firebase.firestore.FirebaseFirestore

private const val TAG = "TutorSchedAndSubsListActivity"
class TutorSchedAndSubsListActivity : AppCompatActivity() {

    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var binding: ActivityTutorSchedAndSubsListBinding
    private lateinit var ibtnHomeFTutorSchedAndSubsList: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorSchedAndSubsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestoreDb = FirebaseFirestore.getInstance()
        val meetingsReference = firestoreDb
            .collection("meetings")
            //.limit(20)
            //.orderBy()
        meetingsReference.addSnapshotListener { snapshot, exception ->
            if (exception != null || snapshot == null) {
                Log.e(TAG, "Exception when querying posts", exception)
                return@addSnapshotListener
            }
            val meetingList = snapshot.toObjects(Meeting::class.java)
//            meetings.addAll(meetingList)
            for (meeting in meetingList) {
                Log.i(TAG,"Meeting ${meeting}")
            }
        }

        val subjectNameList = arrayOf("Calculus - Limits", "Calculus - Integration", "Calculus - Differentiation", "Calculus - Sequences", "Calculus - Series", "Calculus - Vector")
        val dayList = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val timeList = arrayOf("7:30 AM - 9:30 AM", "10:00 AM - 12:00 PM", "2:00 PM - 4:00 PM", "9:00 AM - 11:00 AM", "1:00 PM - 3:00 PM", "8:00 AM - 10:00 AM")
        val slotsList = arrayOf("3 Slots", "2 Slots", "5 Slots", "4 Slots", "1 Slot", "3 Slots")


        val meetingsList = mutableListOf<MeetingData>()
        for (i in subjectNameList.indices) {
            meetingsList.add(
                MeetingData(
                    subjectNameList[i],
                    dayList[i],
                    timeList[i],
                    slotsList[i]
                )
            )
        }

        // Setup RecyclerView
        binding.rvMeetings.layoutManager = LinearLayoutManager(this)
        binding.rvMeetings.adapter = MeetingsAdapter(meetingsList)

        // Home button logic
        ibtnHomeFTutorSchedAndSubsList = findViewById(R.id.ibtnHomeFTutorSchedAndSubsList)
        ibtnHomeFTutorSchedAndSubsList.setOnClickListener {
            Intent(this, StudentMainActivity::class.java).also {
                startActivity(it)
            }
        }

    }
}