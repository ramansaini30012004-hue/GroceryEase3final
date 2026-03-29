package com.example.groceryease3

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.*
import android.speech.RecognizerIntent
import android.text.*
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var categoryRecycler: RecyclerView
    private lateinit var productRecycler: RecyclerView
    private lateinit var tvName: TextView

    private lateinit var etSearch: EditText
    private lateinit var searchBtn: ImageView
    private lateinit var micBtn: ImageView

    private val SPEECH_REQUEST_CODE = 100

    private lateinit var productAdapter: ProductAdapter
    private lateinit var categoryAdapter: CategoryAdapter

    private val productList = ArrayList<Product>()
    private val allProducts = ArrayList<Product>()

    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 0

    private val bannerList = listOf(
        R.drawable.banner1,
        R.drawable.banner2
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        viewPager = view.findViewById(R.id.bannerViewPager)
        categoryRecycler = view.findViewById(R.id.categoryRecycler)
        productRecycler = view.findViewById(R.id.productRecycler)
        tvName = view.findViewById(R.id.tvName)

        etSearch = view.findViewById(R.id.etSearch)
        searchBtn = view.findViewById(R.id.search_24)
        micBtn = view.findViewById(R.id.micBtn)

        setupBanner()
        setupCategories()
        setupProducts()
        setupSearch()

        loadAllProducts()

        return view
    }

    override fun onResume() {
        super.onResume()
        val name = getUserPrefs().getString("name", "User")
        tvName.text = "Hello, $name"
    }

    private fun getUserPrefs(): SharedPreferences {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "default"
        return requireActivity().getSharedPreferences("UserPrefs_$uid", Context.MODE_PRIVATE)
    }

    // ================= PRODUCTS =================
    private fun setupProducts() {

        productRecycler.layoutManager = LinearLayoutManager(requireContext())
        productRecycler.setHasFixedSize(true)

        productAdapter = ProductAdapter(requireContext(), productList, 0.0, 0.0)
        productRecycler.adapter = productAdapter

        // 🔥 OPTIONAL: item click (future use)
        productAdapter.setOnItemClickListener { product ->
            Toast.makeText(requireContext(), product.name, Toast.LENGTH_SHORT).show()
        }
    }

    // ================= LOAD PRODUCTS =================
    private fun loadAllProducts() {

        FirebaseDatabase.getInstance().getReference("products")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    allProducts.clear()

                    for (snap in snapshot.children) {
                        val product = snap.getValue(Product::class.java)

                        if (product != null && !product.id.isNullOrEmpty()) {
                            allProducts.add(product)
                        }
                    }

                    productList.clear()
                    productList.addAll(allProducts)
                    productAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // ================= SEARCH =================
    private fun setupSearch() {

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                filterProducts(text.toString())
            }
        })

        searchBtn.setOnClickListener {
            filterProducts(etSearch.text.toString())
        }

        micBtn.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            startActivityForResult(intent, SPEECH_REQUEST_CODE)
        }
    }

    private fun filterProducts(query: String) {

        if (query.isEmpty()) {
            productList.clear()
            productList.addAll(allProducts)
            productAdapter.notifyDataSetChanged()
            return
        }

        val filtered = allProducts.filter {
            (it.name?.lowercase() ?: "").contains(query.lowercase()) ||
                    (it.category?.lowercase() ?: "").contains(query.lowercase())
        }

        productList.clear()
        productList.addAll(filtered)
        productAdapter.notifyDataSetChanged()
    }

    // ================= CATEGORY =================
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

        categoryAdapter = CategoryAdapter(list) {
            fetchProducts(it.name)
        }

        categoryRecycler.layoutManager = GridLayoutManager(requireContext(), 5)
        categoryRecycler.adapter = categoryAdapter
    }

    private fun fetchProducts(categoryName: String) {

        val filtered = allProducts.filter {
            it.category.equals(categoryName, true)
        }

        productList.clear()
        productList.addAll(filtered)
        productAdapter.notifyDataSetChanged()
    }

    // ================= VOICE =================
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!result.isNullOrEmpty()) {
                etSearch.setText(result[0])
            }
        }
    }

    // ================= BANNER =================
    private fun setupBanner() {

        viewPager.adapter = BannerAdapter(bannerList)

        val runnable = object : Runnable {
            override fun run() {
                currentPage = (currentPage + 1) % bannerList.size
                viewPager.setCurrentItem(currentPage, true)
                handler.postDelayed(this, 3000)
            }
        }

        handler.postDelayed(runnable, 3000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }
}