package com.example.supply7.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.supply7.R
import com.example.supply7.data.Product
import com.example.supply7.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var binding: FragmentProfileBinding? = null

    companion object {
        private const val ARG_USER_ID = "arg_user_id"
        private const val ARG_USER_NAME = "arg_user_name"

        fun newInstance(userId: String, userName: String?): ProfileFragment {
            val fragment = ProfileFragment()
            val args = Bundle()
            args.putString(ARG_USER_ID, userId)
            args.putString(ARG_USER_NAME, userName)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bind = FragmentProfileBinding.bind(view)
        binding = bind

        val currentAuthUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val argUserId = arguments?.getString(ARG_USER_ID)
        val argUserName = arguments?.getString(ARG_USER_NAME)

        // Determine effective user
        val effectiveUid = argUserId ?: currentAuthUser?.uid
        val isMe = (currentAuthUser != null && effectiveUid == currentAuthUser.uid)

        // UI Setup
        val displayName = argUserName ?: currentAuthUser?.displayName ?: "User"
        bind.textUserName.text = displayName
        bind.textRating.text = "0.0 ★"

        // Visibility & Text Logic
        if (!isMe) {
            bind.btnSettings.visibility = View.GONE
            bind.btnEditProfile.visibility = View.GONE
            bind.tabMyOrders.visibility = View.GONE
            bind.textStatSalesCount.visibility = View.GONE 
            
            // Set generic text for other users
            bind.textProfileTitle.text = getString(R.string.profile_title_generic)
            bind.tabMyListing.text = getString(R.string.tab_listings_generic)
            bind.tabMySales.text = getString(R.string.tab_sales_generic)
        } else {
            bind.btnSettings.visibility = View.VISIBLE
            bind.btnEditProfile.visibility = View.VISIBLE
            bind.tabMyOrders.visibility = View.VISIBLE
            
            // Set possessive text for my profile
            bind.textProfileTitle.text = getString(R.string.profile_title_header)
            bind.tabMyListing.text = getString(R.string.tab_my_listings)
            bind.tabMySales.text = getString(R.string.tab_my_sales)
        }

        bind.tabMyOrders.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, OrdersFragment())
                .addToBackStack(null)
                .commit()
        }

        bind.btnBack.setOnClickListener {
            if (parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStack()
            } else {
                activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
                    ?.selectedItemId = R.id.nav_home
            }
        }

        bind.btnSettings.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingsFragment())
                .addToBackStack(null)
                .commit()
        }

        bind.btnEditProfile.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EditProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        val adapter = ProductAdapter(
            products = emptyList(),
            onItemClick = { product ->
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ProductDetailFragment.newInstance(product))
                    .addToBackStack(null)
                    .commit()
            }
        )

        bind.recyclerProfileItems.layoutManager = GridLayoutManager(context, 2)
        bind.recyclerProfileItems.adapter = adapter

        val repo = com.example.supply7.data.ProductRepository()
        
        if (effectiveUid != null) {
            lifecycleScope.launch {
                val result = repo.getUserProducts(effectiveUid)
                val allProducts: List<Product> = result.getOrNull() ?: emptyList()
                
                // Show only active listings (Stock > 0)
                val activeProducts = allProducts.filter { it.stock > 0 }
                adapter.updateData(activeProducts)

                bind.textStatListingCount.text = activeProducts.size.toString()
                
                // Sales count based on stock 0 (or orders)
                // Note: This relies on fetching all products. 
                // If we filter, we might miss sold ones if repository filtered.
                // But ProductRepository.getUserProducts currently returns ALL (I didn't modify it, I modified SEARCH).
                // Wait, I only modified searchProducts and getProducts. getUserProducts relies on whereEqualTo("sellerId", ...).
                // So it returns ALL.
                val soldCount = allProducts.count { it.stock == 0 } 
                
                if (isMe) {
                    bind.textStatSalesCount.text = soldCount.toString()
                }
            }
        }

        val reviewsAdapter = ReviewsAdapter()
        val reviewsViewModel =
            androidx.lifecycle.ViewModelProvider(this)[com.example.supply7.viewmodel.ReviewsViewModel::class.java]

        if (effectiveUid != null) {
            reviewsViewModel.loadReviews(effectiveUid)
        }

        reviewsViewModel.reviews.observe(viewLifecycleOwner) { reviewList ->
            if (bind.recyclerProfileItems.adapter is ReviewsAdapter) {
                reviewsAdapter.updateData(reviewList)
            }

            bind.textStatCommentsCount.text = reviewList.size.toString()

            if (reviewList.isNotEmpty()) {
                val average = reviewList.map { it.rating }.average()
                bind.textRating.text = String.format("%.1f ★", average)
            } else {
                bind.textRating.text = "0.0 ★"
            }
        }

        bind.tabMyListing.setOnClickListener {
            bind.recyclerProfileItems.adapter = adapter
            bind.recyclerProfileItems.layoutManager = GridLayoutManager(context, 2)
            bind.tabMyListing.setTextColor(
                androidx.core.content.ContextCompat.getColor(requireContext(), R.color.primary_pink)
            )
            bind.tabMySales.setTextColor(
                androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_light_gray)
            )
        }


        bind.tabMySales.setOnClickListener {
            val salesAdapter = com.example.supply7.ui.OrdersAdapter { order ->
                Toast.makeText(context, "Order Date: ${java.util.Date(order.timestamp)}", Toast.LENGTH_SHORT).show()
            }

            bind.recyclerProfileItems.adapter = salesAdapter
            bind.recyclerProfileItems.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)

            if (effectiveUid != null) {
                val orderRepo = com.example.supply7.data.OrderRepository()
                lifecycleScope.launch {
                    val result = orderRepo.getUserSales(effectiveUid)
                    val sales = result.getOrNull() ?: emptyList()
                    salesAdapter.updateData(sales)
                    bind.textStatSalesCount.text = sales.size.toString()
                }
            }

            bind.tabMySales.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.primary_pink))
            bind.tabMyListing.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_light_gray))
        }
    }

    override fun onResume() {
        super.onResume()
        // If viewing my own profile, update name?
        // Actually we handled name in onViewCreated
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
