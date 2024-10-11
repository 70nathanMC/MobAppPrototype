package com.example.mobappprototype.utils

import android.util.Log
import com.example.mobappprototype.model.Question
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

private const val TAG = "Constants"
object Constants {

    const val TOTAL_QUESTIONS = "total_questions"
    const val SCORE = "correct_answers"

    fun getQuestions(subjectName: String, callback: (MutableList<Question>?) -> Unit) {
        Log.d(TAG, "Starting Firestore query for $subjectName")
        val db = Firebase.firestore
        val quizzesCollection = db.collection("quizzes")

        quizzesCollection.whereEqualTo("subjectName", subjectName).get()
            .addOnSuccessListener { documents ->
                Log.d(TAG, "Firestore query successful, documents: $documents")
                val questionsList = mutableListOf<Question>()
                for (document in documents) {
                    val questionsData = document.get("questions") as? List<Map<String, Any>>
                    questionsData?.forEach { questionData ->
                        val questionText = questionData["questionText"] as? String ?: ""  
                        val choices = questionData["choices"] as? List<String> ?: listOf()
                        val correctAnswer = questionData["correctAnswer"] as? String ?: ""
                        questionsList.add(Question(questionText, choices, correctAnswer))
                    }
                }
                callback(questionsList)
            }
            .addOnFailureListener { e ->
                println("Error getting quizzes: $e")
                callback(null) // Return null in case of an error
            }
    }
}