package com.example.mobappprototype.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobappprototype.databinding.ItemSearchLayoutBinding
import com.example.mobappprototype.model.TutorSearchData
import com.example.mobappprototype.ui.TutorProfileActivity

class TutorSearchAdapter(private val tutors: List<TutorSearchData>) :
    RecyclerView.Adapter<TutorSearchAdapter.TutorSearchViewHolder>() {

    inner class TutorSearchViewHolder(val binding: ItemSearchLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutorSearchViewHolder {
        val binding = ItemSearchLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TutorSearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TutorSearchViewHolder, position: Int) {
        val tutor = tutors[position]
        holder.binding.root.setOnClickListener {
            val intent = Intent(holder.itemView.context, TutorProfileActivity::class.java)
            intent.putExtra("TUTOR_UID", tutor.tutorUid) // Pass the tutorUid from the TutorSearchData object
            holder.itemView.context.startActivity(intent)
        }

        holder.binding.tvSearchTutorTitle.text = tutor.fullName
        Glide.with(holder.itemView.context)
            .load(tutor.profilePicUrl)
            .into(holder.binding.ivSearchTutorImage)
    }

    override fun getItemCount(): Int = tutors.size
}
