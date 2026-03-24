package com.example.groceryease3

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.*

class ProductActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter
    private lateinit var categoryLayout: LinearLayout

    // UI Headers
    private lateinit var shopNameHeader: TextView
    private lateinit var shopAddressHeader: TextView
    private lateinit var shopImageHeader: ImageView

    private val fullProductList = ArrayList<Product>()
    private val filteredList = ArrayList<Product>()

    private var shopId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)

        // 1. Setup Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        // 2. Initialize Views
        shopNameHeader = findViewById(R.id.shopNameHeader)
        shopAddressHeader = findViewById(R.id.shopAddressHeader)
        shopImageHeader = findViewById(R.id.shopImageHeader)
        categoryLayout = findViewById(R.id.categoryLayout)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 3. Get ID from Intent
        shopId = intent.getStringExtra("shopId") ?: ""

        // Pass shopId to ProductAdapter (matching the update we made to the adapter earlier)
        adapter = ProductAdapter(this, filteredList)
        recyclerView.adapter = adapter

        // 4. Check if we have data or need to fetch from Firebase
        val shopName = intent.getStringExtra("shopName") ?: ""

        if (shopName.isEmpty()) {
            // Fetch Shop Details from Firebase "Users" node
            fetchShopDetails()
        } else {
            // Use existing Intent data
            updateShopUI(
                shopName,
                intent.getStringExtra("shopAddress") ?: "",
                intent.getStringExtra("shopImage") ?: ""
            )
        }

        fetchProducts()
    }

    private fun fetchShopDetails() {
        val db = FirebaseDatabase.getInstance().reference.child("Users").child(shopId)
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("shopName").value?.toString() ?: "Store"
                    val address = snapshot.child("address").value?.toString() ?: ""
                    val image = snapshot.child("image").value?.toString() ?: ""

                    updateShopUI(name, address, image)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PRODUCT_ACT", "Failed to fetch shop details: ${error.message}")
            }
        })
    }

    private fun updateShopUI(name: String, address: String, imageBase64: String) {
        supportActionBar?.title = name
        shopNameHeader.text = name
        shopAddressHeader.text = address

        if (!imageBase64.isNullOrBlank()) {
            try {
                val cleanBase64 = imageBase64
                    .substringAfter("base64,")
                    .replace("\\s".toRegex(), "")
                    .trim()

                val imageBytes = Base64.decode(cleanBase64, Base64.NO_WRAP)

                Glide.with(this)
                    .asBitmap()
                    .load(imageBytes)
                    .placeholder(android.R.drawable.ic_menu_report_image)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(shopImageHeader)
            } catch (e: Exception) {
                Log.e("PRODUCT_ACT_ERROR", "Header image decode failed: ${e.message}")
            }
        }
    }

    private fun fetchProducts() {
        val database = FirebaseDatabase.getInstance().reference
        // Listening to "products" node
        database.child("products").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fullProductList.clear()
                val categories = mutableSetOf<String>()
                categories.add("All")

                for (snap in snapshot.children) {
                    val product = snap.getValue(Product::class.java)
                    // Note: Ensure your Product model has an 'id' or 'shopId' field that matches this.shopId
                    if (product != null && product.id == shopId) {
                        fullProductList.add(product)
                        if (!product.category.isNullOrEmpty()) {
                            categories.add(product.category)
                        }
                    }
                }

                setupCategoryButtons(categories.toList())
                filterProducts("All")
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProductActivity, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupCategoryButtons(categories: List<String>) {
        categoryLayout.removeAllViews()
        for (category in categories) {
            val button = MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(8, 0, 8, 0)
            button.layoutParams = params
            button.text = category
            button.setOnClickListener { filterProducts(category) }
            categoryLayout.addView(button)
        }
    }

    private fun filterProducts(category: String) {
        val result = if (category == "All") {
            fullProductList
        } else {
            fullProductList.filter { it.category == category }
        }
        adapter.updateList(result)
    }
}