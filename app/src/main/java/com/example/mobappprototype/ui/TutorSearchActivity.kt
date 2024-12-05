package com.example.mobappprototype.ui


import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.TouchDelegate
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobappprototype.Adapter.SearchHistoryAdapter
import com.example.mobappprototype.Adapter.TutorSearchAdapter
import com.example.mobappprototype.databinding.ActivityTutorSearchBinding
import com.example.mobappprototype.model.TutorSearchData
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Date

private const val TAG = "TutorSearchActivity"
class TutorSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTutorSearchBinding
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var tutorSearchAdapter: TutorSearchAdapter
    private val tutorList = mutableListOf<TutorSearchData>()
    private lateinit var searchHistoryAdapter: SearchHistoryAdapter
    private val searchHistoryList = mutableListOf<String>()
    private lateinit var auth: FirebaseAuth
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestoreDb = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        searchHistoryAdapter = SearchHistoryAdapter(searchHistoryList) { query ->
            binding.svSearchTutor.setQuery(query, true)
        }
        binding.rvSearchHistory.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvSearchHistory.adapter = searchHistoryAdapter

        loadSearchHistory()

        binding.flImageButtonBack.post {
            val rect = Rect()
            binding.ibtnHomeFFindTutorSearch.getHitRect(rect)
            rect.inset(-50, -50) // Expand the touch area by 50 pixels on each side
            binding.flImageButtonBack.touchDelegate = TouchDelegate(rect, binding.ibtnHomeFFindTutorSearch)
        }


        tutorSearchAdapter = TutorSearchAdapter(tutorList)
        binding.rvTutorSearch.layoutManager = LinearLayoutManager(this)
        binding.rvTutorSearch.adapter = tutorSearchAdapter

        binding.rvTutorSearch.visibility = View.GONE

        binding.svSearchTutor.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    runnable?.let { handler.removeCallbacks(it) }

                    runnable = Runnable {
                        Log.d(TAG, "Query: $query")
                        saveSearchQuery(query) // Save the search query
                        val intent = Intent(this@TutorSearchActivity, TutorListActivity::class.java)
                        intent.putExtra("QUERY_TEXT", query)
                        startActivity(intent)
                    }
                    handler.postDelayed(runnable!!, 500) // Delay for 500 milliseconds
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    searchTutors(newText)
                    binding.rvTutorSearch.visibility = View.VISIBLE
                    binding.clSearchHistory.visibility = View.GONE
                } else {
                    binding.rvTutorSearch.visibility = View.GONE
                    binding.clSearchHistory.visibility = View.VISIBLE
                    tutorList.clear()
                }
                return true
            }
        })

        binding.btnSearch.setOnClickListener {
            val query = binding.svSearchTutor.query.toString() // Get the query from SearchView
            saveSearchQuery(query)
            val intent = Intent(this@TutorSearchActivity, TutorListActivity::class.java)
            intent.putExtra("QUERY_TEXT", query)
            startActivity(intent)
        }

        binding.ibtnHomeFFindTutorSearch.setOnClickListener{
            Intent(this, StudentMainActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun searchTutors(query: String) {
        tutorList.clear() // Clear previous results

        firestoreDb.collection("users")
            .whereEqualTo("role", "Tutor") // Filter for tutors only
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val fullName = document.getString("fullName") ?: ""

                    // Split the full name and query into words
                    val nameWords = fullName.lowercase().split(" ")
                    val queryWords = query.lowercase().split(" ")

                    // Check if ANY query word matches the START of ANY name word
                    val isMatch = queryWords.any { queryWord ->
                        nameWords.any { nameWord ->
                            nameWord.startsWith(queryWord)
                        }
                    }

                    if (isMatch) {
                        val profilePicUrl = document.getString("profilePic") ?: ""
                        val program = document.getString("program") ?: ""
                        val tutor = TutorSearchData(profilePicUrl, fullName, program, document.id)
                        tutorList.add(tutor)
                    }
                }
                tutorSearchAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting tutors: ", exception)
                Toast.makeText(this, "Error searching for tutors", Toast.LENGTH_SHORT).show()
            }
    }
    private fun saveSearchQuery(query: String) {
        Log.d(TAG, "Searched for $query")
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val searchHistoryRef = firestoreDb.collection("users")
                .document(currentUser.uid)
                .collection("searchHistory")

            val queryData = hashMapOf(
                "query" to query,
                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )

            searchHistoryRef.add(queryData)
                .addOnSuccessListener {
                    // find and delete older duplicates
                    searchHistoryRef.whereEqualTo("query", query)
                        .get()
                        .addOnSuccessListener { documents ->
                            val latestDocument = documents.maxByOrNull {
                                it.getTimestamp("timestamp") ?: Timestamp(Date(0))
                            }
                            documents.forEach { document ->
                                if (document != latestDocument) {
                                    searchHistoryRef.document(document.id).delete()
                                        .addOnFailureListener { e ->
                                            Log.w(TAG, "Error deleting duplicate query", e)
                                        }
                                }
                            }
                            loadSearchHistory()
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error checking for existing query", e)
                        }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding search query", e)
                }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadSearchHistory() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestoreDb.collection("users")
                .document(currentUser.uid)
                .collection("searchHistory")
                .orderBy("timestamp", Query.Direction.DESCENDING) // Order by timestamp in descending order
                .limit(10)
                .get()
                .addOnSuccessListener { documents ->
                    searchHistoryList.clear()
                    for (document in documents) {
                        val query = document.getString("query")
                        if (query != null) {
                            searchHistoryList.add(query)
                        }
                    }
                    searchHistoryAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting search history: ", exception)
                }
        }
    }
}
