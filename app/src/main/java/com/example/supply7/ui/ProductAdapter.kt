package com.example.supply7.ui

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.supply7.R
import com.example.supply7.data.Product

class ProductAdapter(
    private var products: List<Product> = emptyList(),
    private val onItemClick: (Product) -> Unit,
    private val onFavoriteClick: ((Product) -> Unit)? = null
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    // ⭐ Kalp için favori ID set’i
    private var favoriteIds: Set<String> = emptySet()

    fun setFavorites(ids: Set<String>) {
        favoriteIds = ids
        notifyDataSetChanged()
    }

    inner class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val root: View = view
        private val image: ImageView = view.findViewById(R.id.imageProduct)
        private val title: TextView = view.findViewById(R.id.textTitle)
        private val price: TextView = view.findViewById(R.id.textPrice)
        private val btnFavorite: ImageView? = view.findViewById(R.id.btnFavorite)

        fun bind(product: Product) {
            title.text = product.title
            price.text = "${product.price.toInt()} TL"

            if (product.imageUrl.isNotBlank()) {
                Glide.with(itemView)
                    .load(product.imageUrl)
                    .centerCrop()
                    .into(image)
            }

            // ⭐ Bu ürün favori mi?
            val isFavorite = favoriteIds.contains(product.id)

            // Kalbin iç rengi
            btnFavorite?.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    itemView.context,
                    if (isFavorite) R.color.white else R.color.primary_pink
                )
            )

            // Kalbin arka planı (yuvarlak)
            btnFavorite?.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    itemView.context,
                    if (isFavorite) R.color.primary_pink else R.color.off_white
                )
            )

            // Kalp tıklaması → sadece dışarı haber veriyoruz
            btnFavorite?.setOnClickListener {
                onFavoriteClick?.invoke(product)
            }

            // Kart tıklaması → detay sayfası
            root.setOnClickListener {
                onItemClick(product)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount() = products.size

    fun updateData(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}






