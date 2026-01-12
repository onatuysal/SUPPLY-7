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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bind = FragmentProfileBinding.bind(view)
        binding = bind

        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            bind.textUserName.text = currentUser.displayName ?: "User ${currentUser.uid.take(4)}"
            bind.textRating.text = "0.0 ★"
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
        val currentUid = com.example.supply7.data.AuthRepository().currentUser?.uid

        if (currentUid != null) {
            lifecycleScope.launch {
                val result = repo.getUserProducts(currentUid)
                val products: List<Product> = result.getOrNull() ?: emptyList()
                adapter.updateData(products)

                bind.textStatListingCount.text = products.size.toString()
                val soldCount = products.count { it.type == "sold" }
                bind.textStatSalesCount.text = soldCount.toString()
            }
        }

        val reviewsAdapter = ReviewsAdapter()
        val reviewsViewModel =
            androidx.lifecycle.ViewModelProvider(this)[com.example.supply7.viewmodel.ReviewsViewModel::class.java]

        if (currentUid != null) {
            reviewsViewModel.loadReviews(currentUid)
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

        bind.tabMySales.text = "My Sales"
        bind.tabMySales.setOnClickListener {
            val salesAdapter = com.example.supply7.ui.OrdersAdapter { order ->
                Toast.makeText(context, "Order Date: ${java.util.Date(order.timestamp)}", Toast.LENGTH_SHORT).show()
            }

            bind.recyclerProfileItems.adapter = salesAdapter
            bind.recyclerProfileItems.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)

            val uid = com.example.supply7.data.AuthRepository().currentUser?.uid
            if (uid != null) {
                val orderRepo = com.example.supply7.data.OrderRepository()
                lifecycleScope.launch {
                    val result = orderRepo.getUserSales(uid)
                    val sales = result.getOrNull() ?: emptyList()
                    salesAdapter.updateData(sales)
                    bind.textStatSalesCount.text = sales.size.toString()
                }
            }

            bind.tabMySales.setTextColor(resources.getColor(R.color.primary_pink, null))
            bind.tabMyListing.setTextColor(resources.getColor(R.color.text_light_gray, null))
        }
    }

    override fun onResume() {
        super.onResume()
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: return
        binding?.textUserName?.text = user.displayName ?: "User ${user.uid.take(4)}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
