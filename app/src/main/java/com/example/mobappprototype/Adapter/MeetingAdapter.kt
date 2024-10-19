package com.example.mobappprototype.Adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobappprototype.databinding.ItemMeetingDashboardBinding
import com.example.mobappprototype.model.MeetingData
import com.example.mobappprototype.ui.ChatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

private const val TAG = "MeetingAdapter"

class MeetingAdapter(initialMeetings: List<MeetingData>) :
    RecyclerView.Adapter<MeetingAdapter.ViewHolder>() {

    var meetings: MutableList<MeetingData> = initialMeetings.toMutableList()
    private lateinit var firestoreDb: FirebaseFirestore

    class ViewHolder(val binding: ItemMeetingDashboardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMeetingDashboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        firestoreDb = FirebaseFirestore.getInstance()
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val meeting = meetings[position]

        holder.binding.tvMeetingSubjectAndBranch.text = "${meeting.subject} - ${meeting.branch}"
        holder.binding.tvTutorFullName.text = meeting.tutorFullName
        Glide.with(holder.itemView.context).load(meeting.tutorProfilePic).into(holder.binding.listTutorImage)

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val formattedTime = timeFormat.format(meeting.startTimeTimestamp.toDate())
        holder.binding.tvMeetingDay.text = formattedTime

        holder.itemView.setOnClickListener {
            val meetingId = meeting.id
            Log.d(TAG, "Meeting ID in adapter: $meetingId")

            val intent = Intent(holder.itemView.context, ChatActivity::class.java)
            intent.putExtra("meetingId", meeting.id)
            Log.d(TAG, "The meeting being passed to ChatActivity is: ${meeting.id}")
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = meetings.size

    fun updateMeetings(newMeetings: List<MeetingData>) {
        this.meetings.clear()
        this.meetings.addAll(newMeetings)
        notifyDataSetChanged()
    }
}