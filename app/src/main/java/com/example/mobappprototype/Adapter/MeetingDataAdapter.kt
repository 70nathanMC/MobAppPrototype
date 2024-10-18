package com.example.mobappprototype.Adapter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.RecyclerView
import com.example.mobappprototype.R
import com.example.mobappprototype.databinding.ListItemMeetingsBinding // Make sure this is the correct import for your layout
import com.example.mobappprototype.model.MeetingData
import com.example.mobappprototype.ui.ChatActivity
import com.example.mobappprototype.ui.TutorSchedAndSubsListActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import java.util.jar.Manifest


private const val TAG = "MeetingDataAdapter"
class MeetingDataAdapter(private val meetings: List<MeetingData>) :
    RecyclerView.Adapter<MeetingDataAdapter.MeetingViewHolder>() {

    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var auth: FirebaseAuth


    inner class MeetingViewHolder(val binding: ListItemMeetingsBinding) : RecyclerView.ViewHolder(binding.root) {
        val btnJoinMeeting: MaterialButton = binding.btnJoinMeeting
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeetingViewHolder {
        val binding = ListItemMeetingsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        firestoreDb = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        return MeetingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MeetingViewHolder, position: Int) {
        val meeting = meetings[position]

        holder.binding.tvMeetingSubject.text = meeting.subject
        holder.binding.tvMeetingBranch.text = meeting.branch
        holder.binding.tvMeetingDay.text = meeting.day
        holder.binding.tvMeetingScheduleStart.text = meeting.startTime
        holder.binding.tvMeetingScheduleEnd.text = meeting.endTime
        holder.btnJoinMeeting.setOnClickListener {
            val meetingId = meeting.id
            val userUID = auth.currentUser?.uid

            if (meetingId != null && userUID != null) {
                val meetingRef = firestoreDb.collection("meetings").document(meetingId)
                meetingRef.get().addOnSuccessListener { meetingDocument ->
                    val slots = meetingDocument.getLong("slots")?.toInt() ?: 0
                    val participants = meetingDocument.get("participants") as? List<String> ?: emptyList()

                    // Calculate slotsRemaining
                    val slotsRemaining = slots - (participants.size - 1)
                    if (slotsRemaining > 0) {
                        val studentMeetingRef = firestoreDb.collection("studentMeetings").document(userUID)

                        // Check if the student already has a document in studentMeetings
                        studentMeetingRef.get()
                            .addOnSuccessListener { studentMeetingDocument ->

                                meetingRef.update("slotsRemaining", slotsRemaining)
                                    .addOnSuccessListener {
                                        Log.d(TAG, "Successfully updated slotsRemaining for meeting: $meetingId")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(TAG, "Error updating slotsRemaining", e)
                                    }

                                if (studentMeetingDocument.exists()) {
                                    // Student document exists, check if the meeting is already in the array
                                    val meetingIds = studentMeetingDocument.get("meetingIds") as? List<String> ?: emptyList()
                                    if (meetingIds.contains(meetingId)) {
                                        // Student has already joined this meeting
                                        Log.d(
                                            TAG,
                                            "Student $userUID has already joined meeting $meetingId"
                                        )
                                        Toast.makeText(
                                            holder.itemView.context,
                                            "You have already joined this meeting",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        // Launch ChatActivity here
                                        val intent =
                                            Intent(
                                                holder.itemView.context,
                                                ChatActivity::class.java
                                            )
                                        intent.putExtra("meetingId", meetingId)
                                        holder.itemView.context.startActivity(intent)
                                    } else {
                                        // Add the meeting to the student's meetingIds array
                                        joinMeetingAndUpdateStudentMeetings(
                                            meetingRef,
                                            studentMeetingRef,
                                            userUID,
                                            meetingId,
                                            holder,
                                            meeting
                                        )
                                    }
                                } else {
                                    // Student document doesn't exist, create it and add the meeting
                                    joinMeetingAndUpdateStudentMeetings(
                                        meetingRef,
                                        studentMeetingRef,
                                        userUID,
                                        meetingId,
                                        holder,
                                        meeting
                                    )
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e(
                                    TAG,
                                    "Error checking for existing student meeting document",
                                    e
                                )
                            }
                    } else {
                        Toast.makeText(holder.itemView.context, "This meeting is full.", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Error checking slotsRemaining", e)
                    }
            }
        }
    }

    private fun joinMeetingAndUpdateStudentMeetings(
        meetingRef: DocumentReference,
        studentMeetingRef: DocumentReference,
        userUID: String,
        meetingId: String,
        holder: MeetingViewHolder,
        meeting: MeetingData
    ) {
        meetingRef.update("participants", FieldValue.arrayUnion(userUID))
            .addOnSuccessListener {
                Log.d(TAG, "Successfully joined meeting: $meetingId")

                val studentMeetingData = hashMapOf("meetingIds" to FieldValue.arrayUnion(meetingId))

                studentMeetingRef.set(studentMeetingData, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d(TAG, "Meeting $meetingId added to student $userUID")

                        // Create or update chat document (this part remains the same)
                        val chatRef = firestoreDb.collection("chats").document(meetingId)
                        chatRef.get().addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists()) {
                                // Chat document already exists, add the user to participants if not already present
                                val participants =
                                    documentSnapshot.get("participants") as? List<String>
                                        ?: emptyList()
                                if (!participants.contains(userUID)) {
                                    chatRef.update(
                                        "participants",
                                        FieldValue.arrayUnion(userUID)
                                    )
                                        .addOnSuccessListener {
                                            Log.d(
                                                TAG,
                                                "Successfully added user to chat: $meetingId"
                                            )
                                            // Launch ChatActivity here
                                            val intent = Intent(
                                                holder.itemView.context,
                                                ChatActivity::class.java
                                            )
                                            intent.putExtra("meetingId", meetingId)
                                            holder.itemView.context.startActivity(intent)
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e(TAG, "Error adding user to chat", e)
                                        }
                                } else {
                                    val intent = Intent(
                                        holder.itemView.context,
                                        ChatActivity::class.java
                                    )
                                    intent.putExtra("meetingId", meetingId)
                                    holder.itemView.context.startActivity(intent)
                                }
                            } else {
                                // Chat document doesn't exist, create a new one
                                val chatData = hashMapOf(
                                    "meetingID" to meetingId,
                                    "participants" to listOf(
                                        userUID,
                                        meeting.tutorId
                                    )
                                )
                                chatRef.set(chatData)
                                    .addOnSuccessListener {
                                        Log.d(
                                            TAG,
                                            "Successfully created chat: $meetingId"
                                        )
                                        // Launch ChatActivity here
                                        val intent = Intent(
                                            holder.itemView.context,
                                            ChatActivity::class.java
                                        )
                                        intent.putExtra("meetingId", meetingId)
                                        holder.itemView.context.startActivity(intent)
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(TAG, "Error creating chat", e)
                                    }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error adding meeting to student", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error joining meeting", e)
            }
    }


    override fun getItemCount(): Int = meetings.size
}