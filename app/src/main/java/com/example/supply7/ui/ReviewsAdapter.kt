package com.example.supply7.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.supply7.R
import com.example.supply7.data.Review

class ReviewsAdapter(
    private var reviews: List<Review> = emptyList()
) : RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.textReviewerName)
        val comment: TextView = view.findViewById(R.id.textComment)
        val rating: RatingBar = view.findViewById(R.id.ratingBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.name.text = review.reviewerName.ifBlank { "Anonymous" }
        holder.comment.text = review.comment
        holder.rating.rating = review.rating
    }

    override fun getItemCount() = reviews.size

    fun updateData(newReviews: List<Review>) {
        reviews = newReviews
        notifyDataSetChanged()
    }
}
