package com.example.groceryease3

import android.content.Context
import android.content.Intent
import android.location.Location as AndroidLocation
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.util.Locale

class ShopAdapter(
    private val context: Context,
    private var shopList: MutableList<Shop>,
    private var userLat: Double? = null,
    private var userLng: Double? = null
) : RecyclerView.Adapter<ShopAdapter.ShopViewHolder>() {

    class ShopViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvShopName)
        val tvDistance: TextView = view.findViewById(R.id.tvDistance)
        val imgShop: ImageView = view.findViewById(R.id.shopImage)
        val btnNavigate: Button = view.findViewById(R.id.btnNavigate)
        val btnProducts: Button = view.findViewById(R.id.btnProducts)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shop, parent, false)
        return ShopViewHolder(view)
    }

    override fun getItemCount(): Int = shopList.size

    // Helper to update location from Fragment
    fun updateLocation(lat: Double, lng: Double) {
        this.userLat = lat
        this.userLng = lng
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        val shop = shopList[position]
        holder.tvName.text = shop.shopName

        // --- Real-time Distance Calculation ---
        if (userLat != null && userLng != null && shop.latitude != 0.0) {
            val results = FloatArray(1)
            AndroidLocation.distanceBetween(userLat!!, userLng!!, shop.latitude, shop.longitude, results)
            val meters = results[0]

            holder.tvDistance.text = if (meters < 1000) {
                "${meters.toInt()} m"
            } else {
                String.format(Locale.getDefault(), "%.1f km", meters / 1000)
            }
        } else {
            holder.tvDistance.text = "Calculating..."
        }

        // --- Image Handling ---
        if (!shop.image.isNullOrBlank()) {
            try {
                val cleanBase64 = shop.image.substringAfter("base64,").replace("\\s".toRegex(), "").trim()
                val imageBytes = Base64.decode(cleanBase64, Base64.NO_WRAP)
                Glide.with(context).asBitmap().load(imageBytes).placeholder(R.drawable.basket).error(R.drawable.basket).into(holder.imgShop)
            } catch (e: Exception) {
                holder.imgShop.setImageResource(R.drawable.basket)
            }
        } else {
            holder.imgShop.setImageResource(R.drawable.basket)
        }

        holder.btnNavigate.setOnClickListener {
            val uri = Uri.parse("google.navigation:q=${shop.latitude},${shop.longitude}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") }
            context.startActivity(intent)
        }

        holder.btnProducts.setOnClickListener {
            val intent = Intent(context, ProductActivity::class.java).apply {
                putExtra("shopId", shop.id)
                putExtra("shopName", shop.shopName)
                putExtra("shopAddress", shop.address)
                putExtra("shopImage", shop.image)
            }
            context.startActivity(intent)
        }
    }
}