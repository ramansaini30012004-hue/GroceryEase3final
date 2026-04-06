package com.example.groceryease3

import android.graphics.*
import android.util.Base64
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(
    private val list: List<Category>,
    private val onClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    var selectedCategory: String = ""

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.catImg)
        val txt: TextView = view.findViewById(R.id.catName)
        val card: View = view
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = list[position]

        holder.txt.text = item.name

        // 🔥 IMAGE LOGIC (IMPORTANT)
        when {
            // ✅ Base64 Image
            item.image.isNotEmpty() -> {
                try {
                    val bytes = Base64.decode(item.image, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    holder.img.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    holder.img.setImageResource(R.drawable.household)
                }
            }

            // ✅ Drawable Image
            item.imageResId != null -> {
                holder.img.setImageResource(item.imageResId)
            }

            else -> {
                holder.img.setImageResource(R.drawable.household)
            }
        }

        // 🔥 CLICK
        holder.itemView.setOnClickListener {
            selectedCategory = item.name
            notifyDataSetChanged()
            onClick(item)
        }

        // 🎯 SELECTED UI
        if (item.name.equals(selectedCategory, true)) {

            holder.card.setBackgroundColor(Color.parseColor("#1B5E20")) // dark green
            holder.txt.setTextColor(Color.WHITE)

        } else {

            holder.card.setBackgroundColor(Color.WHITE)
            holder.txt.setTextColor(Color.BLACK)
        }
    }
}