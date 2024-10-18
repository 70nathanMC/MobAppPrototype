package com.example.mobappprototype.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import com.example.mobappprototype.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val TAG = "RatingBottomSheetFragment"

class RatingBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var ratingBar: RatingBar
    private lateinit var etComment: EditText
    private lateinit var btnSubmitRating: Button
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var existingRating: Float? = null
    private var existingComment: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            existingRating = it.getFloat("EXISTING_RATING", 0f)
            existingComment = it.getString("EXISTING_COMMENT")

            // Reset existingRating and existingComment if they are not being passed in the arguments
            if (!it.containsKey("EXISTING_RATING")) {
                existingRating = null
                existingComment = null
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,

        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_rating_bottom_sheet,
            container, false)
        ratingBar = view.findViewById(R.id.ratingBar)
        etComment = view.findViewById(R.id.etComment)
        btnSubmitRating = view.findViewById(R.id.btnSubmitRating)
        firestoreDb = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        existingRating?.let { ratingBar.rating = it }
        existingComment?.let { etComment.setText(it) }

        btnSubmitRating.setOnClickListener {
            val rating = ratingBar.rating
            val comment = etComment.text.toString()
            val tutorUid = arguments?.getString("TUTOR_UID")

            if (tutorUid != null) {
                submitRating(tutorUid, rating, comment)
            } else {
                Log.e(TAG, "Tutor UID not found")
                Toast.makeText(requireContext(), "Error submitting rating", Toast.LENGTH_SHORT).show()
            }

        }
        return view
    }

    private fun submitRating(tutorUid: String, rating: Float, comment: String) {
        val reviewerUid = auth.currentUser?.uid ?: return

        firestoreDb.collection("users").document(reviewerUid)
            .get()
            .addOnSuccessListener { reviewerDocument ->
                val firstName = reviewerDocument.getString("firstName") ?: ""
                val lastName = reviewerDocument.getString("lastName") ?: ""
                val lastNameInitial = if (lastName.isNotEmpty()) lastName[0].toString() + "." else ""
                val reviewerName = "$firstName $lastNameInitial"
                val reviewerProfilePic = reviewerDocument.getString("profilePic") ?: ""

                if (existingRating != null) {
                    // Edit existing review
                    val reviewId = arguments?.getString("REVIEW_ID")
                    if (reviewId != null) {
                        val updates = mapOf(
                            "rating" to rating,
                            "comment" to comment,
                            "reviewerName" to reviewerName,
                            "reviewerProfilePic" to reviewerProfilePic
                        )
                        firestoreDb.collection("reviews").document(reviewId)
                            .update(updates) // Pass the Map to the update function
                            .addOnSuccessListener {
                                updateTutorRating(tutorUid)
                                Log.d(TAG, "Review updated successfully!")
                                Toast.makeText(
                                    requireContext(),
                                    "Review updated!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                dismiss()
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error updating review", e)
                                Toast.makeText(
                                    requireContext(),
                                    "Error updating review",
                                    Toast.LENGTH_SHORT
                                ).show()
                                dismiss()
                            }
                    } else {
                        Log.e(TAG, "Review ID not found for editing")
                        Toast.makeText(
                            requireContext(),
                            "Error updating review",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // Add new review
                    val reviewData = hashMapOf(
                        "reviewerUID" to reviewerUid,
                        "tutorUID" to tutorUid,
                        "rating" to rating,
                        "comment" to comment,
                        "reviewerName" to reviewerName,
                        "reviewerProfilePic" to reviewerProfilePic
                    )

                    firestoreDb.collection("reviews")
                        .add(reviewData)
                        .addOnSuccessListener {
                            updateTutorRating(tutorUid)
                            firestoreDb.collection("reviews")
                                .add(reviewData)
                                .addOnSuccessListener {
                                    updateTutorRating(tutorUid)

                                    // Update feedbackAmount
                                    firestoreDb.collection("users").document(tutorUid)
                                        .update("feedbackAmount", FieldValue.increment(1))
                                        .addOnSuccessListener {
                                            Log.d(TAG, "feedbackAmount incremented successfully!")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w(TAG, "Error incrementing feedbackAmount", e)
                                        }
                                    Log.d(TAG, "Review submitted successfully!")
                                }
                                .addOnFailureListener { e ->
                                    Log.d(TAG, "$e")
                                }
                            Log.d(TAG, "Review submitted successfully!")
                            Toast.makeText(requireContext(), "Rating submitted!", Toast.LENGTH_SHORT).show()
                            dismiss()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error submitting review", e)
                            Toast.makeText(
                                requireContext(),
                                "Error submitting rating",
                                Toast.LENGTH_SHORT
                            ).show()
                            dismiss()
                        }
                }
            }
    }

    private fun updateTutorRating(tutorUid: String) {
        firestoreDb.collection("reviews")
            .whereEqualTo("tutorUID", tutorUid)
            .get()
            .addOnSuccessListener { documents ->
                var totalRating = 0.0
                for (document in documents) {
                    totalRating += document.getDouble("rating") ?: 0.0
                }
                val averageRating = if (documents.size() > 0) totalRating / documents.size() else 0.0

                firestoreDb.collection("users").document(tutorUid)
                    .update("overallRating", averageRating)
                    .addOnSuccessListener {
                        Log.d(TAG, "Tutor rating updated successfully!")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error updating tutor rating", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting reviews", e)
            }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RatingBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}