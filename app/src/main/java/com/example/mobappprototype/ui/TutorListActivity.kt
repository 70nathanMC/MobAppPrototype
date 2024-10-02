package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobappprototype.Adapter.ListAdapter
import com.example.mobappprototype.R
import com.example.mobappprototype.databinding.ActivityTutorListBinding
import com.example.mobappprototype.model.TutorData
import com.example.mobappprototype.model.Tutor
import com.example.mobappprototype.utils.Constants
import com.example.mobappprototype.utils.DummyData

class TutorListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTutorListBinding
    private lateinit var ibtnHomeFTutorList: ImageView
    private lateinit var listAdapter: ListAdapter
    private lateinit var listTutorData: TutorData
    var dataArrayList = ArrayList<TutorData?>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding = ActivityTutorListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val imageTutorList = intArrayOf(
            R.drawable.james,
            R.drawable.emma,
            R.drawable.isabella,
            R.drawable.noah,
        )
        val tutorDescList = intArrayOf(
            R.string.tutor_about1,
            R.string.tutor_about1,
            R.string.tutor_about1,
            R.string.tutor_about1,
        )
        val tutorStrengthsList = intArrayOf(
            R.string.strength1,
            R.string.strength1,
            R.string.strength1,
            R.string.strength1,
        )
        val tutorScheduleList = intArrayOf(
            R.string.schedule1,
            R.string.schedule1,
            R.string.schedule1,
            R.string.schedule1,
        )
        val ratingDescList = arrayOf("4.5 Rating", "4.0 Rating", "4.0 Rating", "3.0 Rating")
//        val ratingList = floatArrayOf(4.5F, 4.0F, 4.0F, 3.0F)
        val nameTutorList = arrayOf("James Bautista", "Emma Davis", "Isabella Thomas", "Noah Anderson")
        val degreeList = arrayOf("Computer Science", "Computer Engineering", "Civil Engineering", "Data Science")
        for (i in imageTutorList.indices) {
            listTutorData = TutorData(
                nameTutorList[i],
                degreeList[i], tutorDescList[i], ratingDescList[i], imageTutorList[i], tutorStrengthsList[i], tutorScheduleList[i]
            )
            dataArrayList.add(listTutorData)
        }
        listAdapter = ListAdapter(this@TutorListActivity, dataArrayList)
        binding.listview.adapter = listAdapter
        binding.listview.isClickable = true
        binding.listview.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->
                val intent = Intent(this@TutorListActivity, TutorProfileActivity::class.java)
                intent.putExtra("tutorName", nameTutorList[i])
                intent.putExtra("degree", degreeList[i])
                intent.putExtra("tutorDesc", tutorDescList[i])
//                intent.putExtra("rating", ratingList[i])
                intent.putExtra("ratingDesc", ratingDescList[i])
                intent.putExtra("image", imageTutorList[i])
                intent.putExtra("tutorStrengths", tutorStrengthsList[i])
                intent.putExtra("tutorSchedule", tutorScheduleList[i])
                startActivity(intent)
            }
//        // Initialize View Binding
//        binding = ActivityTutorListBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // Get the list of tutors (from DummyData for now)
//        val tutorList: List<Tutor> = DummyData.tutorList
//
//        // Initialize RecyclerView
//        binding.recyclerViewTutors.layoutManager = LinearLayoutManager(this)
//        binding.recyclerViewTutors.adapter = TutorAdapter(tutorList) { tutor ->
//            // Handle tutor item click, navigate to TutorProfileActivity
//            val intent = Intent(this, TutorProfileActivity::class.java)
//            intent.putExtra("TUTOR_NAME", tutor.name) // Pass data to the profile activity
//            startActivity(intent)
//        }

        ibtnHomeFTutorList = findViewById(R.id.ibtnHomeFTutorList)
        ibtnHomeFTutorList.setOnClickListener {
            Intent(this, MainActivity::class.java).also {
                startActivity(it)
            }
        }
    }
}