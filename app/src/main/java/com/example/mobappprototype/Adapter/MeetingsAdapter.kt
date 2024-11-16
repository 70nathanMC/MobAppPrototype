package com.example.mobappprototype.Adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobappprototype.ui.EditMeetingActivity
import com.example.mobappprototype.R
import com.example.mobappprototype.model.MeetingForTutor
import com.example.mobappprototype.ui.ChatActivity

private const val TAG = "MeetingsAdapter"
class MeetingsAdapter(
    var meetings: List<MeetingForTutor>) :
    RecyclerView.Adapter<MeetingsAdapter.MeetingViewHolder>() {

    class MeetingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val tvMeetingSubject: TextView = itemView.findViewById(R.id.tvMeetingSubject)
        val tvMeetingBranch: TextView = itemView.findViewById(R.id.tvMeetingBranch)
        val tvMeetingDay: TextView = itemView.findViewById(R.id.tvMeetingDay)
        val tvMeetingScheduleStart: TextView = itemView.findViewById(R.id.tvMeetingScheduleStart)
        val tvMeetingScheduleEnd: TextView = itemView.findViewById(R.id.tvMeetingScheduleEnd)
        val tvMeetingSlots: TextView = itemView.findViewById(R.id.tvMeetingSlots)
        val btnEditMeeting: Button = itemView.findViewById(R.id.btnEditMeeting)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeetingViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.meeting_item, parent, false)
        return MeetingViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MeetingViewHolder, position: Int) {
        val currentMeeting = meetings[position]

        holder.tvMeetingSubject.text = currentMeeting.subject
        holder.tvMeetingBranch.text = currentMeeting.branch
        holder.tvMeetingDay.text = currentMeeting.day
        holder.tvMeetingScheduleStart.text = currentMeeting.startTime
        holder.tvMeetingScheduleEnd.text = currentMeeting.endTime
        currentMeeting.slots.toString().also { holder.tvMeetingSlots.text = it }

        holder.btnEditMeeting.setOnClickListener {
            val intent = Intent(holder.itemView.context, EditMeetingActivity::class.java)
            intent.putExtra("meeting", currentMeeting)
            intent.putExtra("meetingId", meetings[position].id) // Pass the meeting ID
            Log.d("MeetingAdapter", "before starting EditMeetingActivity")
            holder.itemView.context.startActivity(intent)
        }

        holder.itemView.setOnClickListener {
            val meetingId = currentMeeting.id
            Log.d(TAG, "Meeting ID in adapter: $meetingId")

            val intent = Intent(holder.itemView.context, ChatActivity::class.java)
            intent.putExtra("meetingId", currentMeeting.id)
            Log.d(TAG, "The meeting being passed to ChatActivity is: ${currentMeeting.id}")
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return meetings.size
    }
}
