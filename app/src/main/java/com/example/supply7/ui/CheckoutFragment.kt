package com.example.supply7.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.supply7.R
import com.example.supply7.data.Address
import com.example.supply7.data.CartItem
import com.example.supply7.databinding.FragmentCheckoutBinding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

import androidx.fragment.app.viewModels
import com.example.supply7.viewmodel.CartViewModel

class CheckoutFragment : Fragment(R.layout.fragment_checkout) {

    private var binding: FragmentCheckoutBinding? = null
    
    private val cartViewModel: CartViewModel by viewModels()

    // We need cart items and total passed here, or fetched.
    // Let's assume passed via arguments or we fetch from repo.
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bind = FragmentCheckoutBinding.bind(view)
        binding = bind
        
        bind.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
        
        // Fetch items to calculate total again or ensure valid
        cartViewModel.loadCart()
        
        cartViewModel.cartItems.observe(viewLifecycleOwner) { items ->
            if (items.isEmpty()) {
                // Should not happen if we came from non-empty cart, unless refreshed
                // If checking out, we might want to stay until success
            }
            val total = items.sumOf { item -> item.price * item.quantity }
            bind.textTotalAmount.text = "Total: ₺$total"
            
            bind.btnConfirmPayment.setOnClickListener {
                 processCheckout(bind, items, total)
            }
        }
        
        cartViewModel.checkoutStatus.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                if (result.isSuccess) {
                    cartViewModel.resetCheckoutStatus()
                    parentFragmentManager.beginTransaction()
                         .replace(R.id.fragment_container, SuccessFragment())
                         .commit()
                } else {
                    Toast.makeText(context, "Checkout Failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                    cartViewModel.resetCheckoutStatus()
                }
            }
        }
    }
    
    private fun processCheckout(bind: FragmentCheckoutBinding, items: List<com.example.supply7.data.CartItem>, total: Double) {
        val name = bind.inputName.text.toString()
        val address = bind.inputAddress.text.toString()
        val city = bind.inputCity.text.toString()
        val zip = bind.inputZip.text.toString()
        val cardNum = bind.inputCardNumber.text.toString()
        
        if (name.isBlank() || address.isBlank() || city.isBlank() || cardNum.isBlank()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        val shippingAddress = Address(name, address, city, zip)
        
        cartViewModel.checkout(shippingAddress)
        
        // Observe status in onViewCreated, but here we can just wait for observer there?
        // Actually, best practice is to observe in onViewCreated. 
        // Let's add observer to onViewCreated and just trigger here.
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
