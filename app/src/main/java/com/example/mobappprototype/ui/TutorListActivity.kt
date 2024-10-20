package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobappprototype.Adapter.SubjectChipAdapter
import com.example.mobappprototype.Adapter.TutorListAdapter
import com.example.mobappprototype.ViewModel.UserViewModel
import com.example.mobappprototype.databinding.ActivityTutorListBinding
import com.example.mobappprototype.model.TutorListData
import com.example.mobappprototype.model.User
import com.google.api.Authentication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private const val TAG = "TutorListActivity"

class TutorListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTutorListBinding
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var tutorListAdapter: TutorListAdapter
    private val tutorList = mutableListOf<TutorListData>()
    private lateinit var subjectChipAdapter: SubjectChipAdapter
    private val subjectList = mutableListOf<String>()
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestoreDb = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        subjectChipAdapter = SubjectChipAdapter(subjectList)
        binding.rvSubjectChips.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvSubjectChips.adapter = subjectChipAdapter
        fetchSubjects()

        // Initialize RecyclerView
        tutorListAdapter = TutorListAdapter(tutorList)
        binding.rvTutorList.layoutManager = LinearLayoutManager(this) // Set the layout manager
        binding.rvTutorList.adapter = tutorListAdapter // Assuming you're replacing the ListView with RecyclerView

        val searchQuery = intent.getStringExtra("QUERY_TEXT") ?: ""

        val userUID = auth.currentUser?.uid
        if (userUID != null) {
            val userRef = firestoreDb.collection("users").document(userUID)
            userRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(User::class.java)
                    if (user != null) {
                        userViewModel.setUser(user)
                    }
                } else {
                    Log.d(TAG, "User document does not exist")
                }
            }
        }

        userViewModel.user.observe(this) { user ->
            if (user != null) {
                updateUIWithUserData(user)
            }
        }

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

    private fun updateUIWithUserData(user: User) {
        Glide.with(this).load(user.profilePic).into(binding.ivStudentProfile)
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
                    val overallRating = document.getDouble("overallRating") ?: 0.0 // Assuming you have a rating field
                    val tutor = TutorListData(profilePicUrl, fullName, program, overallRating.toFloat(), document.id) // Include document.id here
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
            .whereEqualTo("role", "Tutor")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val fullName = document.getString("fullName") ?: ""
                    val firstName = document.getString("firstName") ?: ""
                    val lastName = document.getString("lastName") ?: ""
                    val overallRating = document.getDouble("overallRating") ?: 0.0
                    val profilePicUrl = document.getString("profilePic") ?: ""
                    val program = document.getString("program") ?: ""

                    // Check if the first name or last name starts with the query
                    if (firstName.lowercase().startsWith(query.lowercase()) ||
                        lastName.lowercase().startsWith(query.lowercase())) {
                        val tutor = TutorListData(profilePicUrl, fullName, program, overallRating.toFloat(), document.id)
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
                    Toast.makeText(this, "No subject found with the name $subjectName", Toast.LENGTH_SHORT).show()
                    tutorListAdapter.notifyDataSetChanged()
                    return@addOnSuccessListener
                }
                val subjectDocumentId = documents.documents[0].id
                val relatedTutors = documents.documents[0].get("relatedTutors") as? List<String> ?: emptyList()
                Log.d(TAG, "Related tutors for $subjectName: $relatedTutors")
                if (relatedTutors.isEmpty()) {
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
                                    val overallRating = document.getDouble("overallRating") ?: 0.0
                                    val tutor = TutorListData(profilePicUrl, fullName, program, overallRating.toFloat(), document.id)
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