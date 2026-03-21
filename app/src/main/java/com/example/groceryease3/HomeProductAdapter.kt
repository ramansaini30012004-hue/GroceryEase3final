package com.example.groceryease3

import android.content.Context
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class HomeProductAdapter(
    private val context: Context,
    private val list: ArrayList<HomeProduct>
) : RecyclerView.Adapter<HomeProductAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productImg: ImageView = view.findViewById(R.id.productImage)
        val productName: TextView = view.findViewById(R.id.productName)
        val price: TextView = view.findViewById(R.id.productPrice)
        // Ensure this ID is correct in your item_home_product.xml
//        val shopImg: ImageView = view.findViewById(R.id.productImage)
        val btnNavigate: Button = view.findViewById(R.id.Navigate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_home_product, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        // 1. Set Text Data
        holder.productName.text = item.name
        holder.price.text = "₹${item.price}"

        // 2. Load Product Image (Base64)
        loadBase64WithGlide(item.image, holder.productImg)

        // 3. Load Shop Image (Base64)
//        loadBase64WithGlide(item.shopImage, holder.shopImg)

        // 4. Button Click Logic
        holder.btnNavigate.setOnClickListener {
            Toast.makeText(context, "Navigating to ${item.name}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * ✅ Optimized Base64 Loader using Glide
     * This handles the cleaning of the string and memory management automatically.
     */
    private fun loadBase64WithGlide(base64String: String?, imageView: ImageView) {
        if (!base64String.isNullOrBlank()) {
            try {
                // Strip metadata and whitespace
                val cleanBase64 = base64String
                    .substringAfter("base64,")
                    .replace("\\s".toRegex(), "")
                    .trim()

                val imageBytes = Base64.decode(cleanBase64, Base64.NO_WRAP)

                Glide.with(context)
                    .asBitmap()
                    .load(imageBytes)
                    .placeholder(R.drawable.basket)
                    .error(R.drawable.basket)
                    .into(imageView)
            } catch (e: Exception) {
                Log.e("HOME_ADAPTER", "Image decode failed: ${e.message}")
                imageView.setImageResource(R.drawable.basket)
            }
        } else {
            imageView.setImageResource(R.drawable.basket)
        }
    }
}