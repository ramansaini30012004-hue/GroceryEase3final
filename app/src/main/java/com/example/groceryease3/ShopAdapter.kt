package com.example.groceryease3

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ShopAdapter(
    private val context: Context,
    private var shopList: MutableList<Shop>
) : RecyclerView.Adapter<ShopAdapter.ShopViewHolder>() {

    class ShopViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvShopName)
        val tvDistance: TextView = view.findViewById(R.id.tvDistance)
        val imgShop: ImageView = view.findViewById(R.id.shopImage)
        val btnNavigate: Button = view.findViewById(R.id.btnNavigate)
        val btnProducts: Button = view.findViewById(R.id.btnProducts)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shop, parent, false)
        return ShopViewHolder(view)
    }

    override fun getItemCount(): Int = shopList.size

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {

        val shop = shopList[position]

        holder.tvName.text = shop.shopName
        holder.tvDistance.text = shop.distanceText

        Glide.with(context)
            .load(shop.image)
            .placeholder(R.drawable.basket)
            .into(holder.imgShop)

        // Navigate
        holder.btnNavigate.setOnClickListener {

            val loc = shop.location

            if (loc != null) {
                val uri = Uri.parse("google.navigation:q=${loc.lat},${loc.lng}")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setPackage("com.google.android.apps.maps")
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Location not available", Toast.LENGTH_SHORT).show()
            }
        }

        // Products
        holder.btnProducts.setOnClickListener {

            val intent = Intent(context, ProductActivity::class.java)
            intent.putExtra("shopId", shop.id)      // 🔥 correct UID
            intent.putExtra("shopName", shop.shopName)

            context.startActivity(intent)
        }
    }
}