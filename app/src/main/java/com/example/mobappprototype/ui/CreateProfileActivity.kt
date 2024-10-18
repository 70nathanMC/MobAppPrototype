package com.example.mobappprototype.ui

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mobappprototype.Adapter.ButtonAdapter
import com.example.mobappprototype.R
import com.example.mobappprototype.databinding.ActivityCreateProfileBinding
import com.example.mobappprototype.model.ButtonData
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


private const val TAG = "CreateProfileActivity"
class CreateProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateProfileBinding
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var actvRole: AutoCompleteTextView
    private lateinit var actvProgram: AutoCompleteTextView
    private lateinit var adapter: ButtonAdapter
    private lateinit var auth: FirebaseAuth
    private val buttonList = mutableListOf(
        ButtonData("+Add", true, R.color.appGrayButton, R.color.appGray6) // Default "Add" button
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        val rolesList = resources.getStringArray(R.array.roles)
        val arrayAdapter = ArrayAdapter(this, R.layout.dropdown_item, rolesList)
        actvRole = findViewById(R.id.actvRole)
        actvRole.setAdapter(arrayAdapter)
        val programsList = resources.getStringArray(R.array.programs)
        val arrayAdapter2 = ArrayAdapter(this, R.layout.dropdown_item, programsList)
        actvProgram = findViewById(R.id.actvProgram)
        actvProgram.setAdapter(arrayAdapter2)
        actvProgram.dropDownVerticalOffset = actvProgram.height
        firestoreDb = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.layoutWeaknessesOrStrength.visibility = View.GONE

        binding.sivSetProfilePic.setOnClickListener {
            openGallery()
        }

        binding.btnSubmit.setOnClickListener {
            val ref: StorageReference = FirebaseStorage.getInstance().getReference()
                .child("images/${auth.currentUser!!.uid}/userProfile_--photo.jpg")
            ref.getDownloadUrl().addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                Log.d("Image URL:", imageUrl)
                val validation = validateAndSubmitData()
                if (validation){
                    updateUserAndNavigate(imageUrl)
                }
            }
            ref.getDownloadUrl().addOnFailureListener {
                Log.d(TAG, "Download Image failed")
            }
        }

        binding.ivBackFCreateProfile.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }
        binding.etFirstName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.etFirstName.setTextColor(resources.getColor(R.color.appBlack))
                binding.etFirstName.background = ResourcesCompat.getDrawable(resources, R.drawable.default_options_border_bg, null)
            } else {
                binding.etFirstName.setTextColor(resources.getColor(R.color.appGray8))
                binding.etFirstName.background = ResourcesCompat.getDrawable(resources, R.drawable.default_options_border_bg_gray, null)
            }
        }
        binding.etLastName.setOnFocusChangeListener{ _, hasFocus ->
            if (!hasFocus) {
                binding.etLastName.setTextColor(resources.getColor(R.color.appBlack))
                binding.etLastName.background = ResourcesCompat.getDrawable(resources, R.drawable.default_options_border_bg, null)
            } else {
                binding.etLastName.setTextColor(resources.getColor(R.color.appGray8))
                binding.etLastName.background = ResourcesCompat.getDrawable(resources, R.drawable.default_options_border_bg_gray, null)
            }
        }
        binding.etBio.setOnFocusChangeListener{ _, hasFocus ->
            if (!hasFocus) {
                binding.etBio.setTextColor(resources.getColor(R.color.appBlack))
                binding.etBio.background = ResourcesCompat.getDrawable(resources, R.drawable.default_options_border_bg, null)
            } else {
                binding.etBio.setTextColor(resources.getColor(R.color.appGray8))
                binding.etBio.background = ResourcesCompat.getDrawable(resources, R.drawable.default_options_border_bg_gray, null)
            }
        }
        actvRole.setOnItemClickListener{ _, _, _, _ ->
            actvRole.setTextColor(resources.getColor(R.color.appBlack))
            actvRole.background = ResourcesCompat.getDrawable(resources, R.drawable.default_options_border_bg, null)
        }
        actvProgram.setOnItemClickListener{ _, _, _, _ ->
            actvProgram.setTextColor(resources.getColor(R.color.appBlack))
            actvProgram.background = ResourcesCompat.getDrawable(resources, R.drawable.default_options_border_bg, null)
        }

        val displayMetrics: DisplayMetrics = resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        val columnWidthDp = 140
        val spanCount = (screenWidthDp / columnWidthDp).toInt()

        binding.rvSubjectButtons.layoutManager = GridLayoutManager(this, spanCount)
        adapter = ButtonAdapter(buttonList, onAddClicked = {
            // Navigate to Add Button Activity
            val intent = Intent(this, AddSubjectActivity::class.java)
            startActivityForResult(intent, ADD_BUTTON_REQUEST_CODE)
        }, onRemoveClicked = { position ->
            buttonList.removeAt(position)
            adapter.notifyItemRemoved(position)
        }, context = this)
        binding.rvSubjectButtons.adapter = adapter

        val weaknessesOrStrengths = resources.getStringArray(R.array.weaknessesOrStrengths)

        binding.actvRole.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkAndUpdateUI(weaknessesOrStrengths)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            ADD_BUTTON_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val newButtonLabel = data?.getStringExtra("NEW_BUTTON_LABEL")
                    if (!newButtonLabel.isNullOrBlank()) {
                        // Check if subject already exists
                        val isSubjectExist = buttonList.any { it.label == newButtonLabel }
                        if (isSubjectExist) {
                            // Show dialog if subject already exists
                            MaterialAlertDialogBuilder(this)
                                .setTitle("Subject Already Added")
                                .setMessage("The subject '$newButtonLabel' is already on the list.")
                                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                                .show()
                        } else {
                            // Add new subject at the beginning
                            buttonList.add(0, ButtonData(newButtonLabel, false, R.color.appGrayButton, R.color.appGray6))
                            adapter.notifyDataSetChanged() // Notify adapter of changes
                        }
                    }
                }
            }
            IMAGE_PICKER_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val fileUri = data?.data
                    if (fileUri != null) {
                        uploadImage(fileUri)
                        binding.sivSetProfilePic.setImageURI(fileUri)
                    } else {
                        Toast.makeText(this, "Failed to pick image", Toast.LENGTH_SHORT).show()
                    }
                } else if (resultCode == ImagePicker.RESULT_ERROR) {
                    Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                Log.d(TAG, "Received unknown request code: $requestCode")
                Toast.makeText(this, "Unknown request code", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImage(fileUri: Uri){
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Uploading Image..")
        progressDialog.setMessage("Processing...")
        progressDialog.show()

        val ref: StorageReference = FirebaseStorage.getInstance().getReference()
            .child("images/${auth.currentUser!!.uid}/userProfile_--photo.jpg")
        ref.putFile(fileUri).addOnSuccessListener {
            progressDialog.dismiss()
            Toast.makeText(applicationContext, "Image Uploaded", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener{
            progressDialog.dismiss()
            Toast.makeText(applicationContext, "Image Failed to Upload", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAndUpdateUI(weaknessesOrStrengths: Array<String>) {
        when (binding.actvRole.text.toString().trim()) {
            "Student" -> {
                binding.tvWeaknessesOrStrength.text = weaknessesOrStrengths[0] // "Weaknesses"
                binding.layoutWeaknessesOrStrength.visibility = View.VISIBLE
            }
            "Tutor" -> {
                binding.tvWeaknessesOrStrength.text = weaknessesOrStrengths[1] // "Strengths"
                binding.layoutWeaknessesOrStrength.visibility = View.VISIBLE
            }
            else -> {
                binding.layoutWeaknessesOrStrength.visibility = View.GONE
            }
        }
    }

    private fun openGallery() {
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .start()
    }

    private fun validateAndSubmitData(): Boolean {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val program = binding.actvProgram.text.toString().trim()
        val role = binding.actvRole.text.toString().trim()
        val bio = binding.etBio.text.toString().trim()
        val subjects = buttonList.filter { it.label != "+Add" }.map { it.label }

        if (
            firstName.isEmpty() ||
            lastName.isEmpty() ||
            program.isEmpty() ||
            role.isEmpty() ||
            bio.isEmpty() ||
            subjects.isEmpty()
        ) {
            // Show dialog for incomplete fields
            MaterialAlertDialogBuilder(this)
                .setTitle("Missing Information")
                .setMessage("Please complete all fields to create your profile.")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
            return false
        } else {
            return true
        }
    }

    private fun updateUserAndNavigate(
        imageUrl: String
    ) {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val program = binding.actvProgram.text.toString().trim()
        val role = binding.actvRole.text.toString().trim()
        val bio = binding.etBio.text.toString().trim()
        val subjects = buttonList.filter { it.label != "+Add" }.map { it.label }
        val user: MutableMap<String, Any> = HashMap()

        user["firstName"] = firstName
        user["lastName"] = lastName
        user["fullName"] = "$firstName $lastName"
        user["program"] = program
        user["role"] = role
        user["bio"] = bio
        user["subjects"] = subjects
        user["profilePic"] = imageUrl
        if (role == "Tutor") { // Only add overallRating for tutors
            user["overallRating"] = 0.0
            user["feedbackAmount"] = 0
        }

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            firestoreDb.collection("users").document(currentUserId)
                .set(user)
                .addOnSuccessListener {
                    Log.d(TAG, "User profile updated with ID: $currentUserId")
                    if (role == "Tutor") {
                        firestoreDb.collection("users").document(currentUserId)
                            .collection("tutorData")
                            .document("data")
                            .set(hashMapOf("meetings" to listOf<String>())) // Initialize with an empty list
                            .addOnSuccessListener {
                                Log.d(TAG, "Tutor data document created")
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error creating tutor data document", e)
                            }

                        updateSubjectsWithTutor(currentUserId, subjects)
                    }
                    Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error updating profile", e)
                }
        } else {
            Log.w(TAG, "No logged-in user found.")
        }
    }
    private fun updateSubjectsWithTutor(tutorUID: String, subjects: List<String>) {
        for (subjectName in subjects) {
            firestoreDb.collection("subjects").document(subjectName)
                .update("relatedTutors", FieldValue.arrayUnion(tutorUID))
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully added tutor $tutorUID to subject $subjectName")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding tutor $tutorUID to subject $subjectName", e)
                }
        }
    }

    companion object {
        const val ADD_BUTTON_REQUEST_CODE = 1
        const val IMAGE_PICKER_REQUEST_CODE = 2404
    }
}