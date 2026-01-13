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
                bind.textSellerName.text = p.sellerName.ifBlank { "Unknown Seller" }

                // FOTOĞRAF
                if (p.imageUrl.isNotBlank()) {
                    Glide.with(this)
                        .load(p.imageUrl)
                        .centerCrop()
                        .into(bind.imageProduct)
                }
                
                // Load seller's profile photo
                loadSellerAvatar(p.sellerId, bind)
                
                // Navigate to Seller Profile
                bind.textViewProfile.setOnClickListener {
                     parentFragmentManager.beginTransaction()
                         .replace(R.id.fragment_container, ProfileFragment.newInstance(p.sellerId, p.sellerName))
                         .addToBackStack(null)
                         .commit()
                }

                // Detaylar
                bind.textCategory.text = p.category.ifBlank { "N/A" }
                bind.textFaculty.text = p.faculty.ifBlank { "N/A" }
                bind.textBrand.text = p.brand.ifBlank { "N/A" }
                bind.textProductColor.text = p.color.ifBlank { "N/A" }
                bind.textCondition.text = p.condition.ifBlank { "N/A" }
                try {
                    bind.textDepartment.text = p.department.ifBlank { "N/A" }
                } catch (_: Exception) {
                }

                // ---------- DELETE BUTTON LOGIC ----------
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

                // Sadece ürünü satan kişi görsün
                if (currentUserId != null && p.sellerId == currentUserId) {
                    bind.btnDeleteProduct.visibility = View.VISIBLE

                    bind.btnDeleteProduct.setOnClickListener {
                        android.app.AlertDialog.Builder(requireContext())
                            .setTitle(getString(R.string.dialog_title_delete))
                            .setMessage(getString(R.string.dialog_msg_delete))
                            .setPositiveButton(getString(R.string.dialog_yes)) { _, _ ->
                                deleteProductFromFirebase(p)
                            }
                            .setNegativeButton(getString(R.string.dialog_no), null)
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
                        Toast.makeText(context, getString(R.string.msg_cannot_buy_own), Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val cartViewModel =
                        androidx.lifecycle.ViewModelProvider(this)[com.example.supply7.viewmodel.CartViewModel::class.java]
                    cartViewModel.addToCart(p)
                    Toast.makeText(context, getString(R.string.msg_added_to_cart), Toast.LENGTH_SHORT).show()

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, CartFragment())
                        .addToBackStack(null)
                        .commit()
                }
            }

            // RATE SELLER
            bind.btnExchangeOffer.text = getString(R.string.btn_rate_seller)
            val uidForRating = FirebaseAuth.getInstance().currentUser?.uid
            if (uidForRating != null && uidForRating == product?.sellerId) {
                bind.btnExchangeOffer.visibility = View.GONE
            } else {
                bind.btnExchangeOffer.visibility = View.VISIBLE
            }

            bind.btnExchangeOffer.setOnClickListener {
                product?.let { p ->
                    val dialogView =
                        android.view.LayoutInflater.from(context).inflate(R.layout.dialog_rate_seller, null)
                    val ratingBar = dialogView.findViewById<android.widget.RatingBar>(R.id.ratingBar)
                    val input = dialogView.findViewById<android.widget.EditText>(R.id.inputComment)

                    android.app.AlertDialog.Builder(context)
                        .setTitle("Rate ${p.sellerName}")
                        .setView(dialogView)
                        .setPositiveButton(getString(R.string.btn_publish_item)) { _, _ ->
                            val rating = ratingBar.rating
                            val comment = input.text.toString()
                            val reviewsViewModel =
                                androidx.lifecycle.ViewModelProvider(this)[com.example.supply7.viewmodel.ReviewsViewModel::class.java]
                            reviewsViewModel.submitReview(p.sellerId, rating, comment, "Me")
                            Toast.makeText(context, getString(R.string.msg_review_submitted), Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton(getString(R.string.btn_cancel), null)
                        .show()
                }
            }

            // ✅ MESSAGE OWNER (normal chat)
            bind.btnMessageSeller.setOnClickListener {
                product?.let { p ->
                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                    if (currentUserId != null && p.sellerId == currentUserId) {
                        Toast.makeText(context, getString(R.string.msg_cannot_message_self), Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    parentFragmentManager.beginTransaction()
                        .replace(
                            R.id.fragment_container,
                            ChatFragment.newInstance(
                                chatId = null,
                                otherUserName = p.sellerName,
                                receiverId = p.sellerId,
                                offerAmount = null,
                                productTitle = null
                            )
                        )
                        .addToBackStack(null)
                        .commit()
                }
            }

            // ✅ MAKE OFFER (dialog -> offer message)
            bind.btnMakeOffer.setOnClickListener {
                product?.let { p ->
                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                    if (currentUserId != null && p.sellerId == currentUserId) {
                        Toast.makeText(context, getString(R.string.msg_cannot_buy_own), Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val dialog = android.app.AlertDialog.Builder(context)
                    val input = android.widget.EditText(context)
                    input.inputType =
                        android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                    input.hint = getString(R.string.hint_offer_amount)
                    dialog.setView(input)
                    dialog.setTitle(getString(R.string.dialog_title_offer))
                    dialog.setMessage(getString(R.string.dialog_msg_offer, p.title))

                    dialog.setPositiveButton(getString(R.string.btn_send_offer)) { _, _ ->
                        val amount = input.text.toString().trim()
                        if (amount.isNotBlank()) {
                            parentFragmentManager.beginTransaction()
                                .replace(
                                    R.id.fragment_container,
                                    ChatFragment.newInstance(
                                        chatId = null,
                                        otherUserName = p.sellerName,
                                        receiverId = p.sellerId,
                                        offerAmount = amount,
                                        productTitle = p.title,
                                        productId = p.id,
                                        productImageUrl = p.imageUrl
                                    )
                                )
                                .addToBackStack(null)
                                .commit()
                        }
                    }
                    dialog.setNegativeButton(getString(R.string.btn_cancel), null)
                    dialog.show()
                }
            }

        } catch (e: Exception) {
            Toast.makeText(context, "Error showing details: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    // ------------------ LOAD SELLER AVATAR ------------------
    private fun loadSellerAvatar(sellerId: String, bind: FragmentProductDetailBinding) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(sellerId)
            .get()
            .addOnSuccessListener { doc ->
                val photoUrl = doc.getString("photoUrl")
                if (!photoUrl.isNullOrBlank()) {
                    Glide.with(this)
                        .load(photoUrl)
                        .circleCrop()
                        .into(bind.imgSellerAvatar)
                }
                // If photoUrl is null/blank, the default drawable from XML will remain
            }
            .addOnFailureListener { e ->
                android.util.Log.e("SELLER_AVATAR", "Failed to load seller avatar", e)
                // Keep default avatar on failure
            }
    }

    // ------------------ FIREBASE DELETE ------------------
    private fun deleteProductFromFirebase(p: Product) {
        val db = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()

        db.collection("products")
            .document(p.id)
            .delete()
            .addOnSuccessListener {
                if (p.imageUrl.isNotBlank()) {
                    try {
                        storage.getReferenceFromUrl(p.imageUrl).delete()
                    } catch (_: Exception) {
                    }
                }

                Toast.makeText(requireContext(), "Product deleted", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Delete failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }
}



