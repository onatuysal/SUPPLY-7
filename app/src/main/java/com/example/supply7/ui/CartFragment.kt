package com.example.supply7.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.supply7.R
import com.example.supply7.databinding.FragmentCartBinding
import com.example.supply7.viewmodel.CartViewModel

class CartFragment : Fragment(R.layout.fragment_cart) {

    private val viewModel: CartViewModel by viewModels()
    private var binding: FragmentCartBinding? = null
    
    private val adapter = CartAdapter { item ->
        viewModel.removeFromCart(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bind = FragmentCartBinding.bind(view)
        binding = bind
        
        // Hide Bottom Nav
        (activity as? MainActivity)?.showBottomNav(false)

        bind.recyclerViewCart.adapter = adapter
        bind.btnClose.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        bind.btnCheckout.setOnClickListener {
            // Confirm Dialog
            AlertDialog.Builder(requireContext())
                .setTitle("Confirm Order")
                .setMessage("Complete purchase for ${bind.textTotal.text}?")
                .setPositiveButton("Pay") { _, _ ->
                     viewModel.checkout()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        viewModel.cartItems.observe(viewLifecycleOwner) { items ->
            adapter.updateData(items)
            bind.textEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            bind.layoutFooter.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        }
        
        viewModel.totalPrice.observe(viewLifecycleOwner) { total ->
            bind.textTotal.text = "$total TL"
        }

        viewModel.checkoutStatus.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                if (result.isSuccess) {
                    Toast.makeText(context, "Order Placed Successfully!", Toast.LENGTH_LONG).show()
                    parentFragmentManager.popBackStack() // Or go to Orders screen
                } else {
                    Toast.makeText(context, "Order Failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }
                viewModel.resetCheckoutStatus()
            }
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
             bind.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
             bind.btnCheckout.isEnabled = !isLoading
        }
        
        viewModel.loadCart()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainActivity)?.showBottomNav(true)
        binding = null
    }
}
