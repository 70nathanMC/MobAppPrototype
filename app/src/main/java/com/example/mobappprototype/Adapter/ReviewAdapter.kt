package com.example.mobappprototype.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobappprototype.databinding.ListReviewsBinding
import com.example.mobappprototype.model.Review

class ReviewAdapter(private val reviews: List<Review>) :
    RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

    class ViewHolder(val binding: ListReviewsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListReviewsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val review = reviews[position]
        holder.binding.textView37.text = review.reviewerName
        holder.binding.ratingBar.rating = review.rating
        holder.binding.tvComment.text = review.comment.ifEmpty { "User did not submit a review comment." }
        Glide.with(holder.itemView.context)
            .load(review.reviewerProfilePic)
            .into(holder.binding.ivReviewerProfilePic)
    }

    override fun getItemCount() = reviews.size
}