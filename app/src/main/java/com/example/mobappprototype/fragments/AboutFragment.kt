package com.example.mobappprototype.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.mobappprototype.R

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AboutFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_about, container, false)
        val
                bioTextView = view.findViewById<TextView>(R.id.tvBio) // Assuming you have a TextView with this ID
        val bio = arguments?.getString("BIO") ?: ""
        bioTextView.text = bio
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(bio: String) =
            AboutFragment().apply {
                arguments = Bundle().apply {
                    putString("BIO", bio)
                }
            }
    }
}