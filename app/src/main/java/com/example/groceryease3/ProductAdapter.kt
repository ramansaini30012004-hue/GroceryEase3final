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

class ProductAdapter(
    private val context: Context,
    private var list: ArrayList<Product> // Changed to var to allow list updates
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

    // Helper function to update the list when filtering categories
    fun updateList(newList: List<Product>) {
        list = ArrayList(newList)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = list[position]

        holder.name.text = product.name
        holder.price.text = "₹${product.price} / ${product.qty} ${product.unit}"

        // 🔴 DEBUG LOG: See the first 50 characters of the image string in Logcat
        Log.d("IMAGE_DEBUG", "Product: ${product.name} | Base64 Length: ${product.image.length} | Start: ${product.image.take(30)}")

        if (!product.image.isNullOrBlank()) {
            try {
                // 1. Thoroughly clean the string
                val cleanBase64 = product.image
                    .substringAfter("base64,") // Remove data:image/jpeg;base64, if present
                    .replace("\\s".toRegex(), "") // Remove all whitespace, \n, \r
                    .trim()

                // 2. Use NO_WRAP which is usually best for strings from web/Firebase
                val imageBytes = Base64.decode(cleanBase64, Base64.NO_WRAP)

                if (imageBytes != null && imageBytes.isNotEmpty()) {
                    Glide.with(context)
                        .asBitmap()
                        .load(imageBytes)
                        // Use a generic placeholder so you can tell if it's "loading" vs "failed"
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(R.drawable.basket) // Your custom error image
                        .into(holder.image)
                } else {
                    holder.image.setImageResource(R.drawable.basket)
                }

            } catch (e: IllegalArgumentException) {
                Log.e("ADAPTER_ERROR", "Illegal Base64 characters in ${product.name}: ${e.message}")
                holder.image.setImageResource(R.drawable.basket)
            } catch (e: Exception) {
                Log.e("ADAPTER_ERROR", "General error for ${product.name}: ${e.message}")
                holder.image.setImageResource(R.drawable.basket)
            }
        } else {
            // No image data at all
            holder.image.setImageResource(R.drawable.basket)
        }

        holder.itemView.setOnClickListener {
            Toast.makeText(context, "${product.name} clicked", Toast.LENGTH_SHORT).show()
        }
    }
}