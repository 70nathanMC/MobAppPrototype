package com.example.mobappprototype.fragments

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobappprototype.Adapter.ButtonAdapter
import com.example.mobappprototype.R
import com.example.mobappprototype.model.ButtonData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class StrengthFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonAdapter: ButtonAdapter

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
        val view = inflater.inflate(R.layout.fragment_strength, container, false)
        firestoreDb = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val userUID = auth.currentUser?.uid
        recyclerView = view.findViewById(R.id.rvSubjectButtons)

        val layoutManager = GridLayoutManager(context, 3) // 3 columns
        recyclerView.layoutManager = layoutManager

        buttonAdapter = ButtonAdapter(mutableListOf(), { /* onAddClicked - Not needed here */ }, { /* onRemoveClicked - Not needed here */ }, requireContext())
        recyclerView.adapter = buttonAdapter

        val tutorUid = activity?.intent?.getStringExtra("TUTOR_UID")
        if (tutorUid != null) {
            fetchStrengths(tutorUid)
        } else if (userUID != null) {
            fetchStrengths(userUID)
        } else {
            Log.e("StrengthFragment", "Tutor UID not found and user not logged in")
        }

        val displayMetrics: DisplayMetrics = resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density

        val columnWidthDp = 140
        val spanCount = (screenWidthDp / columnWidthDp).toInt()

        val layoutManager2 = GridLayoutManager(context, spanCount)
        recyclerView.layoutManager = layoutManager2


        return view
    }
    private fun fetchStrengths(tutorUid: String) {
        firestoreDb.collection("users").document(tutorUid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val strengths = document.get("subjects") as? List<String> ?: emptyList()
                    val buttonDataList = strengths.map { ButtonData(it, false, R.color.appGrayF, R.color.appBlack) }
                    buttonAdapter.updateButtonList(buttonDataList) // Update the adapter
                } else {
                    Log.e("StrengthFragment", "Tutor document not found")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("StrengthFragment", "Error getting tutor document", exception)
            }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            StrengthFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}