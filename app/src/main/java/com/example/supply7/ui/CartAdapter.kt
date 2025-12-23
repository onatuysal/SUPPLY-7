package com.example.supply7.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.supply7.R
import com.example.supply7.data.CartItem

class CartAdapter(
    private var items: List<CartItem> = emptyList(),
    private val onDeleteClick: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.textTitle)
        val price: TextView = view.findViewById(R.id.textPrice)
        val btnDelete: ImageView = view.findViewById(R.id.btnDelete)
        // val image: ImageView = view.findViewById(R.id.imageProduct) // Pending image load lib
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.productTitle
        holder.price.text = "${item.price} TL"
        
        holder.btnDelete.setOnClickListener {
            onDeleteClick(item)
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<CartItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
