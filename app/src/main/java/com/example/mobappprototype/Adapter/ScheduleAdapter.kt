package com.example.mobappprototype.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mobappprototype.databinding.ListScheduleItemBinding
import com.example.mobappprototype.model.MeetingData

class ScheduleAdapter(private var meetings: List<MeetingData>) :
    RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    class ScheduleViewHolder(val binding: ListScheduleItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = ListScheduleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val meeting = meetings[position] // This is now MeetingData
        if (position > 0 && meeting.day == meetings[position - 1].day) {
            holder.binding.tvMeetingDay.visibility = View.GONE
        } else {
            holder.binding.tvMeetingDay.visibility = View.VISIBLE
            holder.binding.tvMeetingDay.text = meeting.day
        }

        val timeRange = "${meeting.startTime} - ${meeting.endTime}"
        holder.binding.tvMeetingTime.text = timeRange
    }

    fun updateMeetings(newMeetings: List<MeetingData>) {
        meetings = newMeetings
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = meetings.size
}