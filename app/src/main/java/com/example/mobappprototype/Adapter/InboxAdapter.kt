package com.example.mobappprototype.Adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mobappprototype.databinding.ListItemInboxBinding
import com.example.mobappprototype.model.ChatRoom
import com.example.mobappprototype.ui.ChatActivity

private const val TAG = "InboxAdapter"

class InboxAdapter(private val chatRooms: List<ChatRoom>) :
    RecyclerView.Adapter<InboxAdapter.InboxViewHolder>() {

    private val meetingDetailsMap = mutableMapOf<String, Pair<String, String>>()

    fun updateMeetingDetails(meetingDetails: Map<String, Pair<String, String>>) {
        meetingDetailsMap.clear()
        meetingDetailsMap.putAll(meetingDetails)
    }

    inner class InboxViewHolder(val binding: ListItemInboxBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InboxViewHolder {
        val binding = ListItemInboxBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InboxViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InboxViewHolder, position: Int) {
        val chatRoom = chatRooms[position]

        // Access meeting details from the map
        val meetingDetails = meetingDetailsMap[chatRoom.meetingID]
        if (meetingDetails != null) {
            val (subject, branch) = meetingDetails
            holder.binding.tvTutorNameAndSubject.text = "$subject - $branch"
        } else {
            holder.binding.tvTutorNameAndSubject.text = "Error loading meeting details"
        }

        // Display last message if available
        if (chatRoom.lastMessage != null) {
            holder.binding.tvLastMessage.text = chatRoom.lastMessage!!.content
        } else {
            holder.binding.tvLastMessage.text = "No messages yet"
        }

        val unreadCount = chatRoom.unreadCount
        holder.binding.tvUnreadCount.text = unreadCount.toString()

        if (unreadCount > 0) {
            holder.binding.tvUnreadCount.visibility = View.VISIBLE
        } else {
            holder.binding.tvUnreadCount.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ChatActivity::class.java)
            intent.putExtra("meetingId", chatRoom.meetingID)

            // Reset unread count for this chat room in your data
            chatRoom.unreadCount = 0
            notifyItemChanged(position)

            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = chatRooms.size
}