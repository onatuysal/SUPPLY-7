package com.example.supply7.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.supply7.R
import com.example.supply7.data.Product

class ProductAdapter(
    private var products: List<Product> = emptyList(),
    private val onItemClick: (Product) -> Unit,
    private val onFavoriteClick: ((Product) -> Unit)? = null
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root: View = view
        private val image: ImageView = view.findViewById(R.id.imageProduct)   // 📷 karttaki resim
        private val title: TextView = view.findViewById(R.id.textTitle)
        private val price: TextView = view.findViewById(R.id.textPrice)
        private val btnFavorite: ImageView? = view.findViewById(R.id.btnFavorite)

        fun bind(product: Product) {
            // Başlık & fiyat
            title.text = product.title
            // 400.0 TL yerine 400 TL gösterelim
            price.text = "${product.price.toInt()} TL"

            // Resim varsa Firebase Storage URL'den yükle
            if (product.imageUrl.isNotBlank()) {
                Glide.with(itemView)
                    .load(product.imageUrl)
                    .centerCrop()
                    .into(image)
            } else {
                // Resim yoksa karttaki default + ikonu kalsın istiyorsan, hiçbir şey yapma
                // image.setImageResource(R.drawable.ic_placeholder) // istersen placeholder
            }

            // Kart tıklanınca detay sayfasına git
            root.setOnClickListener {
                onItemClick(product)
            }

            // Favori butonu (varsa)
            btnFavorite?.setOnClickListener {
                onFavoriteClick?.invoke(product)
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

