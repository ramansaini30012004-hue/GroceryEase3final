package com.example.groceryease3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController

class BottomNavigationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bottom_navigation)

        // Bottom Navigation View
        val navView = findViewById<BottomNavigationView>(R.id.nav_view)

        // NavHostFragment find karo (IMPORTANT 🔥)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                    as NavHostFragment

        // NavController
        val navController = navHostFragment.navController

        // Connect bottom nav with nav controller
        navView.setupWithNavController(navController)
    }
}