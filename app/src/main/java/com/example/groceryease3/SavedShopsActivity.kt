package com.example.groceryease3

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SavedShopsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ShopAdapter
    private val shopList = mutableListOf<Shop>()

    private lateinit var database: DatabaseReference
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_shops)

        supportActionBar?.title = "Saved Shops"

        // 🔹 RecyclerView setup
        recyclerView = findViewById(R.id.recyclerSaved)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ShopAdapter(this, shopList)
        recyclerView.adapter = adapter

        // 🔹 Firebase setup
        database = FirebaseDatabase.getInstance().reference
        userId = FirebaseAuth.getInstance().currentUser?.uid

        // 🔹 Load data
        loadSavedShops()
    }

    private fun loadSavedShops() {

        val uid = userId

        if (uid.isNullOrEmpty()) {
            Log.e("SavedShops", "User not logged in")
            return
        }

        database.child("Favorites").child(uid)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    shopList.clear()

                    for (snap in snapshot.children) {
                        val shop = snap.getValue(Shop::class.java)

                        if (shop != null) {
                            shop.isFavorite = true // ❤️ keep heart red
                            shopList.add(shop)
                        }
                    }

                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("SavedShops", "Firebase Error: ${error.message}")
                }
            })
    }
}