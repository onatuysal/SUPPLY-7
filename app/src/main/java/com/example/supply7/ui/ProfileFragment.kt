package com.example.supply7.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.supply7.R

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.supply7.databinding.FragmentProfileBinding
import com.example.supply7.data.Product
import com.example.supply7.ui.ProductAdapter
import com.example.supply7.viewmodel.ReviewsViewModel

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var binding: FragmentProfileBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bind = FragmentProfileBinding.bind(view)
        binding = bind

        // Load Real User Data
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            bind.textUserName.text = currentUser.displayName ?: "User ${currentUser.uid.take(4)}"
            bind.textRating.text = "0.0 ★" // Ratings are separate, mocked for now
        }
        
        bind.tabMyOrders.setOnClickListener {
             parentFragmentManager.beginTransaction()
                 .replace(R.id.fragment_container, OrdersFragment())
                 .addToBackStack(null)
                 .commit()
        }
        
        bind.btnBack.setOnClickListener {
             // If we are at root (BottomNav), maybe this should go to Home or just pop
             if (parentFragmentManager.backStackEntryCount > 0) {
                 parentFragmentManager.popBackStack()
             } else {
                 // Fallback to Home if no stack
                 activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)?.selectedItemId = R.id.nav_home
             }
        }

        bind.btnSettings.setOnClickListener {
             parentFragmentManager.beginTransaction()
                 .replace(R.id.fragment_container, SettingsFragment())
                 .addToBackStack(null)
                 .commit()
        }

        bind.btnEditProfile.setOnClickListener {
             val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: return@setOnClickListener
             
             val dialog = android.app.AlertDialog.Builder(context)
             val input = android.widget.EditText(context)
             input.setText(user.displayName)
             input.hint = "Enter full name"
             
             dialog.setView(input)
             dialog.setTitle("Edit Profile")
             
             dialog.setPositiveButton("Save") { _, _ ->
                 val newName = input.text.toString()
                 if (newName.isNotBlank()) {
                     val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                         .setDisplayName(newName)
                         .build()

                     user.updateProfile(profileUpdates)
                         .addOnCompleteListener { task ->
                             if (task.isSuccessful) {
                                 bind.textUserName.text = newName
                                 Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show()
                             } else {
                                 Toast.makeText(context, "Update Failed", Toast.LENGTH_SHORT).show()
                             }
                         }
                 }
             }
             dialog.setNegativeButton("Cancel", null)
             dialog.show()
        }

        // Setup Recycler for "My Listing"
        // Initially empty
        val adapter = ProductAdapter(
            products = emptyList(),
            onItemClick = { product ->
                // Navigate to Detail on click (checking if edit needed later)
                 parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProductDetailFragment.newInstance(product))
                .addToBackStack(null)
                .commit()
            }
        )
        bind.recyclerProfileItems.layoutManager = GridLayoutManager(context, 2)
        bind.recyclerProfileItems.adapter = adapter

        // Fetch User Products
        val repo = com.example.supply7.data.ProductRepository()
        val currentUid = com.example.supply7.data.AuthRepository().currentUser?.uid
        
        if (currentUid != null) {
            lifecycleScope.launch {
                val result = repo.getUserProducts(currentUid)
                val products: List<Product> = result.getOrNull() ?: emptyList()
                adapter.updateData(products)
                
                // Update Listing Count
                bind.textStatListingCount.text = products.size.toString()
                // Calculate Sales Count (Products marked as sold)
                val soldCount = products.count { it.type == "sold" }
                bind.textStatSalesCount.text = soldCount.toString()
            }
        }

        // Reviews Adapter
        val reviewsAdapter = ReviewsAdapter()
        val reviewsViewModel = androidx.lifecycle.ViewModelProvider(this)[com.example.supply7.viewmodel.ReviewsViewModel::class.java]

        if (currentUid != null) {
            reviewsViewModel.loadReviews(currentUid)
        }

        reviewsViewModel.reviews.observe(viewLifecycleOwner) { reviewList ->
             // Update Reviews Adapter if active
             if (bind.recyclerProfileItems.adapter is ReviewsAdapter) {
                 reviewsAdapter.updateData(reviewList)
             }
             
             // Update Comment Count
             bind.textStatCommentsCount.text = reviewList.size.toString()

             // Calculate Average Rating
             if (reviewList.isNotEmpty()) {
                 val average = reviewList.map { it.rating }.average()
                 bind.textRating.text = String.format("%.1f ★", average)
             } else {
                 bind.textRating.text = "0.0 ★"
             }
        }

        // Tab Logic
        bind.tabMyListing.setOnClickListener {
             bind.recyclerProfileItems.adapter = adapter
             bind.recyclerProfileItems.layoutManager = GridLayoutManager(context, 2)
             bind.tabMyListing.setTextColor(resources.getColor(R.color.primary_pink, null))
             bind.tabMySales.setTextColor(resources.getColor(R.color.text_light_gray, null)) 
        }
        
        bind.tabMySales.text = getString(R.string.label_reviews)
        bind.tabMySales.setOnClickListener {
             bind.recyclerProfileItems.adapter = reviewsAdapter
             bind.recyclerProfileItems.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
             
             val uid = com.example.supply7.data.AuthRepository().currentUser?.uid
             if (uid != null) reviewsViewModel.loadReviews(uid)
             
             bind.tabMySales.setTextColor(resources.getColor(R.color.primary_pink, null))
             bind.tabMyListing.setTextColor(resources.getColor(R.color.text_light_gray, null))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
