package com.example.supply7.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.supply7.R
import com.example.supply7.databinding.FragmentFavoritesBinding
import com.example.supply7.viewmodel.FavoritesViewModel

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {

    private val favoritesViewModel: FavoritesViewModel by activityViewModels()

    private var binding: FragmentFavoritesBinding? = null
    private lateinit var adapter: ProductAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.showBottomNav(true)

        val bind = FragmentFavoritesBinding.bind(view)
        binding = bind

        adapter = ProductAdapter(
            onItemClick = { product ->
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ProductDetailFragment.newInstance(product))
                    .addToBackStack(null)
                    .commit()
            },
            onFavoriteClick = { product ->
                favoritesViewModel.toggleFavorite(product)
            }
        )

        bind.recyclerViewFavorites.layoutManager = GridLayoutManager(context, 2)
        bind.recyclerViewFavorites.adapter = adapter

        // Favori ürün listesini gözle
        favoritesViewModel.favorites.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)
            // Eğer layout'ta "textEmptyFavorites" id'li bir TextView varsa açıp kullanabilirsin:
            // bind.textEmptyFavorites.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        // Kalplerin dolu/boş durumunu güncelle
        favoritesViewModel.favoriteIds.observe(viewLifecycleOwner) { ids ->
            adapter.setFavorites(ids)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}


