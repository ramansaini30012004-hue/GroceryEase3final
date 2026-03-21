package com.example.groceryease3

import android.view.*
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class BannerAdapter(private val list: List<Int>) :
    RecyclerView.Adapter<BannerAdapter.ViewHolder>() {

    class ViewHolder(val image: ImageView) : RecyclerView.ViewHolder(image)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val img = ImageView(parent.context)
        img.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        img.scaleType = ImageView.ScaleType.CENTER_CROP
        return ViewHolder(img)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.image.setImageResource(list[position])
    }

    override fun getItemCount(): Int = list.size
}