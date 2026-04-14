package com.example.groceryease3

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.groceryease3.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Already logged in
        if (auth.currentUser != null) {
            startActivity(Intent(this, BottomNavigationActivity::class.java))
            finish()
        }

        // REGISTER
        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, CreateAccountActivity::class.java))
            finish()
//            val email = binding.emailTv.text.toString().trim()
//            val password = binding.passwordTv.text.toString().trim()
//
//            if (email.isEmpty()) {
//                binding.emailTv.error = "Enter Email"
//                return@setOnClickListener
//            }
//
//            if (password.isEmpty()) {
//                binding.passwordTv.error = "Enter Password"
//                return@setOnClickListener
//            }
//
//            auth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener {
//
//                    if (it.isSuccessful) {
//
//                        Toast.makeText(this, "Registered", Toast.LENGTH_SHORT).show()
//
//                        val intent = Intent(this, BottomNavigationActivity::class.java)
//                        intent.putExtra("openProfile", true) // 🔥 IMPORTANT
//                        startActivity(intent)
//                        finish()
//
//                    } else {
//                        Toast.makeText(this, it.exception?.message, Toast.LENGTH_LONG).show()
//                    }
//                }
        }

        // LOGIN
        binding.btnLogin.setOnClickListener {

            val email = binding.emailTv.text.toString().trim()
            val password = binding.passwordTv.text.toString().trim()

            if (email.isEmpty()) {
                binding.emailTv.error = "Enter Email"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.passwordTv.error = "Enter Password"
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {

                    if (it.isSuccessful) {

                        Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show()

                        startActivity(Intent(this, BottomNavigationActivity::class.java))
                        finish()

                    } else {
                        Toast.makeText(this, it.exception?.message, Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}