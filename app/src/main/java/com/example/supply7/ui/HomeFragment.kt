package com.example.supply7.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.supply7.R
import com.example.supply7.databinding.FragmentHomeBinding
import com.example.supply7.viewmodel.HomeViewModel

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: HomeViewModel by viewModels()
    private var binding: FragmentHomeBinding? = null
    private val adapter = ProductAdapter(
        onItemClick = { product ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProductDetailFragment.newInstance(product))
                .addToBackStack(null)
                .commit()
        },
        onFavoriteClick = { product ->
            // Optional: Implement toggle from Home screen too
            val favViewModel = androidx.lifecycle.ViewModelProvider(this)[com.example.supply7.viewmodel.FavoritesViewModel::class.java]
            favViewModel.toggleFavorite(product)
            Toast.makeText(context, "Added to Favorites", Toast.LENGTH_SHORT).show()
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Show Bottom Nav when on Home
        (activity as? MainActivity)?.showBottomNav(true)
        
        val bind = FragmentHomeBinding.bind(view)
        binding = bind

        bind.recyclerViewProducts.layoutManager = GridLayoutManager(context, 2)
        bind.recyclerViewProducts.adapter = adapter

        // FAB Removed - handled by Bottom Navigation "Add" tab

        var searchTimer: java.util.Timer? = null
        bind.editSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                searchTimer?.cancel()
                searchTimer = java.util.Timer()
                searchTimer?.schedule(object : java.util.TimerTask() {
                    override fun run() {
                        // Switch to main thread for ViewModel call just in case or assume VM handles it
                        // Since VM launches coroutine, calling from background thread is OK but updating UI not.
                        // However, onTextChanged is main thread, TimerTask is not.
                        // Proper way:
                        activity?.runOnUiThread {
                            viewModel.searchProducts(query = s.toString())
                        }
                    }
                }, 500) // 500ms debounce
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

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
        
        parentFragmentManager.setFragmentResultListener("requestKeyFilters", viewLifecycleOwner) { requestKey, bundle ->
            val minPrice = bundle.getDouble("minPrice")
            val maxPrice = bundle.getDouble("maxPrice")
            val condition = bundle.getString("condition")
            
            val query = bind.editSearch.text.toString()
            viewModel.searchProducts(query, minPrice, maxPrice, null, condition)
        }

        viewModel.products.observe(viewLifecycleOwner) { products ->
            adapter.updateData(products)
            bind.textEmpty.visibility = if (products.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            bind.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
