package com.example.mobappprototype.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobappprototype.databinding.ListItemBinding
import com.example.mobappprototype.model.TutorListData
import com.example.mobappprototype.ui.TutorProfileActivity

class TutorListAdapter(private val tutors: List<TutorListData>) :
    RecyclerView.Adapter<TutorListAdapter.TutorListViewHolder>() {

    inner class TutorListViewHolder(val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            TutorListViewHolder {
        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TutorListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TutorListViewHolder, position:
    Int) {
        val tutor = tutors[position]
        holder.binding.listTutorName.text = tutor.fullName
        holder.binding.tutorDegree.text = tutor.program
        holder.binding.ratingBar.rating = tutor.overallRating
        holder.binding.ratingtxt.text = "${tutor.overallRating} Rating"

        holder.binding.root.setOnClickListener {
            val intent = Intent(holder.itemView.context, TutorProfileActivity::class.java)
            intent.putExtra("TUTOR_UID", tutor.tutorUid)
            holder.itemView.context.startActivity(intent)
        }

        Glide.with(holder.itemView.context)
            .load(tutor.profilePicUrl)
            .into(holder.binding.listTutorImage)
    }

    override fun getItemCount(): Int = tutors.size
}