package com.example.supply7.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.supply7.R
import com.example.supply7.databinding.FragmentFavoritesBinding
import com.example.supply7.viewmodel.FavoritesViewModel

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {

    private val viewModel: FavoritesViewModel by viewModels()
    private var binding: FragmentFavoritesBinding? = null
    
    // Reuse ProductAdapter
    private val adapter = ProductAdapter(
        onItemClick = { product ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProductDetailFragment.newInstance(product))
                .addToBackStack(null)
                .commit()
        },
        onFavoriteClick = { product ->
            viewModel.toggleFavorite(product)
            Toast.makeText(context, "Removed from Favorites", Toast.LENGTH_SHORT).show()
            // Reload manually or observe flow
            viewModel.loadFavorites() // Quick refresh
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bind = FragmentFavoritesBinding.bind(view)
        binding = bind
        
        // Ensure Nav is visible
        (activity as? MainActivity)?.showBottomNav(true)

        bind.recyclerViewFavorites.layoutManager = GridLayoutManager(context, 2)
        bind.recyclerViewFavorites.adapter = adapter

        viewModel.favorites.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)
            bind.textEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
             bind.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.loadFavorites()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
