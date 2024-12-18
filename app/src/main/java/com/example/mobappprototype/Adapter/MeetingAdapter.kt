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
import java.util.Calendar
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

        // Format date and time using meeting.upcomingDate
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEE MMM d", Locale.getDefault())
        val formattedStartTime = timeFormat.format(meeting.startTimeTimestamp.toDate())
        val formattedEndTime = timeFormat.format(meeting.endTimeTimestamp.toDate())
        val formattedMeetingDate = dateFormat.format(meeting.upcomingDate) // Use upcomingDate

        holder.binding.tvMeetingTime.text = "$formattedStartTime - $formattedEndTime"

        // Split the formatted date string by spaces
        val dateParts = formattedMeetingDate.split(" ")

        // Update UI elements using the split parts
        if (dateParts.size == 3) {
            holder.binding.tvMeetingDay.text = dateParts[0].trim()
            holder.binding.tvMeetingMonth.text = dateParts[1].trim()
            holder.binding.tvMeetingDate.text = dateParts[2].trim()
        } else {
            // Handle the error (e.g., log the error or set default values)
            Log.e(TAG, "Error splitting date string: $formattedMeetingDate")
        }

        holder.binding.tvMeetingSite.text = meeting.meetingSite

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