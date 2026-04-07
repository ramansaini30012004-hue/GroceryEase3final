package com.example.groceryease3

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class StoresFragment : Fragment(R.layout.fragment_stores) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ShopAdapter

    private val shopList = mutableListOf<Shop>()
    private val allShops = mutableListOf<Shop>()

    private lateinit var database: DatabaseReference

    private lateinit var searchEditText: EditText
    private lateinit var searchBtn: ImageView
    private lateinit var micBtn: ImageView

    private val SPEECH_REQUEST_CODE = 200

    // 📍 LOCATION
    private var userLat = 0.0
    private var userLng = 0.0

    private var locationEnabled = false
    private var isLocationReady = false
    private var isDataLoaded = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewShops)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ShopAdapter(requireContext(), shopList)
        recyclerView.adapter = adapter

        database = FirebaseDatabase.getInstance().reference

        searchEditText = view.findViewById(R.id.searchEditText)
        searchBtn = view.findViewById(R.id.searchIcon)
        micBtn = view.findViewById(R.id.micIcon)

        setupSearch()

        // ✅ पहले ALL shops दिखाओ
        loadAllShops()

        // ✅ Toast
        Toast.makeText(
            context,
            "Enable location to find shops within 5 km radius",
            Toast.LENGTH_LONG
        ).show()

        // 📍 location
        getCurrentLocation()
    }

    // 🔍 SEARCH
    private fun setupSearch() {

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                filterShops(text.toString())
            }
        })

        searchBtn.setOnClickListener {
            filterShops(searchEditText.text.toString())
        }

        micBtn.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            startActivityForResult(intent, SPEECH_REQUEST_CODE)
        }
    }

    // 🔎 FILTER SEARCH
    private fun filterShops(query: String) {

        val baseList = if (locationEnabled) shopList else allShops

        if (query.isEmpty()) {
            shopList.clear()
            shopList.addAll(baseList)
            adapter.notifyDataSetChanged()
            return
        }

        val filtered = baseList.filter {
            it.shopName.lowercase().contains(query.lowercase())
        }

        shopList.clear()
        shopList.addAll(filtered)
        adapter.notifyDataSetChanged()
    }

    // 📍 GET LOCATION
    private fun getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        LocationServices.getFusedLocationProviderClient(requireActivity())
            .lastLocation.addOnSuccessListener { location ->
                location?.let {
                    userLat = it.latitude
                    userLng = it.longitude

                    locationEnabled = true
                    isLocationReady = true

                    if (isDataLoaded) {
                        apply5KmFilter()
                    }
                }
            }
    }

    // 🏪 LOAD ALL SHOPS
    private fun loadAllShops() {

        database.child("Users").addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                shopList.clear()
                allShops.clear()

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
                    allShops.add(shop)
                }

                isDataLoaded = true

                // 👉 अगर location ready है तो filter लगाओ
                if (isLocationReady) {
                    apply5KmFilter()
                }

                loadFavorites()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("StoreFragment", error.message)
            }
        })
    }

    // 🔥 APPLY 5KM FILTER
    private fun apply5KmFilter() {

        val filtered = allShops.filter {

            if (it.latitude == 0.0 || it.longitude == 0.0) return@filter false

            val distance = calculateDistance(userLat, userLng, it.latitude, it.longitude)

            Log.d("DISTANCE", "${it.shopName} -> $distance km")

            distance <= 5
        }

        Log.d("FILTER", "Total: ${filtered.size}")

        shopList.clear()
        shopList.addAll(filtered)

        adapter.updateLocation(userLat, userLng)
        adapter.notifyDataSetChanged()
    }

    // 📏 DISTANCE
    private fun calculateDistance(
        userLat: Double,
        userLng: Double,
        shopLat: Double,
        shopLng: Double
    ): Double {

        val results = FloatArray(1)

        android.location.Location.distanceBetween(
            userLat,
            userLng,
            shopLat,
            shopLng,
            results
        )

        return results[0].toDouble() / 1000
    }

    // ❤️ FAVORITES
    private fun loadFavorites() {

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        database.child("Favorites").child(userId)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    for (shop in shopList) {
                        shop.isFavorite = false
                    }

                    for (favSnap in snapshot.children) {
                        val favId = favSnap.key
                        shopList.find { it.id == favId }?.isFavorite = true
                    }

                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("StoreFragment", error.message)
                }
            })
    }

    // 🎤 VOICE SEARCH
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!result.isNullOrEmpty()) {
                val text = result[0]
                searchEditText.setText(text)
                filterShops(text)
            }
        }
    }

    // 📍 PERMISSION RESULT
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 100 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        }
    }
}