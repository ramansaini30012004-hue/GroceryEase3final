package com.example.groceryease3

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.groceryease3.databinding.FragmentProductBinding
import com.example.groceryease3.databinding.ItemProductsBinding
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class ProductFragment : Fragment(R.layout.fragment_product) {

    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ProductsAdapter
    private val productList = ArrayList<Product>()
    private val fullList = ArrayList<Product>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProductBinding.bind(view)

        setupRecyclerView()
        setupSearch()
        fetchData()
    }

    private fun setupRecyclerView() {
        adapter = ProductsAdapter(requireContext(), productList)

        // Use GridLayoutManager with 2 columns
        binding.rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvProducts.adapter = adapter

        adapter.setOnItemClickListener { product ->
            val intent = Intent(requireContext(), ProductActivity::class.java)
            intent.putExtra("productId", product.id)
//            intent.putExtra("shopId", product.id ?: product.)
            startActivity(intent)
        }
    }

    private fun setupSearch() {
        binding.etSearchProducts.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                filter(text.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun fetchData() {
        FirebaseDatabase.getInstance().getReference("products")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    fullList.clear()
                    for (snap in snapshot.children) {
                        val product = snap.getValue(Product::class.java)
                        product?.let {
                            if (it.id == null) it.id = snap.key.toString()
                            fullList.add(it)
                        }
                    }
                    filter(binding.etSearchProducts.text.toString())
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun filter(query: String) {
        productList.clear()
        val lowerQuery = query.lowercase()
        for (item in fullList) {
            if ((item.name?.lowercase()?.contains(lowerQuery) == true) ||
                (item.category?.lowercase()?.contains(lowerQuery) == true)) {
                productList.add(item)
            }
        }
        adapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ProductsAdapter(
    private val context: Context,
    private val productList: List<Product>
) : RecyclerView.Adapter<ProductsAdapter.ViewHolder>() {

    private var onItemClickListener: ((Product) -> Unit)? = null

    fun setOnItemClickListener(listener: (Product) -> Unit) {
        onItemClickListener = listener
    }

    class ViewHolder(val binding: ItemProductsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductsBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]

        holder.binding.tvProductName.text = product.name
        holder.binding.tvProductPrice.text = "₹${product.price}"
        holder.binding.tvProductQuantity.text = "${product.qty} ${product.unit}"

        // Reset image to default before loading
        holder.binding.ivProductImage.setImageResource(R.drawable.basket)

        // Native Async Image Loading
        // Inside onBindViewHolder
        if (!product.image.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // STEP 1: Handle potential Base64 headers
                    val pureBase64 = if (product.image!!.contains(",")) {
                        product.image!!.substringAfter(",")
                    } else {
                        product.image!!
                    }

                    // STEP 2: Decode the string into bytes
                    val imageBytes = android.util.Base64.decode(pureBase64, android.util.Base64.DEFAULT)

                    // STEP 3: Convert bytes to a Bitmap
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                    withContext(Dispatchers.Main) {
                        holder.binding.ivProductImage.setImageBitmap(bitmap)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(product)
        }
    }

    override fun getItemCount(): Int = productList.size
}