package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mobappprototype.R
import com.example.mobappprototype.databinding.ActivityQuizQuestBinding
import com.google.firebase.firestore.FirebaseFirestore

private const val TAG = "QuizQuestActivity"
class QuizQuestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuizQuestBinding
    private lateinit var firestoreDb: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityQuizQuestBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firestoreDb = FirebaseFirestore.getInstance()

        val subjectName = intent.getStringExtra("SUBJECT_NAME") ?: ""
        val quizLogo = intent.getIntExtra("QUIZ_LOGO", 0)

        binding.tvSubjectName.text = subjectName
        binding.sivQuizLogo.setImageResource(quizLogo)

        fetchQuizDescription(subjectName)

        binding.btnTakeQuiz.setOnClickListener {
            val intent = Intent(this, QuizActivity::class.java)
            intent.putExtra("SUBJECT_NAME", subjectName)
            startActivity(intent)
        }

        binding.btnHome.setOnClickListener{
            Intent(this, StudentMainActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    private fun fetchQuizDescription(subjectName: String) {
        firestoreDb.collection("quizzes")
            .whereEqualTo("subjectName", subjectName)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.e(TAG, "No quiz found for subject: $subjectName")
                    return@addOnSuccessListener
                }
                val quizDesc = documents.documents[0].getString("quizDesc") ?: ""
                binding.tvSubjectDesc.text = quizDesc
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching quiz description", exception)
            }
    }
}