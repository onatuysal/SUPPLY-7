package com.example.supply7.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.supply7.R
import com.example.supply7.databinding.FragmentOrdersBinding
import com.example.supply7.viewmodel.OrdersViewModel
import androidx.fragment.app.viewModels

class OrdersFragment : Fragment(R.layout.fragment_orders) {

    private val viewModel: OrdersViewModel by viewModels()
    private var binding: FragmentOrdersBinding? = null
    
    // Adapter logic later
    private val adapter = OrdersAdapter { _ ->
        // Detail view? For now maybe just list.
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bind = FragmentOrdersBinding.bind(view)
        binding = bind
        
        bind.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
        bind.recyclerViewOrders.adapter = adapter
        
        viewModel.orders.observe(viewLifecycleOwner) { orders ->
            adapter.updateData(orders)
            bind.textEmpty.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
        }
        
        viewModel.loadOrders()
    }
}
