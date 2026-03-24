package com.example.groceryease3

import android.content.Context
import android.content.Intent
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ProductAdapter(
    private val context: Context,
    private var list: ArrayList<Product>,
   // 1. Added shopId to constructor
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.productName)
        val price: TextView = view.findViewById(R.id.productPrice)
        val image: ImageView = view.findViewById(R.id.productImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<Product>) {
        list = ArrayList(newList)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = list[position]

        holder.name.text = product.name
        holder.price.text = "₹${product.price} / ${product.qty} ${product.unit}"

        // --- Base64 Image Handling ---
        if (!product.image.isNullOrBlank()) {
            try {
                val cleanBase64 = product.image
                    .substringAfter("base64,")
                    .replace("\\s".toRegex(), "")
                    .trim()

                val imageBytes = Base64.decode(cleanBase64, Base64.NO_WRAP)

                Glide.with(context)
                    .asBitmap()
                    .load(imageBytes)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(R.drawable.basket)
                    .into(holder.image)
            } catch (e: Exception) {
                Log.e("ADAPTER_ERROR", "Error for ${product.name}: ${e.message}")
                holder.image.setImageResource(R.drawable.basket)
            }
        } else {
            holder.image.setImageResource(R.drawable.basket)
        }

        // --- Navigation Logic ---
        holder.itemView.setOnClickListener {
            // 2. Create intent to navigate to your Detail Activity
            // (Assuming you have a ProductDetailActivity or similar)

        }
    }
}