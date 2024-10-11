package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.mobappprototype.Adapter.ListAdapter
import com.example.mobappprototype.R
import com.example.mobappprototype.databinding.ActivityTutorListBinding
import com.example.mobappprototype.model.TutorData

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

        val imageTutorList = intArrayOf(
            R.drawable.james,
            R.drawable.emma,
            R.drawable.isabella,
            R.drawable.noah,
        )
        val tutorBioList = arrayOf(
            "", "", "", "",
        )
        val tutorStrengthsList = arrayOf(
            "", "", "", "",
        )
        val tutorScheduleList = arrayListOf(
            "", "", "", "",
        )
        val tutorRating = floatArrayOf(4.5F, 4.7F, 4.8F, 4.2F)
        val nameTutorList = arrayOf("James Bautista", "Emma Davis", "Isabella Thomas", "Noah Anderson")
        val degreeList = arrayOf("Computer Science", "Computer Engineering", "Civil Engineering", "Data Science")
        for (i in imageTutorList.indices) {
            listTutorData = TutorData(
                nameTutorList[i],
                degreeList[i], tutorBioList[i], tutorRating[i], "${tutorRating[i]} Rating", imageTutorList[i], tutorStrengthsList[i], tutorScheduleList[i]
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
                intent.putExtra("tutorDesc", tutorBioList[i])
                intent.putExtra("rating", tutorRating[i])
                intent.putExtra("image", imageTutorList[i])
                intent.putExtra("tutorStrengths", tutorStrengthsList[i])
                intent.putExtra("tutorSchedule", tutorScheduleList[i])
                startActivity(intent)
            }


        ibtnHomeFTutorList = findViewById(R.id.ibtnHomeFTutorList)
        ibtnHomeFTutorList.setOnClickListener {
            Intent(this, StudentMainActivity::class.java).also {
                startActivity(it)
            }
        }
    }
}