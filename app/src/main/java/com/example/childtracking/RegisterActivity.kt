package com.example.childtracking

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.childtracking.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        
        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Setup spinner
        setupSpinner()
        
        // Setup click listeners
        setupClickListeners()
    }

    private fun setupSpinner() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.user_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            binding.userTypeSpinner.adapter = adapter
        }
    }

    private fun setupClickListeners() {
        // Register button click
        binding.registerButton.setOnClickListener {
            performRegistration()
        }

        // Login text click
        binding.loginTextView.setOnClickListener {
            // Navigate back to login screen
            finish()
        }
    }

    private fun performRegistration() {
        val fullName = binding.fullNameEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        val mobile = binding.mobileEditText.text.toString().trim()
        val userType = binding.userTypeSpinner.selectedItem.toString()

        // Validate input fields
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || mobile.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Password validation (at least 6 characters)
        if (password.length < 6) {
            Toast.makeText(this, "Password should be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        // Disable register button to prevent multiple clicks
        binding.registerButton.isEnabled = false

        // Create account with Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // User creation successful, now save additional data to Firestore
                    val userId = auth.currentUser?.uid
                    val user = hashMapOf(
                        "fullName" to fullName,
                        "email" to email,
                        "mobile" to mobile,
                        "userType" to userType
                    )

                    // Save user data to Firestore
                    userId?.let {
                        db.collection("users").document(it)
                            .set(user)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                                // Navigate to MainActivity
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                // Enable register button again
                                binding.registerButton.isEnabled = true
                                Toast.makeText(this, "Error saving data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // Enable register button again
                    binding.registerButton.isEnabled = true
                    // If sign-up fails, display a message to the user
                    Toast.makeText(
                        this,
                        "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
} 