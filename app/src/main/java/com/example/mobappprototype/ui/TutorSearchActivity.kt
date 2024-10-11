package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobappprototype.R
import com.example.mobappprototype.databinding.ActivityTutorSearchBinding
import com.example.mobappprototype.utils.DummyData


class TutorSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTutorSearchBinding
    private lateinit var tvWhatYouHaveSearchedFor: TextView
    lateinit var listAdapter: ArrayAdapter<String>
    lateinit var tutorNamesList: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tvWhatYouHaveSearchedFor = findViewById(R.id.tvWhatYouHaveSearchedFor)
        tutorNamesList = ArrayList()
        tutorNamesList.addAll(DummyData.tutorList.map {it.name})

        listAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, tutorNamesList)

        binding.lvSearchTutor.adapter = listAdapter

        binding.lvSearchTutor.visibility = View.GONE

        binding.svSearchTutor.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (tutorNamesList.contains(query)){
                    listAdapter.filter.filter(query)
                }
                else {
                    //If doesn't exist
                    Toast.makeText(this@TutorSearchActivity, "Doesn't Exist", Toast.LENGTH_SHORT).show()
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    tvWhatYouHaveSearchedFor.visibility = View.GONE
                    binding.lvSearchTutor.visibility = View.VISIBLE  // Show ListView on input
                } else {
                    binding.lvSearchTutor.visibility = View.GONE  // Hide ListView on empty input
                    tvWhatYouHaveSearchedFor.visibility = View.VISIBLE
                }
                listAdapter.filter.filter(newText)
                return true
            }
        })

        binding.btnSearch.setOnClickListener {
            Intent(this@TutorSearchActivity, TutorListActivity::class.java).also {
                startActivity(it)
            }
        }

        binding.ibtnHomeFFindTutorSearch.setOnClickListener{
            Intent(this, StudentMainActivity::class.java).also {
                startActivity(it)
            }
        }
    }
}
