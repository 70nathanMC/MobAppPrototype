package com.example.mobappprototype.ui

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.TouchDelegate
import android.view.View
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

        binding.layoutMainActivity.visibility = View.GONE
        binding.loadingLayout.visibility = View.VISIBLE

        firestoreDb = FirebaseFirestore.getInstance()

        val subjectName = intent.getStringExtra("SUBJECT_NAME") ?: ""
        val quizLogo = intent.getIntExtra("QUIZ_LOGO", 0)

        binding.tvSubjectName.text = subjectName
        binding.sivQuizLogo.setImageResource(quizLogo)

        fetchQuizDescription(subjectName)

        binding.linearLayout6.post {
            val rect = Rect()
            binding.btnHome.getHitRect(rect)
            rect.inset(-50, -50) // Expand the touch area by 50 pixels on each side
            binding.linearLayout6.touchDelegate = TouchDelegate(rect, binding.btnHome)
        }

        binding.btnTakeQuiz.setOnClickListener {
            val intent = Intent(this, QuizActivity::class.java)
            binding.layoutMainActivity.visibility = View.GONE
            binding.loadingLayout.visibility = View.VISIBLE
            intent.putExtra("SUBJECT_NAME", subjectName)
            startActivity(intent)
        }

        binding.btnHome.setOnClickListener{
            Intent(this, StudentMainActivity::class.java).also {
                binding.layoutMainActivity.visibility = View.GONE
                binding.loadingLayout.visibility = View.VISIBLE
                startActivity(it)
                finish()
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
                    binding.loadingLayout.visibility = View.GONE
                    binding.layoutMainActivity.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }
                val quizDesc = documents.documents[0].getString("quizDesc") ?: ""
                binding.tvSubjectDesc.text = quizDesc
                binding.loadingLayout.visibility = View.GONE
                binding.layoutMainActivity.visibility = View.VISIBLE
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching quiz description", exception)
                binding.loadingLayout.visibility = View.GONE
                binding.layoutMainActivity.visibility = View.VISIBLE
            }
    }

    override fun onResume() {
        super.onResume()
        binding.loadingLayout.visibility = View.GONE
        binding.layoutMainActivity.visibility = View.VISIBLE
    }
}