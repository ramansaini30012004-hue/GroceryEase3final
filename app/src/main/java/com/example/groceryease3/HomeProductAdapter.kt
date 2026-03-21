package com.example.groceryease3



import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

class HomeProductAdapter(
    private val context: Context,
    private val list: ArrayList<HomeProduct>
) : RecyclerView.Adapter<HomeProductAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productImg: ImageView = view.findViewById(R.id.productImage)
        val productName: TextView = view.findViewById(R.id.productName)
        val price: TextView = view.findViewById(R.id.productPrice)
//        val shopName: TextView = view.findViewById(R.id.shopName)
        val shopImg: ImageView = view.findViewById(R.id.productImage)
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

        // 🔥 TEXT SET
        holder.productName.text = item.name
        holder.price.text = "₹${item.price}"
//        holder.shopName.text =
//            if (item.shopName.isNotEmpty()) item.shopName else "Shop"

        // 🔥 PRODUCT IMAGE (Base64 SAFE)
        loadBase64Image(
            base64 = item.image,
            imageView = holder.productImg,
            placeholder = R.drawable.basket
        )

        // 🔥 SHOP IMAGE (Base64 SAFE)
        loadBase64Image(
            base64 = item.shopImage,
            imageView = holder.shopImg,
            placeholder = R.drawable.basket
        )

        // 🔥 BUTTON CLICK
        holder.btnNavigate.setOnClickListener {
            Toast.makeText(context, "${item.name} clicked", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ COMMON IMAGE FUNCTION (NO CRASH)
    private fun loadBase64Image(
        base64: String,
        imageView: ImageView,
        placeholder: Int
    ) {
        try {
            if (base64.isNotEmpty()) {
                val bytes = Base64.decode(base64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                imageView.setImageBitmap(bitmap)
            } else {
                imageView.setImageResource(placeholder)
            }
        } catch (e: Exception) {
            imageView.setImageResource(placeholder)
        }
    }
}