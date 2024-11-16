package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mobappprototype.R
import com.example.mobappprototype.databinding.ActivityQuestionsBinding
import com.example.mobappprototype.databinding.ActivityResultBinding
import com.example.mobappprototype.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private const val TAG = "ResultActivity"
class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestoreDb = FirebaseFirestore.getInstance()

        val totalQuestions = intent.getIntExtra(Constants.TOTAL_QUESTIONS, 0)
        val score = intent.getIntExtra(Constants.SCORE, 0)

        binding.tvScoreResult.text = "$score/$totalQuestions"

        binding.ibtnHomeFResult.setOnClickListener {
            checkUserRole()
        }
    }

    private fun checkUserRole() {
        val user = auth.currentUser
        if (user != null) {
            val userUid = user.uid
            val usersRef = firestoreDb.collection("users").document(userUid)

            usersRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val role = document.getString("role")
                    when (role) {
                        "Student" -> {
                            goStudentMainActivity()
                        }

                        "Tutor" -> {
                            goTutorMainActivity()
                        }
                    }
                }
            }
        }
    }
    private fun goStudentMainActivity() {
        Log.i(TAG, "goStudentMainActivity")
        val intent = Intent(this, StudentMainActivity::class.java)
        binding.layoutMainActivity.visibility = View.GONE
        binding.loadingLayout.visibility = View.VISIBLE
        startActivity(intent)
        finish()
    }

    private fun goTutorMainActivity() {
        Log.i(TAG, "goTutorMainActivity")
        val intent = Intent(this, TutorMainActivity::class.java)
        binding.layoutMainActivity.visibility = View.GONE
        binding.loadingLayout.visibility = View.VISIBLE
        startActivity(intent)
        finish()
    }
}