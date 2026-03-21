package com.example.groceryease3

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import kotlin.apply

class HelpCenterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help_center)

        // Toolbar mein Back button aur Title setup
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Help Center"

        // Views initialize karein
        val cardWhatsApp = findViewById<CardView>(R.id.cardWhatsApp)
        val cardCall = findViewById<CardView>(R.id.cardCall)
        val cardEmail = findViewById<CardView>(R.id.cardEmail)

        // --- CLICK LISTENERS ---

        // 1. WhatsApp Logic
        cardWhatsApp.setOnClickListener {
            val phoneNumber = "918728957154" // Aapka number
            val message = "Hello GroceryEase Support, I need help."
            val url = "https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}"
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "WhatsApp not installed!", Toast.LENGTH_SHORT).show()
            }
        }

        // 2. Call Logic
        cardCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:8728957154")
            startActivity(intent)
        }

        // 3. Email Logic (Isse mail khulega)
        cardEmail.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:sainiramandeep567@gmail.com") // Aapka email jo image mein tha
                putExtra(Intent.EXTRA_SUBJECT, "Customer Support Request")
                putExtra(Intent.EXTRA_TEXT, "Hi Team, I need help with...")
            }
            try {
                // Chooser dikhayega taaki user Gmail ya koi aur app select kar sake
                startActivity(Intent.createChooser(emailIntent, "Send Email via..."))
            } catch (e: Exception) {
                Toast.makeText(this, "No Email app found!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Back button handle karne ke liye (Toolbar arrow click)
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}