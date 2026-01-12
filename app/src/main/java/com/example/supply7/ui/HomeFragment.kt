package com.example.supply7.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.supply7.R
import com.example.supply7.databinding.FragmentHomeBinding
import com.example.supply7.viewmodel.FavoritesViewModel
import com.example.supply7.viewmodel.HomeViewModel
import java.util.Timer
import java.util.TimerTask

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val homeViewModel: HomeViewModel by viewModels()
    private val favoritesViewModel: FavoritesViewModel by activityViewModels()

    private var binding: FragmentHomeBinding? = null
    private lateinit var adapter: ProductAdapter
    private var searchTimer: Timer? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.showBottomNav(true)

        val bind = FragmentHomeBinding.bind(view)
        binding = bind

        // ⚠️ BUNU ARTIK ÇAĞIRMIYORUZ:
        // favoritesViewModel.loadFavorites()

        // Adapter
        adapter = ProductAdapter(
            onItemClick = { product ->
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ProductDetailFragment.newInstance(product))
                    .addToBackStack(null)
                    .commit()
            },
            onFavoriteClick = { product ->
                Toast.makeText(requireContext(), getString(R.string.msg_favorite_clicked, product.title), Toast.LENGTH_SHORT).show()
                favoritesViewModel.toggleFavorite(product)
            }
        )

        bind.recyclerViewProducts.layoutManager = GridLayoutManager(context, 2)
        bind.recyclerViewProducts.adapter = adapter

        // Search bar
        bind.editSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchTimer?.cancel()
                val query = s?.toString() ?: ""
                searchTimer = Timer()
                searchTimer?.schedule(object : TimerTask() {
                    override fun run() {
                        activity?.runOnUiThread {
                            homeViewModel.searchProducts(query = query)
                        }
                    }
                }, 500)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Butonlar
        bind.btnCart.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CartFragment())
                .addToBackStack(null)
                .commit()
        }

        bind.btnNotification.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, NotificationsFragment())
                .addToBackStack(null)
                .commit()
        }

        bind.btnFilter.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FilterFragment())
                .addToBackStack(null)
                .commit()
        }

        parentFragmentManager.setFragmentResultListener(
            "requestKeyFilters",
            viewLifecycleOwner
        ) { _, bundle ->
            val minPrice = bundle.getDouble("minPrice")
            val maxPrice = bundle.getDouble("maxPrice")
            val condition = bundle.getString("condition")
            val department = bundle.getString("department")
            val brand = bundle.getString("brand")
            val city = bundle.getString("city")

            val query = bind.editSearch.text.toString()
            homeViewModel.searchProducts(
                query = query,
                minPrice = minPrice,
                maxPrice = maxPrice,
                condition = condition,
                department = department,
                brand = brand,
                city = city
            )
        }

        // Ürün listesi
        homeViewModel.products.observe(viewLifecycleOwner) { products ->
            adapter.updateData(products)
            bind.textEmpty.visibility = if (products.isEmpty()) View.VISIBLE else View.GONE

            // Favori ekranı için tüm ürünleri bildir - ARTIK GEREK YOK
            // favoritesViewModel.setAllProducts(products)
        }

        homeViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        homeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            bind.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Kalplerin dolu/boş halini güncelle
        favoritesViewModel.favoriteIds.observe(viewLifecycleOwner) { ids ->
            adapter.setFavorites(ids)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchTimer?.cancel()
        binding = null
    }
}






