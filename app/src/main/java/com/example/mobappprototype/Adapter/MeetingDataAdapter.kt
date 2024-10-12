package com.example.mobappprototype.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mobappprototype.databinding.ListItemMeetingsBinding // Make sure this is the correct import for your layout
import com.example.mobappprototype.model.MeetingData

class MeetingDataAdapter(private val meetings: List<MeetingData>) :
    RecyclerView.Adapter<MeetingDataAdapter.MeetingViewHolder>() {

    inner class MeetingViewHolder(val binding: ListItemMeetingsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeetingViewHolder {
        val binding = ListItemMeetingsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MeetingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MeetingViewHolder, position:
    Int) {
        val meeting = meetings[position]
        // Populate the views in list_item_meetings.xml with meeting data
        holder.binding.tvMeetingSubject.text = meeting.subject
        holder.binding.tvMeetingBranch.text = meeting.branch
        holder.binding.tvMeetingDay.text = meeting.day
        holder.binding.tvMeetingScheduleStart.text = meeting.startTime
        holder.binding.tvMeetingScheduleEnd.text = meeting.endTime
        holder.binding.tvMeetingSlots.text = meeting.slotsRemaining.toString()
        // ... (Populate other views as needed) ...
    }

    override fun getItemCount(): Int = meetings.size
}