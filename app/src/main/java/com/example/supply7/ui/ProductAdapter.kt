package com.example.supply7.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.supply7.R
import com.example.supply7.data.Product

class ProductAdapter(
    private var products: List<Product> = emptyList(),
    private val onItemClick: (Product) -> Unit,
    private val onFavoriteClick: ((Product) -> Unit)? = null
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root: View = view
        val title: TextView = view.findViewById(R.id.textTitle)
        val price: TextView = view.findViewById(R.id.textPrice)
        val btnFavorite: android.widget.ImageView = view.findViewById(R.id.btnFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.title.text = product.title
        holder.price.text = "${product.price} TL"
        
        holder.root.setOnClickListener {
            onItemClick(product)
        }
        
        holder.btnFavorite.setOnClickListener {
            onFavoriteClick?.invoke(product)
        }
    }

    override fun getItemCount() = products.size

    fun updateData(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}
