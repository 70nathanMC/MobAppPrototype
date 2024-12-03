package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobappprototype.Adapter.MeetingsAdapter
import com.example.mobappprototype.R
import com.example.mobappprototype.ViewModel.UserViewModel
import com.example.mobappprototype.databinding.ActivityTutorMainBinding
import com.example.mobappprototype.model.MeetingForTutor
import com.example.mobappprototype.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

private const val TAG = "TutorMainActivity"
class TutorMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTutorMainBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var meetingsAdapter: MeetingsAdapter
    private lateinit var userViewModel: UserViewModel
    private var dotsCount: Int = 0
    private lateinit var dots: Array<ImageView?>

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityTutorMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.layoutMainActivity.visibility = View.GONE
        binding.loadingLayout.visibility = View.VISIBLE

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        val userUID = auth.currentUser?.uid
        if (userUID != null) {
            val userRef = db.collection("users").document(userUID)
            userRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(User::class.java)
                    if (user != null) {
                        userViewModel.setUser(user)
                    }
                } else {
                    Log.d(TAG, "User document does not exist")
                    goCreateProfileActivity()
                }
            }
        }

        userViewModel.user.observe(this) { user ->
            if (user != null) {
                updateUIWithUserData(user)
                fetchMeetings()
                updateFcmToken()
            }
        }

//        setupHorizontalScrollView()
        binding.bottomNavigationBar.selectedItemId = R.id.home

        binding.btnCreateMeeting.setOnClickListener {
            val intent = Intent(this, CreateMeetingActivity::class.java)
            binding.layoutMainActivity.visibility = View.GONE
            binding.loadingLayout.visibility = View.VISIBLE
            startActivity(intent)
        }
        binding.ivUserImageDashboard.setOnClickListener{
            val intent = Intent(this, TutorMainProfileActivity::class.java)
            binding.layoutMainActivity.visibility = View.GONE
            binding.loadingLayout.visibility = View.VISIBLE
            startActivity(intent)
        }

        binding.rvMeetings.layoutManager = LinearLayoutManager(this)
        meetingsAdapter = MeetingsAdapter(emptyList())
        binding.rvMeetings.adapter = meetingsAdapter

//        binding.ivGenMath.setOnClickListener {
//            startQuizQuestActivity("Gen Math", R.drawable.ic_qq_gen_math)
//        }
//        binding.ivPhysics.setOnClickListener{
//            startQuizQuestActivity("Physics", R.drawable.ic_qq_physics)
//        }
//        binding.ivCalculus.setOnClickListener{
//            startQuizQuestActivity("Calculus", R.drawable.ic_qq_calculus)
//        }
//        binding.ivScience.setOnClickListener{
//            startQuizQuestActivity("Science", R.drawable.ic_qq_science)
//        }
//        binding.ivHistory.setOnClickListener{
//            startQuizQuestActivity("History", R.drawable.ic_qq_history)
//        }
//        binding.ivLiterature.setOnClickListener{
//            startQuizQuestActivity("Literature", R.drawable.ic_qq_literature)
//        }
//        binding.ivStatistics.setOnClickListener{
//            startQuizQuestActivity("Statistics", R.drawable.ic_qq_statistics)
//        }
//        binding.ivPhilosophy.setOnClickListener{
//            startQuizQuestActivity("Philosophy", R.drawable.ic_qq_philosophy)
//        }


        binding.bottomNavigationBar.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    true
                }
                R.id.messages -> {
                    val intent = Intent(this, InboxActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.profile -> {
                    val intent = Intent(this, TutorMainProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun startQuizQuestActivity(subjectName: String, quizLogo: Int) {
        Intent(this, QuizQuestActivity::class.java).also {
            binding.layoutMainActivity.visibility = View.GONE
            binding.loadingLayout.visibility = View.VISIBLE
            it.putExtra("SUBJECT_NAME", subjectName)
            it.putExtra("QUIZ_LOGO", quizLogo)
            startActivity(it)
        }
    }

//    private fun setupHorizontalScrollView() {
//        val scrollView = binding.hsvDashboard
//        val linearLayout = binding.layoutDots
//        val subjectsPerPage = 4 // Number of subjects per page
//
//        // Calculate the number of dots dynamically
//        val totalSubjects = (scrollView.getChildAt(0) as LinearLayout).childCount
//        dotsCount = (totalSubjects + subjectsPerPage - 1) / subjectsPerPage // Ceiling division
//
//        dots = arrayOfNulls(dotsCount)
//
//        for (i in 0 until dotsCount) {
//            dots[i] = ImageView(this)
//            dots[i]?.setImageDrawable(resources.getDrawable(R.drawable.ic_non_active_dot))
//
//            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
//            params.setMargins(8, 0, 8, 0)
//            linearLayout.addView(dots[i], params)
//        }
//
//        dots[0]?.setImageDrawable(resources.getDrawable(R.drawable.ic_active_dot))
//
//        scrollView.setOnScrollChangeListener { _, scrollX, _, _, _ ->
//            val viewWidth = scrollView.width
//            val page = (scrollX + (viewWidth / 2)) / viewWidth
//            updateDots(page)
//        }
//    }

    private fun updateDots(currentPage: Int) {
        for (i in 0 until dotsCount) {
            dots[i]?.setImageDrawable(resources.getDrawable(R.drawable.ic_non_active_dot))
        }
        dots[currentPage]?.setImageDrawable(resources.getDrawable(R.drawable.ic_active_dot))
    }

    private fun updateUIWithUserData(user: User) {
        binding.tvUserFirstNameDashboard.text = user.firstName
        Glide.with(this).load(user.profilePic).into(binding.ivUserImageDashboard)
    }

    private fun goCreateProfileActivity() {
        Log.i(TAG, "goCreateProfileActivity")
        val intent = Intent(this, CreateProfileActivity::class.java)
        startActivity(intent)
        finish()
    }
    override fun onResume() {
        super.onResume()
        fetchMeetings()
        binding.bottomNavigationBar.selectedItemId = R.id.home
    }
    private fun fetchMeetings() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .collection("tutorData")
            .document("data")
            .get()
            .addOnSuccessListener { document ->
                val meetingIds = document.get("meetings") as? List<String> ?: emptyList()

                // Initialize RecyclerView and adapter only once
                if (!::meetingsAdapter.isInitialized) { // Check if already initialized
                    binding.rvMeetings.layoutManager = LinearLayoutManager(this)
                    meetingsAdapter = MeetingsAdapter(emptyList())
                    binding.rvMeetings.adapter = meetingsAdapter
                }

                if (meetingIds.isEmpty()) {
                    meetingsAdapter.meetings = emptyList() // Update the adapter with an empty list
                    meetingsAdapter.notifyDataSetChanged() // Notify the adapter of the change
                    return@addOnSuccessListener
                }

                Log.d("TutorMainActivity", "Meeting IDs: $meetingIds")

                val query = db.collection("meetings")
                    .whereIn(FieldPath.documentId(), meetingIds)

                query.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Toast.makeText(this, "Error fetching meetings: ${error.message}", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    val meetings = mutableListOf<MeetingForTutor>()
                    for (meetingDocument in snapshot!!.documents) {
                        val meeting = meetingDocument.toObject(MeetingForTutor::class.java)
                        val meetingWithId = meeting!!.copy(id = meetingDocument.id)
                        meetings.add(meetingWithId)
                    }

                    meetingsAdapter.meetings = meetings
                    meetingsAdapter.notifyDataSetChanged()

                    binding.loadingLayout.visibility = View.GONE
                    binding.layoutMainActivity.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching meetings: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun updateFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            val userId = auth.currentUser?.uid
            if (userId != null && token != null) {
                db.collection("users").document(userId)
                    .update("fcmToken", token)
                    .addOnSuccessListener {
                        Log.d(TAG, "FCM token updated successfully")
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Error updating FCM token", exception)
                    }
            }
        }
    }
}