package com.example.mobappprototype.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mobappprototype.R
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobappprototype.Adapter.ReviewAdapter
import com.example.mobappprototype.model.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val TAG = "ReviewFragment"

class ReviewFragment : Fragment() {
    private lateinit var rvReviews: RecyclerView
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var auth: FirebaseAuth
    private val reviews = mutableListOf<Review>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_review, container, false)
        rvReviews = view.findViewById(R.id.rvReviews)
        firestoreDb = FirebaseFirestore.getInstance()
        reviewAdapter = ReviewAdapter(reviews)
        auth = FirebaseAuth.getInstance()
        rvReviews.layoutManager = LinearLayoutManager(context)
        rvReviews.adapter = reviewAdapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tutorUid = activity?.intent?.getStringExtra("TUTOR_UID")
        val userUID = auth.currentUser?.uid
        if (tutorUid != null) {
            fetchReviews(tutorUid)
        } else {
            if (userUID != null) {
                firestoreDb.collection("users").document(userUID)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists() && document.getString("role") == "Tutor") {
                            fetchReviews(userUID) // Fetch schedule using the user's own UID
                        } else {
                            Log.e("SchedFragment", "User is not a Tutor")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("SchedFragment", "Error checking user role", exception)
                    }
            }
        }
    }

    private fun fetchReviews(tutorUid: String) {
        firestoreDb.collection("reviews")
            .whereEqualTo("tutorUID", tutorUid)
            .get()
            .addOnSuccessListener { documents ->
                reviews.clear()
                for (document in documents) {
                    val review = document.toObject(Review::class.java)
                    reviews.add(review)
                }
                reviewAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching reviews", e)
            }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ReviewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}