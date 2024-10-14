package com.example.mobappprototype.Adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mobappprototype.databinding.ListItemInboxBinding
import com.example.mobappprototype.model.ChatRoom
import com.example.mobappprototype.model.MeetingData
import com.example.mobappprototype.ui.ChatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private const val TAG = "InboxAdapter"

class InboxAdapter(private val chatRooms: List<ChatRoom>) :
    RecyclerView.Adapter<InboxAdapter.InboxViewHolder>() {

    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    inner class InboxViewHolder(val binding: ListItemInboxBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InboxViewHolder {
        val binding = ListItemInboxBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        firestoreDb = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        return InboxViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InboxViewHolder, position: Int) {
        val chatRoom = chatRooms[position]
        val meetingID = chatRoom.meetingID

        firestoreDb.collection("meetings").document(meetingID)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val meetingData = document.toObject(MeetingData::class.java)
                    if (meetingData != null) {
                        val subject = meetingData.subject
                        val branch = meetingData.branch
                        holder.binding.tvTutorNameAndSubject.text = "$subject - $branch"
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching meeting details", e)
            }

        // Display last message if available
        if (chatRoom.lastMessage != null) {
            holder.binding.tvLastMessage.text = chatRoom.lastMessage!!.content
        } else {
            holder.binding.tvLastMessage.text = "No messages yet"
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ChatActivity::class.java)
            intent.putExtra("meetingId", chatRoom.meetingID)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = chatRooms.size
}