package com.example.groceryease3

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth

class BottomNavigationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bottom_navigation)

        val navView = findViewById<BottomNavigationView>(R.id.nav_view)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController

        // 🔥 Register / Login check
        val openProfile = intent.getBooleanExtra("openProfile", false)

        if (openProfile) {
            navController.navigate(R.id.navigation_profile)
        } else {
            navController.navigate(R.id.navigation_home)
        }

        // 🔥 Bottom Navigation Click
        navView.setOnItemSelectedListener { item ->

//            // ✅ SAME PREFS AS PROFILE
//            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "default"
//            val prefs = getSharedPreferences("UserPrefs_$uid", MODE_PRIVATE)
//
//            val name = prefs.getString("name", "")
//            val email = prefs.getString("email", "")
//
//            // 🔴 Block Home & Store
//            if ((name.isNullOrEmpty() || email.isNullOrEmpty())
//                && item.itemId != R.id.navigation_profile
//            ) {
//                Toast.makeText(this, "Complete Profile First", Toast.LENGTH_SHORT).show()
//                return@setOnItemSelectedListener false
//            }

            // ✅ Navigate
            navController.navigate(item.itemId)
            true
        }
    }
}