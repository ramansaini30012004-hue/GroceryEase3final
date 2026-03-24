package com.example.groceryease3

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.*

class StoresFragment : Fragment(R.layout.fragment_stores) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ShopAdapter
    private val shopList = mutableListOf<Shop>()
    private lateinit var database: DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        recyclerView = view.findViewById(R.id.recyclerViewShops)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize adapter with empty location first
        adapter = ShopAdapter(requireContext(), shopList)
        recyclerView.adapter = adapter

        database = FirebaseDatabase.getInstance().reference

        getCurrentLocation()
        loadShops()
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                adapter.updateLocation(it.latitude, it.longitude)
                Log.d("Store Fragment", "User Location: ${it.latitude}, ${it.longitude}")
            }
        }
    }

    private fun loadShops() {
        database.child("Users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                shopList.clear()
                for (shopSnap in snapshot.children) {
                    val shop = Shop()
                    shop.id = shopSnap.key ?: ""
                    shop.shopName = shopSnap.child("shopName").value?.toString() ?: ""
                    shop.image = shopSnap.child("image").value?.toString() ?: ""
                    shop.address = shopSnap.child("address").value?.toString() ?: ""

                    val lat = shopSnap.child("latitude").getValue(Double::class.java) ?: 0.0
                    val lng = shopSnap.child("longitude").getValue(Double::class.java) ?: 0.0

                    shop.latitude = lat
                    shop.longitude = lng

                    shopList.add(shop)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Store Fragment", "Firebase Error: ${error.message}")
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        }
    }
}