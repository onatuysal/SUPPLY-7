package com.example.supply7.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.supply7.R
import com.example.supply7.data.Product
import com.example.supply7.databinding.FragmentProductDetailBinding

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
            bind.btnBack.setOnClickListener {
                parentFragmentManager.popBackStack()
            }

            product?.let { p ->
                bind.textTitle.text = p.title
                bind.textPrice.text = "₺${p.price}"
                bind.textDescription.text = p.description

                // FOTOĞRAFI YÜKLE
                if (p.imageUrl.isNotBlank()) {
                    Glide.with(this)
                        .load(p.imageUrl)
                        .centerCrop()
                        .into(bind.imageProduct)
                }

                // Details
                bind.textCategory.text = p.category.ifBlank { "N/A" }
                bind.textFaculty.text = p.faculty.ifBlank { "N/A" }
                bind.textBrand.text = p.brand.ifBlank { "N/A" }
                bind.textColor.text = p.color.ifBlank { "N/A" }
                bind.textCondition.text = p.condition.ifBlank { "N/A" }
                try { bind.textDepartment.text = p.department.ifBlank { "N/A" } } catch(e: Exception) {}
            }

            bind.btnMessage.setOnClickListener {
                product?.let { p ->
                    parentFragmentManager.beginTransaction()
                        .replace(
                            R.id.fragment_container,
                            ChatFragment.newInstance(null, p.sellerName, p.sellerId)
                        )
                        .addToBackStack(null)
                        .commit()
                }
            }

            bind.btnBuy.setOnClickListener {
                product?.let { p ->
                    val cartViewModel = androidx.lifecycle.ViewModelProvider(this)[com.example.supply7.viewmodel.CartViewModel::class.java]
                    cartViewModel.addToCart(p)
                    Toast.makeText(context, "Added to Cart!", Toast.LENGTH_SHORT).show()

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, CartFragment())
                        .addToBackStack(null)
                        .commit()
                }
            }

            bind.btnExchangeOffer.text = "Rate Seller"
            bind.btnExchangeOffer.setOnClickListener {
                product?.let { p ->
                    val dialog = android.app.AlertDialog.Builder(context)
                    val layout = android.widget.LinearLayout(context)
                    layout.orientation = android.widget.LinearLayout.VERTICAL
                    layout.setPadding(32, 32, 32, 32)

                    val ratingBar = android.widget.RatingBar(context)
                    ratingBar.numStars = 5
                    ratingBar.stepSize = 1.0f
                    layout.addView(ratingBar)

                    val input = android.widget.EditText(context)
                    input.hint = "Write a review..."
                    layout.addView(input)

                    dialog.setView(layout)
                    dialog.setTitle("Rate ${p.sellerName}")

                    dialog.setPositiveButton("Submit") { _, _ ->
                        val rating = ratingBar.rating
                        val comment = input.text.toString()
                        val reviewsViewModel = androidx.lifecycle.ViewModelProvider(this)[com.example.supply7.viewmodel.ReviewsViewModel::class.java]
                        reviewsViewModel.submitReview(p.sellerId, rating, comment, "Me")
                        Toast.makeText(context, "Review Submitted!", Toast.LENGTH_SHORT).show()
                    }
                    dialog.setNegativeButton("Cancel", null)
                    dialog.show()
                }
            }

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
}

