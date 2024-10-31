package com.example.mobappprototype.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.mobappprototype.R
import com.example.mobappprototype.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException

private const val TAG = "RegisterActivity"
class RegisterActivity : AppCompatActivity() {
    lateinit var registerActivityBinding : ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerActivityBinding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = registerActivityBinding.root
        setContentView(view)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()

        registerActivityBinding.etEmailRegister.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                registerActivityBinding.etEmailRegister.setTextColor(resources.getColor(R.color.appBlack))
                registerActivityBinding.etEmailRegister.background = ResourcesCompat.getDrawable(resources, R.drawable.default_options_border_bg, null)

            } else {
                registerActivityBinding.etEmailRegister.setTextColor(resources.getColor(R.color.appGray8))
                registerActivityBinding.etEmailRegister.background = ResourcesCompat.getDrawable(resources, R.drawable.default_options_border_bg_gray, null)
            }
        }

        registerActivityBinding.etPasswordRegister.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                registerActivityBinding.etPasswordRegister.setTextColor(resources.getColor(R.color.appBlack))
                registerActivityBinding.etPasswordRegister.background = ResourcesCompat.getDrawable(resources, R.drawable.default_options_border_bg, null)

            } else {
                registerActivityBinding.etPasswordRegister.setTextColor(resources.getColor(R.color.appGray8))
                registerActivityBinding.etPasswordRegister.background = ResourcesCompat.getDrawable(resources, R.drawable.default_options_border_bg_gray, null)
            }
        }

        registerActivityBinding.etConfirmPasswordRegister.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                registerActivityBinding.etConfirmPasswordRegister.setTextColor(resources.getColor(R.color.appBlack))
                registerActivityBinding.etConfirmPasswordRegister.background = ResourcesCompat.getDrawable(resources, R.drawable.default_options_border_bg, null)

            } else {
                registerActivityBinding.etConfirmPasswordRegister.setTextColor(resources.getColor(R.color.appGray8))
                registerActivityBinding.etConfirmPasswordRegister.background = ResourcesCompat.getDrawable(resources, R.drawable.default_options_border_bg_gray, null)
            }
        }

        registerActivityBinding.btnRegisterReal.setOnClickListener {
            registerActivityBinding.layoutMainActivity.visibility = View.GONE
            registerActivityBinding.loadingLayout.visibility = View.VISIBLE
            val email = registerActivityBinding.etEmailRegister.text.toString()
            val password = registerActivityBinding.etPasswordRegister.text.toString()
            val confirmPassword = registerActivityBinding.etConfirmPasswordRegister.text.toString()
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Email/password cannot be empty", Toast.LENGTH_SHORT).show()
                registerActivityBinding.loadingLayout.visibility = View.GONE
                registerActivityBinding.layoutMainActivity.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (password == confirmPassword){
                register(email, password)
            } else {
                Toast.makeText(this, "Password does not match", Toast.LENGTH_SHORT).show()
                registerActivityBinding.loadingLayout.visibility = View.GONE
                registerActivityBinding.layoutMainActivity.visibility = View.VISIBLE
                return@setOnClickListener
            }
        }

        registerActivityBinding.ivBackFRegister.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            registerActivityBinding.layoutMainActivity.visibility = View.GONE
            registerActivityBinding.loadingLayout.visibility = View.VISIBLE
            startActivity(intent)
            finish()
        }

    }
    private fun register(email: String, password: String) {
        //logic of creating user
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail: success")

                    Toast.makeText(baseContext, "Registration Successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Log.w(TAG, "createUserWithEmail: failure", task.exception)
                    registerActivityBinding.loadingLayout.visibility = View.GONE
                    registerActivityBinding.layoutMainActivity.visibility = View.VISIBLE

                    if (task.exception is FirebaseAuthException
                        && task.exception?.message == "The email address is already in use by another account.") {
                        // Email address already in use
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Email Address Already in Use")
                            .setMessage("This email address is already associated with an account. Please try another email.")
                            .setPositiveButton("OK") { _, _ ->
                            }
                            .show()
                    } else {
                        Toast.makeText(baseContext, "Registration failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(baseContext, "Something went wrong, Registration Failed", Toast.LENGTH_SHORT).show()
                registerActivityBinding.loadingLayout.visibility = View.GONE
                registerActivityBinding.layoutMainActivity.visibility = View.VISIBLE
            }
    }

    override fun onResume() {
        super.onResume()
        registerActivityBinding.loadingLayout.visibility = View.GONE
        registerActivityBinding.layoutMainActivity.visibility = View.VISIBLE
    }
}