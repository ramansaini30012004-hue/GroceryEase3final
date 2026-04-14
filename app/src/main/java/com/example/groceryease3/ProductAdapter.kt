package com.example.groceryease3

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*

class ProductAdapter(
    private val context: Context,
    private var list: ArrayList<Product>,
    private var shopLat: Double,
    private var shopLng: Double
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    // 🔥 CLICK LISTENER (optional)
    private var onItemClick: ((Product) -> Unit)? = null

    fun setOnItemClickListener(listener: (Product) -> Unit) {
        onItemClick = listener
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.productName)
        val price: TextView = view.findViewById(R.id.productPrice)
        val image: ImageView = view.findViewById(R.id.productImage)
        val btnNavigate: Button = view.findViewById(R.id.btnNavigate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_product, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val product = list[position]

        holder.name.text = product.name.ifEmpty { "No Name" }
        holder.price.text = "₹${product.price.ifEmpty { "0" }}"

        // ✅ IMAGE LOAD
        // inside onBindViewHolder
        if (product.image.isNotEmpty()) {
            try {
                // 1. Remove any web-prefixes if they exist
                val cleanBase64 = if (product.image.contains(",")) {
                    product.image.substringAfter(",")
                } else {
                    product.image
                }

                // 2. Convert the string to a byte array
                val imageBytes = Base64.decode(cleanBase64, Base64.DEFAULT)

                // 3. Load the bytes into the view
                Glide.with(context)
                    .asBitmap() // Explicitly tell Glide to treat this as a bitmap
                    .load(imageBytes)
                    .placeholder(R.drawable.bg_circle)
                    .error(R.drawable.bg_circle) // Show placeholder if it fails
                    .into(holder.image)

            } catch (e: Exception) {
                holder.image.setImageResource(R.drawable.bg_circle)
            }
        } else {
            holder.image.setImageResource(R.drawable.bg_circle)
        }

        // 🔥 ITEM CLICK (optional)
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(product)
        }

        // 🚀 NAVIGATE BUTTON (FINAL LOGIC)
        holder.btnNavigate.setOnClickListener {

            val shopId = product.id

            if (shopId.isEmpty()) {
                Toast.makeText(context, "Invalid shop", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔥 STEP 1: loading message
            Toast.makeText(context, "Location finding...", Toast.LENGTH_SHORT).show()

            // 🔥 STEP 2: Firebase se location fetch
            FirebaseDatabase.getInstance().getReference("Users")
                .child(shopId)
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {

                        if (!snapshot.exists()) {
                            Toast.makeText(context, "Shop not found", Toast.LENGTH_SHORT).show()
                            return
                        }

                        val lat = snapshot.child("latitude").getValue(Double::class.java) ?: 0.0
                        val lng = snapshot.child("longitude").getValue(Double::class.java) ?: 0.0

                        if (lat != 0.0 && lng != 0.0) {

                            val uri = Uri.parse("google.navigation:q=$lat,$lng")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            intent.setPackage("com.google.android.apps.maps")

                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Google Maps not installed", Toast.LENGTH_SHORT).show()
                            }

                        } else {
                            Toast.makeText(context, "Location not available", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "Failed to fetch location", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}