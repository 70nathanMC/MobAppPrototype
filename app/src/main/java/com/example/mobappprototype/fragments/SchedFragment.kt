package com.example.mobappprototype.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobappprototype.Adapter.ScheduleAdapter
import com.example.mobappprototype.R
import com.example.mobappprototype.model.MeetingData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SchedFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var scheduleAdapter: ScheduleAdapter
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sched, container, false)
        firestoreDb = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        recyclerView = view.findViewById(R.id.rvSchedule) // Make sure this ID matches your XML
        recyclerView.layoutManager = LinearLayoutManager(context)
        scheduleAdapter = ScheduleAdapter(emptyList())
        recyclerView.adapter = scheduleAdapter

        val tutorUid = activity?.intent?.getStringExtra("TUTOR_UID")
        val userUID = auth.currentUser?.uid
        if (tutorUid != null) {
            // If tutorUid is present in intent extras, use that (Student viewing Tutor's schedule)
            fetchSchedule(tutorUid)
        } else if (userUID != null) {
            // If tutorUid is not present, and user is logged in, fetch their own schedule (Tutor viewing own schedule)
            firestoreDb.collection("users").document(userUID)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists() && document.getString("role") == "Tutor") {
                        fetchSchedule(userUID) // Fetch schedule using the user's own UID
                    } else {
                        Log.e("SchedFragment", "User is not a Tutor")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("SchedFragment", "Error checking user role", exception)
                }
        } else {
            Log.e("SchedFragment", "Tutor UID not found and user not logged in")
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SchedFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    private fun fetchSchedule(tutorUid: String) {
        firestoreDb.collection("meetings")
            .whereEqualTo("tutorId", tutorUid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val meetings = mutableListOf<MeetingData>()
                for (document in querySnapshot) {
                    val meeting = MeetingData(
                        document.id, // Get the meeting ID
                        document.getString("subject") ?: "",
                        document.getString("branch") ?: "",
                        document.getString("day") ?: "",
                        document.getString("startTime") ?: "",
                        document.getString("endTime") ?: "",
                        document.getLong("slots")?.toInt() ?: 0,
                        document.getLong("slotsRemaining")?.toInt() ?: 0,
                        document.get("participants") as? List<String> ?: emptyList(),
                        document.getString("tutorId") ?: ""
                    )
                    meetings.add(meeting)
                }
                meetings.sortBy { meeting ->
                    when (meeting.day.lowercase()) {
                        "monday" -> 1
                        "tuesday" -> 2
                        "wednesday" -> 3
                        "thursday" -> 4
                        "friday" -> 5
                        "saturday" -> 6
                        "sunday" -> 7
                        else -> 8 // Handle cases with invalid day names
                    }
                }
                scheduleAdapter.updateMeetings(meetings)
            }
            .addOnFailureListener { exception ->
                Log.e("SchedFragment", "Error fetching schedule", exception)
            }
    }
}