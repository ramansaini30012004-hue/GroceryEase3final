package com.example.groceryease3

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProductActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter
    private val productList = ArrayList<Product>()

    var shopId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ProductAdapter(this, productList)
        recyclerView.adapter = adapter

        val shopName = intent.getStringExtra("shopName")
        title = shopName ?: "Products"
        shopId = intent.getStringExtra("shopId").toString()


        var auth = FirebaseAuth.getInstance().uid

//        fetchUserProductsFromCategories(auth.toString(), { listData->
//
//            Log.d("Product Fragment",listData.toString())
//
//        })
        fetchProducts()
    }


//    fun fetchUserProductsFromCategories(currentUserId: String, callback: (List<Product>) -> Unit) {
//        val database = FirebaseDatabase.getInstance().getReference("products")
//        val userProducts = mutableListOf<Product>()
//
//        database.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                userProducts.clear()
//
//                // 1. Loop through each category (e.g., "bakery", "fruits")
//                for (categorySnapshot in snapshot.children) {
//
//                    // 2. Loop through products inside that category
//                    for (productSnapshot in categorySnapshot.children) {
//                        val product = productSnapshot.getValue(Product::class.java)
//
//                        // 3. Check if the product's 'id' matches the current user
//                        if (product != null && product.id == currentUserId) {
//                            userProducts.add(product)
//                        }
//                    }
//                }
//
//                // Return the final list to the UI
//                callback(userProducts)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                callback(emptyList())
//            }
//        })
//    }

    private fun fetchProducts() {

        val database = FirebaseDatabase.getInstance().reference

        var auth = FirebaseAuth.getInstance().currentUser?.uid
        database.child("products")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    productList.clear()

                    for (snap in snapshot.children) {

                        val product = snap.getValue(Product::class.java)

                        if (product != null) {

                            Log.d("PRODUCT_DEBUG", "Product: ${product.name}")
                            Log.d("PRODUCT_DEBUG", "Product: ${product.id}")
                            Log.d("PRODUCT_DEBUG", "Auth: ${auth}")


                            // ✅ Filter by current user
                            if (product.id == shopId) {
                                productList.add(product)
                            }
                        } else {
                            Log.e("PRODUCT_DEBUG", "Null product OR wrong structure")
                        }
                    }

                    Log.d("PRODUCT_DEBUG", "Final list size: ${productList.size}")

                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ProductActivity, error.message, Toast.LENGTH_SHORT).show()
                }
            })
    }


}