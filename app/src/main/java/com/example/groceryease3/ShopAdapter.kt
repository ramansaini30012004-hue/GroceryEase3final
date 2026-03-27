package com.example.groceryease3

import android.content.Context
import android.content.Intent
import android.location.Location as AndroidLocation
import android.net.Uri
import android.util.Base64
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class ShopAdapter(
    private val context: Context,
    private var shopList: MutableList<Shop>,
    private var userLat: Double? = null,
    private var userLng: Double? = null
) : RecyclerView.Adapter<ShopAdapter.ShopViewHolder>() {

    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    class ShopViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvShopName)
        val tvDistance: TextView = view.findViewById(R.id.tvDistance)
        val imgShop: ImageView = view.findViewById(R.id.shopImage)
        val btnNavigate: Button = view.findViewById(R.id.btnNavigate)
        val btnProducts: Button = view.findViewById(R.id.btnProducts)
        val btnFav: ImageView = view.findViewById(R.id.btnFav) // ❤️
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shop, parent, false)
        return ShopViewHolder(view)
    }

    override fun getItemCount(): Int = shopList.size

    fun updateLocation(lat: Double, lng: Double) {
        userLat = lat
        userLng = lng
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        val shop = shopList[position]

        holder.tvName.text = shop.shopName

        // 📍 Distance
        if (userLat != null && userLng != null && shop.latitude != 0.0) {
            val result = FloatArray(1)
            AndroidLocation.distanceBetween(
                userLat!!, userLng!!,
                shop.latitude, shop.longitude,
                result
            )

            val meters = result[0]
            holder.tvDistance.text =
                if (meters < 1000) "${meters.toInt()} m"
                else String.format(Locale.getDefault(), "%.1f km", meters / 1000)

        } else {
            holder.tvDistance.text = "Calculating..."
        }

        // 🖼 Image
        if (!shop.image.isNullOrBlank()) {
            try {
                val bytes = Base64.decode(
                    shop.image.substringAfter("base64,"),
                    Base64.DEFAULT
                )
                Glide.with(context).load(bytes).into(holder.imgShop)
            } catch (e: Exception) {
                holder.imgShop.setImageResource(R.drawable.basket)
            }
        } else {
            holder.imgShop.setImageResource(R.drawable.basket)
        }

        // ❤️ SET HEART STATE (VERY IMPORTANT)
        if (shop.isFavorite) {
            holder.btnFav.setImageResource(R.drawable.ic_heart_filled)
        } else {
            holder.btnFav.setImageResource(R.drawable.ic_heart_outline)
        }

        // ❤️ HEART CLICK
        holder.btnFav.setOnClickListener {

            shop.isFavorite = !shop.isFavorite

            if (shop.isFavorite) {
                holder.btnFav.setImageResource(R.drawable.ic_heart_filled)

                // 🔥 SAVE TO FIREBASE
                userId?.let {
                    FirebaseDatabase.getInstance().reference
                        .child("Favorites")
                        .child(it)
                        .child(shop.id)
                        .setValue(shop)
                }

                Toast.makeText(context, "Added to Favorites ❤️", Toast.LENGTH_SHORT).show()

            } else {
                holder.btnFav.setImageResource(R.drawable.ic_heart_outline)

                // ❌ REMOVE FROM FIREBASE
                userId?.let {
                    FirebaseDatabase.getInstance().reference
                        .child("Favorites")
                        .child(it)
                        .child(shop.id)
                        .removeValue()
                }

                Toast.makeText(context, "Removed from Favorites ❌", Toast.LENGTH_SHORT).show()
            }
        }

        // 🧭 NAVIGATE BUTTON
        holder.btnNavigate.setOnClickListener {
            if (shop.latitude != 0.0 && shop.longitude != 0.0) {
                val uri = Uri.parse("google.navigation:q=${shop.latitude},${shop.longitude}")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setPackage("com.google.android.apps.maps")
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Location not available", Toast.LENGTH_SHORT).show()
            }
        }

        // 📦 PRODUCTS BUTTON
        holder.btnProducts.setOnClickListener {
            val intent = Intent(context, ProductActivity::class.java)
            intent.putExtra("shopId", shop.id)
            intent.putExtra("shopName", shop.shopName)
            intent.putExtra("shopAddress", shop.address)
            intent.putExtra("shopImage", shop.image)
            intent.putExtra("shopLat", shop.latitude)
            intent.putExtra("shopLng", shop.longitude)

            context.startActivity(intent)
        }
    }
}