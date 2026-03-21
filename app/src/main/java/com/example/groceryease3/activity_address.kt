package com.example.groceryease3

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class AddressActivity : AppCompatActivity() {

    private lateinit var tvAddress: TextView
    private lateinit var btnGetLocation: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // ✅ SharedPrefs
    private fun getUserPrefs(): android.content.SharedPreferences {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "default"
        return getSharedPreferences("UserPrefs_$uid", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address)

        tvAddress = findViewById(R.id.tvAddress)
        btnGetLocation = findViewById(R.id.btnGetLocation)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // ✅ LOAD SAVED ADDRESS
        val savedAddress = getUserPrefs().getString("user_address", null)
        if (!savedAddress.isNullOrEmpty()) {
            tvAddress.text = savedAddress
        }

        btnGetLocation.setOnClickListener {
            turnOnLocation()
        }
    }

    // 🔥 Enable Location Dialog (like GPay)
    private fun turnOnLocation() {

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 1000
        ).build()

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            getLocation()
        }

        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(this, 200)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    // 🔥 Handle result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 200) {
            getLocation()
        }
    }

    // 🔥 Get location
    private fun getLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->

            if (location != null) {

                val lat = location.latitude
                val lng = location.longitude

                val geocoder = Geocoder(this, Locale.getDefault())

                try {
                    val list = geocoder.getFromLocation(lat, lng, 1)
                    val address = list?.get(0)?.getAddressLine(0) ?: "Unknown"

                    // ✅ SHOW
                    tvAddress.text = address

                    // ✅ SAVE LOCAL
                    getUserPrefs().edit()
                        .putString("user_address", address)
                        .putString("lat", lat.toString())
                        .putString("lng", lng.toString())
                        .apply()

                    // ✅ SAVE FIREBASE
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid != null) {
                        val map = HashMap<String, Any>()
                        map["address"] = address
                        map["lat"] = lat
                        map["lng"] = lng

                        FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(uid)
                            .child("location")
                            .setValue(map)
                    }

                    Toast.makeText(this, "Address Saved", Toast.LENGTH_SHORT).show()

                } catch (e: Exception) {
                    Toast.makeText(this, "Error getting address", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(this, "Turn ON Location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 🔥 Permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == 100 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            getLocation()
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }
}