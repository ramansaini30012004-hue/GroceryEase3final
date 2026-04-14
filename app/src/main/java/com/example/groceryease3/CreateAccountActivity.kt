package com.example.groceryease3

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.util.*

class CreateAccountActivity : AppCompatActivity() {

    private lateinit var ivProfile: ImageView
    private lateinit var auth: FirebaseAuth
    private var base64Image: String = ""

    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            ivProfile.setImageURI(it)
            base64Image = uriToBase64(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword) // Ensure this exists in XML
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnSelectImage = findViewById<Button>(R.id.btnSelectImage)
        ivProfile = findViewById(R.id.ivProfile)

        btnSelectImage.setOnClickListener {
            getImage.launch("image/*")
        }

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isNotEmpty() && email.isNotEmpty() && password.length >= 6) {
                createFirebaseAccount(name, email, password)
            } else {
                Toast.makeText(this, "Check fields (Password min 6 chars)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createFirebaseAccount(name: String, email: String, pass: String) {
        // 1. Create User in Firebase Auth
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    saveUserToDatabase(userId, name, email)
                } else {
                    Toast.makeText(this, "Auth Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserToDatabase(userId: String, name: String, email: String) {
        val database = FirebaseDatabase.getInstance().getReference("Users_data")

        val userMap = mapOf(
            "id" to userId,
            "name" to name,
            "email" to email,
            "imageUrl" to base64Image,
            "time" to System.currentTimeMillis().toString()
        )

        database.child(userId).setValue(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                // Proceed to next Activity (e.g., MainActivity)
                startActivity(Intent(this, BottomNavigationActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save profile: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uriToBase64(uri: Uri): String {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream) // Lower quality for Base64 efficiency
            val bytes = outputStream.toByteArray()
            Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e("Base64", "Conversion error", e)
            ""
        }
    }
}