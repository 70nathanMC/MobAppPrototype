package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobappprototype.Adapter.SubjectChipAdapter
import com.example.mobappprototype.Adapter.TutorListAdapter
import com.example.mobappprototype.databinding.ActivityTutorListBinding
import com.example.mobappprototype.model.TutorListData
import com.google.firebase.firestore.FirebaseFirestore

private const val TAG = "TutorListActivity"

class TutorListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTutorListBinding
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var tutorListAdapter: TutorListAdapter
    private val tutorList = mutableListOf<TutorListData>()
    private lateinit var subjectChipAdapter: SubjectChipAdapter
    private val subjectList = mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestoreDb = FirebaseFirestore.getInstance()
        subjectChipAdapter = SubjectChipAdapter(subjectList)
        binding.rvSubjectChips.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvSubjectChips.adapter = subjectChipAdapter
        fetchSubjects()

        // Initialize RecyclerView
        tutorListAdapter = TutorListAdapter(tutorList)
        binding.rvTutorList.layoutManager = LinearLayoutManager(this) // Set the layout manager
        binding.rvTutorList.adapter = tutorListAdapter // Assuming you're replacing the ListView with RecyclerView

        val searchQuery = intent.getStringExtra("QUERY_TEXT") ?: ""

        if (searchQuery.isBlank()) {
            fetchAllTutors()
        } else {
            searchTutors(searchQuery)
        }

        binding.ibtnHomeFTutorList.setOnClickListener {
            Intent(this, StudentMainActivity::class.java).also {
                startActivity(it)
            }

        }


        binding.ivStudentProfile.setOnClickListener {
            Intent(this, StudentMainProfileActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    private fun fetchAllTutors() {
        tutorList.clear()

        firestoreDb.collection("users")
            .whereEqualTo("role", "Tutor")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val fullName = document.getString("fullName") ?: ""
                    val profilePicUrl = document.getString("profilePic") ?: ""
                    val program = document.getString("program") ?: ""
                    val rating = document.getDouble("rating") ?: 0.0 // Assuming you have a rating field
                    val tutor = TutorListData(profilePicUrl, fullName, program, rating.toFloat(), document.id) // Include document.id here
                    tutorList.add(tutor)
                }

                tutorListAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting tutors: ", exception)
                Toast.makeText(this, "Error fetching tutors", Toast.LENGTH_SHORT).show()
            }
    }
    private fun searchTutors(query: String) {
        tutorList.clear() // Clear previous results

        firestoreDb.collection("users")
            .whereEqualTo("role", "Tutor") // Filter for tutors only
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val fullName = document.getString("fullName") ?: ""
                    val firstName = document.getString("firstName") ?: ""
                    val lastName = document.getString("lastName") ?: ""
                    val rating = document.getDouble("rating") ?: 0.0
                    val profilePicUrl = document.getString("profilePic") ?: ""
                    val program = document.getString("program") ?: ""

                    // Check if the first name or last name starts with the query
                    if (firstName.lowercase().startsWith(query.lowercase()) ||
                        lastName.lowercase().startsWith(query.lowercase())) {
                        val tutor = TutorListData(profilePicUrl, fullName, program, rating.toFloat(), document.id)
                        tutorList.add(tutor)
                    }
                }
                tutorListAdapter.notifyDataSetChanged()

            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting tutors: ", exception)
                Toast.makeText(this, "Error fetching tutors", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchSubjects() {
        subjectList.clear()
        firestoreDb.collection("subjects")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val subjectName = document.getString("subjectName") ?: ""
                    subjectList.add(subjectName)
                }
                subjectChipAdapter.notifyDataSetChanged()
                binding.rvSubjectChips.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
                    override fun onChildViewAttachedToWindow(view: View) {
                        view.setOnClickListener()
                        {
                            val position = binding.rvSubjectChips.getChildAdapterPosition(view)
                            if (position != RecyclerView.NO_POSITION) {
                                fetchTutorsBySubject(subjectList[position])
                            }
                        }
                    }
                    override fun onChildViewDetachedFromWindow(view: View) {}
                })
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting subjects: ", exception)
                Toast.makeText(this, "Error fetching subjects", Toast.LENGTH_SHORT).show()
            }
    }

    fun fetchTutorsBySubject(subjectName: String) {
        tutorList.clear()
        firestoreDb.collection("subjects").whereEqualTo("subjectName", subjectName)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Handle case where no subject is found
                    Toast.makeText(this, "No subject found with the name $subjectName", Toast.LENGTH_SHORT).show()
                    tutorListAdapter.notifyDataSetChanged()
                    return@addOnSuccessListener
                }
                val subjectDocumentId = documents.documents[0].id // Get the document ID
                val relatedTutors = documents.documents[0].get("relatedTutors") as? List<String> ?: emptyList()
                Log.d(TAG, "Related tutors for $subjectName: $relatedTutors") // Add this line to log the relatedTutors list
                if (relatedTutors.isEmpty()) {
                    // Handle case where no tutors are found for the subject
                    Toast.makeText(this, "No tutors found for $subjectName", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Subject document ID: $subjectDocumentId")
                    tutorListAdapter.notifyDataSetChanged()
                    return@addOnSuccessListener
                } else {
                    Log.d(TAG, "Subject document ID: $subjectDocumentId")
                    Log.d(TAG, "Related tutors for $subjectName: $relatedTutors")
                    val userDocuments = mutableListOf<TutorListData>()
                    for (tutorUid in relatedTutors) {
                        firestoreDb.collection("users").document(tutorUid)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val fullName = document.getString("fullName") ?: ""
                                    val profilePicUrl = document.getString("profilePic") ?: ""
                                    val program = document.getString("program") ?: ""
                                    val rating = document.getDouble("rating") ?: 0.0
                                    val tutor = TutorListData(profilePicUrl, fullName, program, rating.toFloat(), document.id)
                                    userDocuments.add(tutor)
                                }
                                if (userDocuments.size == relatedTutors.size) {
                                    tutorList.addAll(userDocuments)
                                    tutorListAdapter.notifyDataSetChanged()
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.w(TAG, "Error getting tutor details: ", exception)
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting subjects: ", exception)
                Toast.makeText(this, "Error fetching subjects", Toast.LENGTH_SHORT).show()
            }
    }

}