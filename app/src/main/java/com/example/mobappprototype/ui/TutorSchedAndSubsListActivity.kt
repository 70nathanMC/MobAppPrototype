package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobappprototype.Adapter.MeetingsAdapter
import com.example.mobappprototype.R
import com.example.mobappprototype.databinding.ActivityTutorProfileBinding
import com.example.mobappprototype.databinding.ActivityTutorSchedAndSubsListBinding
import com.example.mobappprototype.model.MeetingData
import com.example.mobappprototype.model.TutorData

class TutorSchedAndSubsListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTutorSchedAndSubsListBinding
    private lateinit var ibtnHomeFTutorSchedAndSubsList: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorSchedAndSubsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        binding.recyclerViewMeetings.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewMeetings.adapter = MeetingsAdapter(meetingsList)

        // Home button logic
        ibtnHomeFTutorSchedAndSubsList = findViewById(R.id.ibtnHomeFTutorSchedAndSubsList)
        ibtnHomeFTutorSchedAndSubsList.setOnClickListener {
            Intent(this, MainActivity::class.java).also {
                startActivity(it)
            }
        }

    }
}