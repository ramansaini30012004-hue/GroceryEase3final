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

    private var selectedPosition = -1   // 🔥 highlight ke liye

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.catImg)
        val txt: TextView = view.findViewById(R.id.catName)
        val card: View = view   // poora item
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

        // ✅ CLICK LISTENER (DEBUG + WORKING)
        holder.itemView.setOnClickListener {

            Toast.makeText(holder.itemView.context, item.name, Toast.LENGTH_SHORT).show()

            selectedPosition = position
            notifyDataSetChanged()

            onClick(item)  // 🔥 ye fetchProducts call karega
        }

        // ✅ SELECTED UI (green highlight)
        if (position == selectedPosition) {
            holder.card.setBackgroundColor(Color.parseColor("#C8E6C9"))
        } else {
            holder.card.setBackgroundColor(Color.WHITE)
        }
    }
}