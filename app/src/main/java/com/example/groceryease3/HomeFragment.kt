package com.example.groceryease3

import android.os.*
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var categoryRecycler: RecyclerView
    private lateinit var productRecycler: RecyclerView
    private lateinit var tvName: TextView

    private lateinit var productAdapter: HomeProductAdapter
    private val productList = ArrayList<HomeProduct>()

    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 0

    private val bannerList = listOf(
        R.drawable.banner1,
        R.drawable.banner2
    )

    // ✅ STORE SHOP DATA ONCE (OPTIMIZED)
    private var shopName = "Shop"
    private var shopImage = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        viewPager = view.findViewById(R.id.bannerViewPager)
        categoryRecycler = view.findViewById(R.id.categoryRecycler)
        productRecycler = view.findViewById(R.id.productRecycler)
        tvName = view.findViewById(R.id.tvName)

        tvName.text = "Hello, Ramandeep"

        setupBanner()
        setupCategories()
        setupProducts()

        fetchShopData() // ✅ call once

        return view
    }

    private fun setupBanner() {
        viewPager.adapter = BannerAdapter(bannerList)

        val runnable = object : Runnable {
            override fun run() {
                currentPage = (currentPage + 1) % bannerList.size
                viewPager.setCurrentItem(currentPage, true)
                handler.postDelayed(this, 6000)
            }
        }
        handler.postDelayed(runnable, 6000)
    }

    private fun setupCategories() {

        val list = listOf(
            Category("Vegetables", R.drawable.vegetales),
            Category("Fruits", R.drawable.fruits),
            Category("Spices", R.drawable.spices),
            Category("Dairy", R.drawable.dairy),
            Category("Oils", R.drawable.oils),
            Category("Bakery", R.drawable.bakery),
            Category("Household", R.drawable.household),
            Category("Pulses", R.drawable.pulses),
            Category("Beverages", R.drawable.beverages),
            Category("Snacks", R.drawable.snacks)
        )

        categoryRecycler.layoutManager = GridLayoutManager(requireContext(), 5)

        categoryRecycler.adapter = CategoryAdapter(list) {
            fetchProducts(it.name) // ✅ click → filter
        }
    }

    private fun setupProducts() {
        productRecycler.layoutManager = LinearLayoutManager(requireContext())
        productAdapter = HomeProductAdapter(requireContext(), productList)
        productRecycler.adapter = productAdapter
    }

    // ✅ FETCH SHOP DATA ONCE (OPTIMIZED)
    private fun fetchShopData() {
        val db = FirebaseDatabase.getInstance().reference

        db.child("Users").limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    for (user in snapshot.children) {
                        shopName = user.child("shopName").value.toString()
                        shopImage = user.child("image").value.toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // ✅ FINAL FILTERED FETCH
    private fun fetchProducts(categoryName: String) {

        val db = FirebaseDatabase.getInstance().reference

        Log.d("CHECK", "Category Clicked: $categoryName")

        db.child("products")
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    productList.clear()

                    if (!snapshot.exists()) {
                        Log.d("CHECK", "No Data Found")
                        productAdapter.notifyDataSetChanged()
                        return
                    }

                    for (snap in snapshot.children) {

                        val product = snap.getValue(Product::class.java)

                        // ✅ 🔥 FILTER APPLIED HERE
                        if (product != null &&
                            product.category.equals(categoryName, ignoreCase = true)
                        ) {

                            val item = HomeProduct(
                                name = product.name,
                                price = product.price,
                                image = product.image,
                                shopName = shopName,
                                shopImage = shopImage
                            )

                            productList.add(item)
                        }
                    }

                    // ✅ notify once (BEST PRACTICE)
                    productAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ERROR", error.message)
                }
            })
    }
}