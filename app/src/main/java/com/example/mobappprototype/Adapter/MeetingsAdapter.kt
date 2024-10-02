package com.example.mobappprototype.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mobappprototype.databinding.ListItemMeetingsBinding
import com.example.mobappprototype.model.MeetingData

class MeetingsAdapter(private val meetings: List<MeetingData>) :
    RecyclerView.Adapter<MeetingsAdapter.MeetingViewHolder>() {

    class MeetingViewHolder(private val binding: ListItemMeetingsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(meeting: MeetingData) {
            binding.listTutorName.text = meeting.subjectName
            binding.degreeTutor.text = meeting.day
            binding.degreeSchedule.text = meeting.time
            binding.degreeSlots.text = meeting.slots
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeetingViewHolder {
        val binding = ListItemMeetingsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MeetingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MeetingViewHolder, position: Int) {
        holder.bind(meetings[position])
    }

    override fun getItemCount(): Int = meetings.size
}

