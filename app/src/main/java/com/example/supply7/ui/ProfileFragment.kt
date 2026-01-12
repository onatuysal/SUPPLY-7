package com.example.supply7.ui

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.supply7.R
import com.example.supply7.data.Product
import com.example.supply7.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var binding: FragmentProfileBinding? = null
    private var selectedAvatarUri: Uri? = null

    private val pickAvatar =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedAvatarUri = uri
                binding?.imageAvatar?.setImageURI(uri)
                uploadAvatarToFirebase(uri)
            }
        }

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

        val currentAuthUser = FirebaseAuth.getInstance().currentUser
        val argUserId = arguments?.getString(ARG_USER_ID)
        val argUserName = arguments?.getString(ARG_USER_NAME)

        val effectiveUid = argUserId ?: currentAuthUser?.uid
        val isMe = (currentAuthUser != null && effectiveUid == currentAuthUser.uid)

        val displayName = argUserName ?: currentAuthUser?.displayName ?: "User"
        bind.textUserName.text = displayName
        bind.textRating.text = "0.0 ★"

        if (!isMe) {
            bind.btnSettings.visibility = View.GONE
            bind.btnEditProfile.visibility = View.GONE
            bind.tabMyOrders.visibility = View.GONE
            // bind.textStatSalesCount.visibility = View.VISIBLE // Default
            bind.textProfileTitle.text = getString(R.string.profile_title_generic)
            bind.tabMyListing.text = getString(R.string.tab_listings_generic)
            bind.tabMySales.text = getString(R.string.tab_sales_generic)
        } else {
            bind.btnSettings.visibility = View.VISIBLE
            bind.btnEditProfile.visibility = View.VISIBLE
            bind.tabMyOrders.visibility = View.VISIBLE
            bind.textProfileTitle.text = getString(R.string.profile_title_header)
            bind.tabMyListing.text = getString(R.string.tab_my_listings)
            bind.tabMySales.text = getString(R.string.tab_my_sales)
        }

        bind.cardAvatar.setOnClickListener {
            if (isMe) {
                pickAvatar.launch("image/*")
            } else {
                Toast.makeText(context, "You can only change your own profile photo.", Toast.LENGTH_SHORT).show()
            }
        }

        if (effectiveUid != null) {
            loadAvatarFromFirestore(effectiveUid, bind)
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
                val activeProducts = allProducts.filter { it.stock > 0 }
                adapter.updateData(activeProducts)
                bind.textStatListingCount.text = activeProducts.size.toString()
                // Fetch actual sales count for everyone
                val orderRepo = com.example.supply7.data.OrderRepository()
                val orderResult = orderRepo.getUserSales(effectiveUid)
                val salesCount = orderResult.getOrNull()?.size ?: 0
                bind.textStatSalesCount.text = salesCount.toString()
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

    private fun uploadAvatarToFirebase(uri: Uri) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val storage = FirebaseStorage.getInstance("gs://supply-7.firebasestorage.app")
        val ref = storage.reference.child("profile_images/$uid.jpg")

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUrl ->
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(uid)
                        .set(mapOf("photoUrl" to downloadUrl.toString()), SetOptions.merge())
                }
            }
            .addOnFailureListener { e ->
                Log.e("AVATAR_UPLOAD", "Upload failed", e)
                Toast.makeText(context, e.message ?: "Upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadAvatarFromFirestore(uid: String, bind: FragmentProfileBinding) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val url = doc.getString("photoUrl")
                if (!url.isNullOrBlank()) {
                    Glide.with(this).load(url).into(bind.imageAvatar)
                }
            }
            .addOnFailureListener { e ->
                Log.e("AVATAR_LOAD", "Load failed", e)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
