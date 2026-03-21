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
        val shopNameHeader = findViewById<TextView>(R.id.shopNameHeader)
        val shopAddressHeader = findViewById<TextView>(R.id.shopAddressHeader)
        val shopImageHeader = findViewById<ImageView>(R.id.shopImageHeader)
        categoryLayout = findViewById(R.id.categoryLayout)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize adapter with the empty filteredList
        adapter = ProductAdapter(this, filteredList)
        recyclerView.adapter = adapter

        // 3. Get Data from Intent
        val shopName = intent.getStringExtra("shopName") ?: "Products"
        val shopAddress = intent.getStringExtra("shopAddress") ?: ""
        val shopImageBase64 = intent.getStringExtra("shopImage") ?: "" // This is the Base64 string
        shopId = intent.getStringExtra("shopId").toString()

        // 4. Update UI Text
        supportActionBar?.title = "Store Products"
        shopNameHeader.text = shopName
        shopAddressHeader.text = shopAddress

        // 5. Decode and Load Shop Header Image (Base64)
        if (!shopImageBase64.isNullOrBlank()) {
            try {
                val cleanBase64 = shopImageBase64
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

        fetchProducts()
    }

    private fun fetchProducts() {
        val database = FirebaseDatabase.getInstance().reference
        database.child("products").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fullProductList.clear()
                val categories = mutableSetOf<String>()
                categories.add("All")

                for (snap in snapshot.children) {
                    val product = snap.getValue(Product::class.java)
                    // Check if product's ID field matches this shop's ID
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

        // Use the updateList method from your Adapter
        adapter.updateList(result)
    }
}