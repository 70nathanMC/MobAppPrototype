package com.example.mobappprototype.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mobappprototype.databinding.ItemButtonListBinding
import com.example.mobappprototype.ui.TutorListActivity

private const val TAG = "SubjectChipAdapter"
class SubjectChipAdapter(private val subjects: List<String>) :
    RecyclerView.Adapter<SubjectChipAdapter.SubjectChipViewHolder>() {

    inner class SubjectChipViewHolder(val binding: ItemButtonListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectChipViewHolder {
        val binding = ItemButtonListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubjectChipViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubjectChipViewHolder, position: Int) {
        val subject = subjects[position]
        holder.binding.root.text = subject

        holder.binding.root.setOnClickListener {
            // Get a reference to the TutorListActivity
            val activity = holder.itemView.context as? TutorListActivity

            // Call fetchTutorsBySubject on the activity
            activity?.fetchTutorsBySubject(subject)
        }
    }

    override fun getItemCount(): Int = subjects.size
}

