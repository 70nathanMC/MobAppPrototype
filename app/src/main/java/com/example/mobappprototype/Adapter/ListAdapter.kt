package com.example.mobappprototype.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.example.mobappprototype.R
import com.example.mobappprototype.model.TutorData

class ListAdapter (context: Context, dataArrayList: ArrayList<TutorData?>?) :
    ArrayAdapter<TutorData?>(context, R.layout.list_item, dataArrayList!!) {
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var view = view
        val listData = getItem(position)
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        }
        val listTutorImage = view!!.findViewById<ImageView>(R.id.listTutorImage)
        val listTutorName = view.findViewById<TextView>(R.id.listTutorName)
        val tutorDegree = view.findViewById<TextView>(R.id.tutorDegree)
        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
        val ratingTxt = view.findViewById<TextView>(R.id.ratingtxt)
        listTutorImage.setImageResource(listData!!.tutorImage)
        listTutorName.text = listData.tutorName
        tutorDegree.text = listData.tutorDegree
        ratingBar.rating = listData.rating
        ratingTxt.text = listData.ratingDesc
        return view
    }
}