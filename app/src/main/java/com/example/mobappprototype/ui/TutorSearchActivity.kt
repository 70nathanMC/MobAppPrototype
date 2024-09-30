package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobappprototype.R
import com.example.mobappprototype.utils.DummyData
import com.google.android.material.button.MaterialButton
import java.util.*
import kotlin.collections.ArrayList


class TutorSearchActivity : AppCompatActivity() {
    private lateinit var svSearchTutor: SearchView
    private lateinit var lvSearchTutor: ListView
    private lateinit var tvWhatYouHaveSearchedFor: TextView
    lateinit var listAdapter: ArrayAdapter<String>
    lateinit var tutorNamesList: ArrayList<String>
    private lateinit var btnSearch: MaterialButton
    private lateinit var ibtnHomeFFindTutorSearch: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutor_search)

        ibtnHomeFFindTutorSearch = findViewById(R.id.ibtnHomeFFindTutorSearch)
        btnSearch = findViewById(R.id.btnSearch)

        svSearchTutor = findViewById(R.id.svSearchTutor)
        lvSearchTutor = findViewById(R.id.lvSearchTutor)
        tvWhatYouHaveSearchedFor = findViewById(R.id.tvWhatYouHaveSearchedFor)
        tutorNamesList = ArrayList()
        tutorNamesList.addAll(DummyData.tutorList.map {it.name})

        listAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, tutorNamesList)

        lvSearchTutor.adapter = listAdapter

        lvSearchTutor.visibility = View.GONE

        svSearchTutor.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
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
                    lvSearchTutor.visibility = View.VISIBLE  // Show ListView on input
                } else {
                    lvSearchTutor.visibility = View.GONE  // Hide ListView on empty input
                    tvWhatYouHaveSearchedFor.visibility = View.VISIBLE
                }
                listAdapter.filter.filter(newText)
                return true
            }
        })

        btnSearch.setOnClickListener {
            Intent(this@TutorSearchActivity, TutorListActivity::class.java).also {
                startActivity(it)
            }
        }

        ibtnHomeFFindTutorSearch.setOnClickListener{
            Intent(this, MainActivity::class.java).also {
                startActivity(it)
            }
        }
    }
}
