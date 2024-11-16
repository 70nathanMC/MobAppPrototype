package com.example.mobappprototype.Adapter // Make sure this is in the correct package

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mobappprototype.databinding.ItemCalendarBinding // Import the correct binding
import com.example.mobappprototype.model.MeetingData
import com.example.mobappprototype.ui.ChatActivity
import java.text.SimpleDateFormat
import java.util.Locale

class CalendarMeetingAdapter(initialMeetings: List<MeetingData>) :
    RecyclerView.Adapter<CalendarMeetingAdapter.ViewHolder>() {

    var meetings: MutableList<MeetingData> = initialMeetings.toMutableList()

    class ViewHolder(val binding: ItemCalendarBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCalendarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val meeting = meetings[position]

        holder.binding.tvMeetingSubjectAndBranch.text = "${meeting.subject} - ${meeting.branch}"

        // Format date and time using meeting.upcomingDate
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val formattedStartTime = timeFormat.format(meeting.startTimeTimestamp.toDate())
        val formattedEndTime = timeFormat.format(meeting.endTimeTimestamp.toDate())

        holder.binding.tvMeetingTime.text = "$formattedStartTime - $formattedEndTime"
        holder.binding.tvMeetingSite.text = meeting.meetingSite

        holder.itemView.setOnClickListener {
            val meetingId = meeting.id
            Log.d("CalendarMeetingAdapter", "Meeting ID in adapter: $meetingId")

            val intent = Intent(holder.itemView.context, ChatActivity::class.java)
            intent.putExtra("meetingId", meeting.id)
            Log.d("CalendarMeetingAdapter", "The meeting being passed to ChatActivity is: ${meeting.id}")
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