package com.example.mobappprototype.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobappprototype.R
import com.example.mobappprototype.databinding.ActivityAddSubjectBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class AddSubjectActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddSubjectBinding
    private lateinit var firestoreDb: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSubjectBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firestoreDb = FirebaseFirestore.getInstance()

        // Fetch subjects from Firestore
        fetchSubjectsFromFirestore()

        // Handle the confirm button click
        binding.btnConfirmAdd.setOnClickListener {
            val newButtonLabel = binding.etButtonLabel.text.toString()
            val resultIntent = Intent().apply {
                putExtra("NEW_BUTTON_LABEL", newButtonLabel)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun fetchSubjectsFromFirestore() {
        val subjectsRef = firestoreDb.collection("subjects")

        subjectsRef.get().addOnSuccessListener { querySnapshot: QuerySnapshot ->
            val subjectsList = mutableListOf<String>()

            for (document in querySnapshot) {
                val subjectName = document.getString("subjectName")
                subjectName?.let {
                    subjectsList.add(it)
                }
            }

            if (subjectsList.isNotEmpty()) {
                // Create an ArrayAdapter with the fetched subjects list
                val arrayAdapter = ArrayAdapter(this, R.layout.dropdown_item_normal, subjectsList)

                // Set the ArrayAdapter to the AutoCompleteTextView
                binding.etButtonLabel.setAdapter(arrayAdapter)
            } else {
                Toast.makeText(this, "No subjects found", Toast.LENGTH_SHORT).show()
            }

        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error fetching subjects: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
