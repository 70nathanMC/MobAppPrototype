package com.example.mobappprototype.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mobappprototype.R
import com.google.android.material.textfield.TextInputEditText

class TutorSearchActivity : AppCompatActivity() {
    private lateinit var tieTutorSearch: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tutor_search)

        // Set up window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find the search input field
        tieTutorSearch = findViewById<TextInputEditText>(R.id.tieTutorSearch)

        // Set listener for "Done" or "Enter" action on the keyboard
        tieTutorSearch.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Close the keyboard
                closeKeyboard()

                // Navigate to the TutorListActivity
                Intent(this, TutorListActivity::class.java).also{
                    startActivity(it)
                }

                true  // Return true to indicate that we handled the event
            } else {
                false  // Let the system handle other actions
            }
        }
    }

    // Function to close the keyboard
    private fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
