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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
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

                // Update participants array in meetings collection
                meetingRef.update("participants", FieldValue.arrayUnion(userUID))
                    .addOnSuccessListener {
                        Log.d(TAG, "Successfully joined meeting: $meetingId")

                        // Create or update chat document
                        val chatRef = firestoreDb.collection("chats").document(meetingId)
                        chatRef.get().addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists()) {
                                // Chat document already exists, add the user to participants if not already present
                                val participants = documentSnapshot.get("participants") as? List<String> ?: emptyList()
                                if (!participants.contains(userUID)) {
                                    chatRef.update("participants", FieldValue.arrayUnion(userUID))
                                        .addOnSuccessListener {
                                            Log.d(TAG, "Successfully added user to chat: $meetingId")
                                            // Launch ChatActivity here
                                            val intent = Intent(holder.itemView.context, ChatActivity::class.java)
                                            intent.putExtra("meetingId", meetingId)
                                            val tutorUid = intent.getStringExtra("TUTOR_UID")
                                            holder.itemView.context.startActivity(intent)
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e(TAG, "Error adding user to chat", e)
                                        }
                                } else {
                                    // Launch ChatActivity here
                                    val intent = Intent(holder.itemView.context, ChatActivity::class.java)
                                    intent.putExtra("meetingId", meetingId)
                                    holder.itemView.context.startActivity(intent)
                                }
                            } else {
                                // Chat document doesn't exist, create a new one
                                val chatData = hashMapOf(
                                    "meetingID" to meetingId,
                                    "participants" to listOf(userUID, meeting.tutorId) // Add tutor and student
                                )
                                chatRef.set(chatData)
                                    .addOnSuccessListener {
                                        Log.d(TAG, "Successfully created chat: $meetingId")
                                        // Launch ChatActivity here
                                        val intent = Intent(holder.itemView.context, ChatActivity::class.java)
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
                        Log.e(TAG, "Error joining meeting", e)
                    }
            }
        }
    }

    override fun getItemCount(): Int = meetings.size
}