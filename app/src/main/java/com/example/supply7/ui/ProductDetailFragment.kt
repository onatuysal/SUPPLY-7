package com.example.supply7.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.supply7.R
import com.example.supply7.data.Product
import com.example.supply7.databinding.FragmentProductDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProductDetailFragment : Fragment(R.layout.fragment_product_detail) {

    private var product: Product? = null

    companion object {
        const val ARG_PRODUCT = "arg_product"

        fun newInstance(product: Product): ProductDetailFragment {
            val fragment = ProductDetailFragment()
            val args = Bundle()
            args.putParcelable(ARG_PRODUCT, product)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            product = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable(ARG_PRODUCT, Product::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.getParcelable(ARG_PRODUCT)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bind = FragmentProductDetailBinding.bind(view)

        try {
            // Back
            bind.btnBack.setOnClickListener {
                parentFragmentManager.popBackStack()
            }

            product?.let { p ->
                // Temel bilgiler
                bind.textTitle.text = p.title
                bind.textPrice.text = "₺${p.price}"
                bind.textDescription.text = p.description

                // FOTOĞRAF
                if (p.imageUrl.isNotBlank()) {
                    Glide.with(this)
                        .load(p.imageUrl)
                        .centerCrop()
                        .into(bind.imageProduct)
                }

                // Detaylar
                bind.textCategory.text = p.category.ifBlank { "N/A" }
                bind.textFaculty.text = p.faculty.ifBlank { "N/A" }
                bind.textBrand.text = p.brand.ifBlank { "N/A" }
                bind.textProductColor.text = p.color.ifBlank { "N/A" }
                bind.textCondition.text = p.condition.ifBlank { "N/A" }
                try {
                    bind.textDepartment.text = p.department.ifBlank { "N/A" }
                } catch (_: Exception) {}

                // ---------- DELETE BUTTON LOGIC ----------
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

                // Sadece ürünü satan kişi görsün
                if (currentUserId != null && p.sellerId == currentUserId) {
                    bind.btnDeleteProduct.visibility = View.VISIBLE

                    bind.btnDeleteProduct.setOnClickListener {
                        android.app.AlertDialog.Builder(requireContext())
                            .setTitle("Delete product")
                            .setMessage("Are you sure you want to delete this product?")
                            .setPositiveButton("Yes") { _, _ ->
                                deleteProductFromFirebase(p)
                            }
                            .setNegativeButton("No", null)
                            .show()
                    }
                } else {
                    bind.btnDeleteProduct.visibility = View.GONE
                }
                // ---------- DELETE BUTTON LOGIC END ----------
            }



            // BUY + CART
            bind.btnBuy.setOnClickListener {
                product?.let { p ->
                     val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                     if (currentUserId != null && p.sellerId == currentUserId) {
                         Toast.makeText(context, "You cannot buy your own product!", Toast.LENGTH_SHORT).show()
                         return@setOnClickListener
                     }
                    
                    val cartViewModel =
                        androidx.lifecycle.ViewModelProvider(this)[com.example.supply7.viewmodel.CartViewModel::class.java]
                    cartViewModel.addToCart(p)
                    Toast.makeText(context, "Added to Cart!", Toast.LENGTH_SHORT).show()

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, CartFragment())
                        .addToBackStack(null)
                        .commit()
                }
            }

            // RATE SELLER
            bind.btnExchangeOffer.text = "Rate Seller"
            bind.btnExchangeOffer.setOnClickListener {
                product?.let { p ->
                    val dialogView = android.view.LayoutInflater.from(context).inflate(R.layout.dialog_rate_seller, null)
                    val ratingBar = dialogView.findViewById<android.widget.RatingBar>(R.id.ratingBar)
                    val input = dialogView.findViewById<android.widget.EditText>(R.id.inputComment)

                    android.app.AlertDialog.Builder(context)
                        .setTitle("Rate ${p.sellerName}")
                        .setView(dialogView)
                        .setPositiveButton("Submit") { _, _ ->
                            val rating = ratingBar.rating
                            val comment = input.text.toString()
                            val reviewsViewModel =
                                androidx.lifecycle.ViewModelProvider(this)[com.example.supply7.viewmodel.ReviewsViewModel::class.java]
                            reviewsViewModel.submitReview(p.sellerId, rating, comment, "Me")
                            Toast.makeText(context, "Review Submitted!", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }

            // PRICE OFFER
            bind.btnMessageSeller.setOnClickListener {
                product?.let { p ->
                    val dialog = android.app.AlertDialog.Builder(context)
                    val input = android.widget.EditText(context)
                    input.inputType =
                        android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                    input.hint = "Offer Amount (TL)"
                    dialog.setView(input)
                    dialog.setTitle("Make an Offer")
                    dialog.setMessage("Enter your price offer for ${p.title}")

                    dialog.setPositiveButton("Send Offer") { _, _ ->
                        val amount = input.text.toString()
                        if (amount.isNotBlank()) {
                            parentFragmentManager.beginTransaction()
                                .replace(
                                    R.id.fragment_container,
                                    ChatFragment.newInstance(
                                        chatId = null,
                                        otherUserName = p.sellerName,
                                        receiverId = p.sellerId,
                                        offerAmount = amount,
                                        productTitle = p.title
                                    )
                                )
                                .addToBackStack(null)
                                .commit()
                        }
                    }
                    dialog.setNegativeButton("Cancel", null)
                    dialog.show()
                }
            }

        } catch (e: Exception) {
            Toast.makeText(context, "Error showing details: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    // ------------------ FIREBASE DELETE ------------------

    private fun deleteProductFromFirebase(p: Product) {
        val db = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()

        // 1) Firestore dokümanı sil
        db.collection("products") // <- koleksiyon adın farklıysa burayı değiştir
            .document(p.id)
            .delete()
            .addOnSuccessListener {
                // 2) Fotoğraf varsa Storage'dan da sil
                if (p.imageUrl.isNotBlank()) {
                    try {
                        storage.getReferenceFromUrl(p.imageUrl)
                            .delete()
                    } catch (_: Exception) {
                        // URL bozuksa app patlamasın
                    }
                }

                Toast.makeText(requireContext(), "Product deleted", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack() // geri dön (Home'a veya önceki ekrana)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Delete failed: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}


