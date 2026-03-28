package com.example.groceryease3

import android.os.Bundle
import android.util.Base64
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*

class ProductActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter

    private val productList = ArrayList<Product>()
    private val fullList = ArrayList<Product>()

    private var selectedCategory = "ALL"

    private var shopLat = 0.0
    private var shopLng = 0.0
    private var shopId: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)

        val shopNameHeader = findViewById<TextView>(R.id.shopNameHeader)
        val shopAddressHeader = findViewById<TextView>(R.id.shopAddressHeader)
        val shopImageHeader = findViewById<ImageView>(R.id.shopImageHeader)

        shopId = intent.getStringExtra("shopId")
        val shopName = intent.getStringExtra("shopName")
        val shopAddress = intent.getStringExtra("shopAddress")
        val shopImage = intent.getStringExtra("shopImage")

        shopLat = intent.getDoubleExtra("shopLat", 0.0)
        shopLng = intent.getDoubleExtra("shopLng", 0.0)

        shopNameHeader.text = shopName ?: "Shop"
        shopAddressHeader.text = shopAddress ?: ""

        // 🔥 IMAGE LOAD
        if (!shopImage.isNullOrEmpty()) {
            try {
                val base64 = shopImage.substringAfter("base64,", shopImage)
                val bytes = Base64.decode(base64, Base64.DEFAULT)

                Glide.with(this)
                    .load(bytes)
                    .placeholder(R.drawable.basket)
                    .into(shopImageHeader)

            } catch (e: Exception) {
                shopImageHeader.setImageResource(R.drawable.basket)
            }
        } else {
            shopImageHeader.setImageResource(R.drawable.basket)
        }

        // 🔥 RECYCLER
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ProductAdapter(this, productList, shopLat, shopLng)
        recyclerView.adapter = adapter

        fetchProducts()
    }

    // 🔥 FETCH PRODUCTS + CATEGORY BUTTONS
    private fun fetchProducts() {

        val ref = FirebaseDatabase.getInstance().getReference("products")

        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                fullList.clear()
                val categorySet = HashSet<String>()

                for (snap in snapshot.children) {
                    val product = snap.getValue(Product::class.java)

                    if (product != null) {

                        // ⚠️ IMPORTANT: change if needed
                        if (product.id == shopId) {
                            fullList.add(product)
                        }

                        if (product.category.isNotEmpty()) {
                            categorySet.add(product.category.uppercase())
                        }
                    }
                }

                // 🔥 CATEGORY LIST
                val finalCategories = ArrayList<String>()
                finalCategories.add("ALL")
                finalCategories.addAll(categorySet)

                val categoryLayout = findViewById<LinearLayout>(R.id.categoryLayout)
                setupCategories(categoryLayout, finalCategories)

                filterProducts()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // 🔥 CATEGORY BUTTON UI
    private fun setupCategories(layout: LinearLayout, categories: List<String>) {

        layout.removeAllViews()

        for (cat in categories) {

            val btn = Button(this)
            btn.text = cat

            btn.setBackgroundResource(R.drawable.button_green)
            btn.setTextColor(getColor(android.R.color.white))

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(16, 0, 16, 0)
            btn.layoutParams = params

            btn.setOnClickListener {
                selectedCategory = cat
                filterProducts()
            }

            layout.addView(btn)
        }
    }

    // 🔥 FILTER LOGIC
    private fun filterProducts() {

        productList.clear()

        for (p in fullList) {
            if (selectedCategory == "ALL" ||
                p.category.equals(selectedCategory, true)
            ) {
                productList.add(p)
            }
        }

        adapter.notifyDataSetChanged()
    }
}