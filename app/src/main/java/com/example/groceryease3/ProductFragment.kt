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

        binding.rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvProducts.adapter = adapter

        adapter.setOnItemClickListener { product ->
            val intent = Intent(requireContext(), ProductActivity::class.java)

            intent.putExtra("productId", product.id)
            intent.putExtra("shopId", product.id)   // ✅ FINAL FIX (id = shopId)

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
                            if (it.id.isEmpty()) it.id = snap.key.toString()
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
            if ((item.name.lowercase().contains(lowerQuery)) ||
                (item.category.lowercase().contains(lowerQuery))
            ) {
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

    class ViewHolder(val binding: ItemProductsBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemProductsBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]

        holder.binding.tvProductName.text = product.name
        holder.binding.tvProductPrice.text = "₹${product.price}"
        holder.binding.tvProductQuantity.text = "${product.qty} ${product.unit}"

        holder.binding.ivProductImage.setImageResource(R.drawable.basket)

        if (product.image.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val pureBase64 = if (product.image.contains(",")) {
                        product.image.substringAfter(",")
                    } else {
                        product.image
                    }

                    val imageBytes = android.util.Base64.decode(
                        pureBase64,
                        android.util.Base64.DEFAULT
                    )

                    val bitmap = BitmapFactory.decodeByteArray(
                        imageBytes,
                        0,
                        imageBytes.size
                    )

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