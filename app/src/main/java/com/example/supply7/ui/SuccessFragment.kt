package com.example.supply7.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.supply7.R
import com.example.supply7.databinding.FragmentSuccessBinding

class SuccessFragment : Fragment(R.layout.fragment_success) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bind = FragmentSuccessBinding.bind(view)

        // 1. Get Items
        val items = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("items", ArrayList::class.java) as? ArrayList<com.example.supply7.data.CartItem>
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable("items") as? ArrayList<com.example.supply7.data.CartItem>
        }

        // 2. Populate First Item Details (if available)
        if (!items.isNullOrEmpty()) {
            val firstItem = items[0]
            
            // Basic info we have
            bind.textTitle.text = firstItem.productTitle
            bind.textPrice.text = "${firstItem.price}₺"
            if (firstItem.imageUrl.isNotBlank()) {
                 com.bumptech.glide.Glide.with(this).load(firstItem.imageUrl).into(bind.imgProduct)
            }
            
            // Fetch detailed info
            fetchProductDetails(firstItem.productId, bind)
        }

        // 3. Buttons
        bind.btnBack.setOnClickListener {
             // Go home
             (activity as? MainActivity)?.showBottomNav(true)
             parentFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
             parentFragmentManager.beginTransaction()
                 .replace(R.id.fragment_container, HomeFragment())
                 .commit()
        }
        
        bind.btnCheckOrders.setOnClickListener {
             // Go to Orders
             (activity as? MainActivity)?.showBottomNav(true)
             parentFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
             parentFragmentManager.beginTransaction()
                 .replace(R.id.fragment_container, com.example.supply7.ui.OrdersFragment()) 
                 .addToBackStack(null) // allow back
                 .commit()
        }

        bind.textDone.setOnClickListener {
             // Go home
             (activity as? MainActivity)?.showBottomNav(true)
             parentFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
             parentFragmentManager.beginTransaction()
                 .replace(R.id.fragment_container, HomeFragment())
                 .commit()
        }
    }
    
    private fun fetchProductDetails(productId: String, bind: FragmentSuccessBinding) {
        if (productId.isBlank()) return
        
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("products")
            .document(productId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                     val p = document.toObject(com.example.supply7.data.Product::class.java)
                     if (p != null) {
                         bind.textTitle.text = p.title
                         bind.textPrice.text = "${p.price}₺"
                         bind.textCategory.text = p.category
                         bind.textBrand.text = p.brand
                         bind.textFaculty.text = p.faculty
                         bind.textCondition.text = p.condition
                         // Dimensions? Not in Product class usually, assuming standard or from details if exists.
                     }
                }
            }
    }
}
