package com.example.groceryease3

import android.graphics.Color
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

data class Category(val name: String, val image: Int)

class CategoryAdapter(
    private val list: List<Category>,
    private val onClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    // 🔥 CHANGE: position hata ke category name use karenge
    var selectedCategory: String = "" // default first

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

        holder.img.setImageResource(item.image)
        holder.txt.text = item.name

        // 🔥 CLICK
        holder.itemView.setOnClickListener {

            selectedCategory = item.name   // 🔥 update by name
            notifyDataSetChanged()

            onClick(item)
        }

        // 🎯 SELECTED UI (NAME BASED)
        if (item.name.equals(selectedCategory, true)) {

            // ✅ GREEN
            holder.card.setBackgroundColor(Color.parseColor("#1B5E20"))
            holder.txt.setTextColor(Color.WHITE)

        } else {

            // ❌ NORMAL
            holder.card.setBackgroundColor(Color.WHITE)
            holder.txt.setTextColor(Color.BLACK)
        }
    }
}