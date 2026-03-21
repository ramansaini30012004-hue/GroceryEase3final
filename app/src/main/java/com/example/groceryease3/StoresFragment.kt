package com.example.groceryease3

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class StoresFragment : Fragment(R.layout.fragment_stores) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ShopAdapter
    private val shopList = mutableListOf<Shop>()
    private lateinit var database: DatabaseReference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewShops)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ShopAdapter(requireContext(), shopList)
        recyclerView.adapter = adapter

        database = FirebaseDatabase.getInstance().reference
        Toast.makeText(requireActivity(), "Store Fragment", Toast.LENGTH_SHORT).show()

        loadShops()
    }

    private fun loadShops() {

        Log.d("Store Fragment", "🔥 loadShops called")

        database.child("Users")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    Log.d("Store Fragment", "✅ onDataChange triggered")
                    Log.d("Store Fragment", "📦 Snapshot exists: ${snapshot.exists()}")
                    Log.d("Store Fragment", "📊 Children count: ${snapshot.childrenCount}")

                    shopList.clear()

                    if (!snapshot.exists()) {
                        Log.e("Store Fragment", "❌ No data found in 'Users'")
                        return
                    }

                    for (shopSnap in snapshot.children) {

                        Log.d("Store Fragment", "➡️ Raw Data: ${shopSnap.value}")

                        val shop = Shop()

                        // ID
                        shop.id = shopSnap.key ?: ""
                        Log.d("Store Fragment", "🆔 ID: ${shop.id}")

                        // Fields
                        shop.shopName = shopSnap.child("shopName").value?.toString() ?: ""
                        shop.image = shopSnap.child("image").value?.toString() ?: ""
                        shop.address = shopSnap.child("address").value?.toString() ?: ""

                        Log.d("Store Fragment", "🏪 Name: ${shop.shopName}")
                        Log.d("Store Fragment", "📍 Address: ${shop.address}")
                        Log.d("Store Fragment", "🖼 Image: ${shop.image}")

                        // Lat/Lng
                        val lat = shopSnap.child("lat").getValue(Double::class.java) ?: 0.0
                        val lng = shopSnap.child("lng").getValue(Double::class.java) ?: 0.0

                        Log.d("Store Fragment", "🌍 Lat: $lat , Lng: $lng")

                        shop.location.lat = lat
                        shop.location.lng = lng

                        // Distance
                        shop.distanceText = "${(1..7).random()} km"

                        shopList.add(shop)
                    }

                    Log.d("Store Fragment", "📋 Final Shop List Size: ${shopList.size}")
                    Log.d("Store Fragment", "📋 Shop List Data: $shopList")

                    adapter.notifyDataSetChanged()
                    Log.d("Store Fragment", "🔄 Adapter notified")

                    // UI check
                    if (shopList.isEmpty()) {
                        Log.e("Store Fragment", "❌ List empty → UI will show nothing")
                    } else {
                        Log.d("Store Fragment", "✅ Data ready for UI")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Store Fragment", "🚨 Firebase Error: ${error.message}")
                }
            })
    }
}