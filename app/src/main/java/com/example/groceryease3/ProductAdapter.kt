package com.example.groceryease3

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ProductAdapter(
    private val context: Context,
    private var list: ArrayList<Product>,
    private var shopLat: Double,
    private var shopLng: Double
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    // 🔥 CLICK LISTENER
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

    // 🔥 UPDATE LOCATION
    fun updateLocation(lat: Double, lng: Double) {
        this.shopLat = lat
        this.shopLng = lng
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val product = list[position]

        holder.name.text = product.name.ifEmpty { "No Name" }
        holder.price.text = "₹${product.price.ifEmpty { "0" }}"

        // ✅ IMAGE
        if (product.image.isNotEmpty()) {
            try {
                val base64 = product.image.substringAfter("base64,", product.image)
                val bytes = Base64.decode(base64, Base64.DEFAULT)

                Glide.with(context)
                    .load(bytes)
                    .placeholder(R.drawable.bg_circle)
                    .into(holder.image)

            } catch (e: Exception) {
                holder.image.setImageResource(R.drawable.bg_circle)
            }
        } else {
            holder.image.setImageResource(R.drawable.bg_circle)
        }

        // 🔥 ITEM CLICK (IMPORTANT)
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(product)
        }

        // ✅ NAVIGATION BUTTON
        holder.btnNavigate.setOnClickListener {

            if (shopLat != 0.0 && shopLng != 0.0) {

                val uri = Uri.parse("google.navigation:q=$shopLat,$shopLng")
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
    }
}

