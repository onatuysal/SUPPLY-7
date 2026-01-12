package com.example.supply7.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.supply7.R
import com.example.supply7.data.Address
import com.example.supply7.data.Card
import com.example.supply7.databinding.FragmentCheckoutBinding
import com.example.supply7.viewmodel.CartViewModel
import com.example.supply7.viewmodel.WalletViewModel
import com.bumptech.glide.Glide

class CheckoutFragment : Fragment(R.layout.fragment_checkout) {

    private var binding: FragmentCheckoutBinding? = null
    private val cartViewModel: CartViewModel by viewModels()
    private val walletViewModel: WalletViewModel by viewModels() // Reuse Wallet VM for cards

    private var selectedDeliveryLocation: String? = null
    private var selectedCard: Card? = null

    private var currentCartItems: List<com.example.supply7.data.CartItem> = emptyList()
    private var isDirectPurchase = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bind = FragmentCheckoutBinding.bind(view)
        binding = bind

        // User Setup
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Set initial text
            bind.textUserName.text = currentUser.displayName ?: "User"

            // Try explicit Firestore fetch for most up-to-date data
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                         val name = document.getString("name") ?: document.getString("fullName") ?: currentUser.displayName
                         bind.textUserName.text = name ?: "User"
                    } else {
                         bind.textUserName.text = currentUser.displayName ?: "User"
                    }
                }
                .addOnFailureListener {
                    bind.textUserName.text = currentUser.displayName ?: "User"
                }

            if (currentUser.photoUrl != null) {
                Glide.with(this).load(currentUser.photoUrl).circleCrop().into(bind.imgUserAvatar)
            }
        }

        bind.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        // 1. Delivery Selection
        bind.cardUserInfo.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DeliverySelectionFragment())
                .addToBackStack(null)
                .commit()
        }

        parentFragmentManager.setFragmentResultListener("requestKeyDelivery", viewLifecycleOwner) { _, bundle ->
            val location = bundle.getString("location")
            if (location != null) {
                selectedDeliveryLocation = location
                bind.textSelectedLocation.text = location
            }
        }
        // 2. Cards Setup
        walletViewModel.loadWalletData()
        walletViewModel.cards.observe(viewLifecycleOwner) { cards ->
            val cardAdapter = CardAdapter(cards) { card ->
                selectedCard = card
            }
            bind.recyclerCards.layoutManager = LinearLayoutManager(context)
            bind.recyclerCards.adapter = cardAdapter
            
            if (cards.isNotEmpty()) {
                selectedCard = cards.first()
            }
        }

        bind.btnAddCard.setOnClickListener {
             showAddCardDialog()
        }

        // 3. Totals - Check arguments first for direct purchase
        @Suppress("UNCHECKED_CAST")
        val directItems = arguments?.getSerializable("items") as? ArrayList<com.example.supply7.data.CartItem>
        
        if (directItems != null && directItems.isNotEmpty()) {
            // Direct purchase from offer
            isDirectPurchase = true
            currentCartItems = directItems
            val total = directItems.sumOf { it.price * it.quantity }
            bind.textTotalPrice.text = "₺$total"
            
            bind.btnConfirmPayment.setOnClickListener {
                processCheckout(total)
            }
        } else {
            // Normal cart checkout
            isDirectPurchase = false
            cartViewModel.loadCart()
            cartViewModel.cartItems.observe(viewLifecycleOwner) { items ->
                currentCartItems = items
                val total = items.sumOf { it.price * it.quantity }
                bind.textTotalPrice.text = "₺$total"

                bind.btnConfirmPayment.setOnClickListener {
                    processCheckout(total)
                }
            }
        }
        
        cartViewModel.checkoutStatus.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                if (result.isSuccess) {
                    cartViewModel.resetCheckoutStatus()
                    
                    val fragment = SuccessFragment()
                    val args = Bundle()
                    // Pass items as ArrayList (Serializable)
                    args.putSerializable("items", ArrayList(currentCartItems))
                    fragment.arguments = args

                    parentFragmentManager.beginTransaction()
                         .replace(R.id.fragment_container, fragment)
                         .commit()
                } else {
                    Toast.makeText(context, "Checkout Failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                    cartViewModel.resetCheckoutStatus()
                }
            }
        }
    }

    private fun showAddCardDialog() {
        // Simple input for card number
        val input = android.widget.EditText(requireContext())
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        input.hint = "Card Number (Last 4 digits)"
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Add New Card")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val num = input.text.toString()
                if (num.length >= 4) {
                    val card = Card(cardNumber = "**** **** **** $num", expiryDate = "12/28")
                    walletViewModel.addCard(card)
                    Toast.makeText(context, "Card Saved", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Invalid card number", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun processCheckout(amount: Double) {
        if (selectedDeliveryLocation == null) {
            Toast.makeText(context, "Please select a delivery point", Toast.LENGTH_SHORT).show()
            return
        }
        
        val shippingAddress = Address(
            name = binding?.textUserName?.text.toString(),
            address = selectedDeliveryLocation!!,
            city = "Campus",
            zip = "00000"
        )

        if (isDirectPurchase) {
            // Direct purchase - create order manually
            createOrderDirectly(shippingAddress)
        } else {
            // Normal cart flow
            cartViewModel.checkout(shippingAddress)
        }
    }

    private fun createOrderDirectly(shippingAddress: Address) {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val order = com.example.supply7.data.Order(
            id = "",
            userId = currentUser.uid,
            items = currentCartItems,
            totalAmount = currentCartItems.sumOf { it.price * it.quantity },
            status = "CONFIRMED",
            timestamp = System.currentTimeMillis(),
            shippingAddress = shippingAddress
        )

        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        
        // Use a transaction to ensure stock is updated atomically
        db.runTransaction { transaction ->
            // 1. Deduct stock from each product
            currentCartItems.forEach { item ->
                val productRef = db.collection("products").document(item.productId)
                val snapshot = transaction.get(productRef)
                
                if (snapshot.exists()) {
                    val currentStock = snapshot.getLong("stock")?.toInt() ?: 0
                    val newStock = (currentStock - item.quantity).coerceAtLeast(0)
                    transaction.update(productRef, "stock", newStock)
                }
            }
            
            // 2. Create the order
            val orderRef = db.collection("orders").document()
            transaction.set(orderRef, order.copy(id = orderRef.id))
        }.addOnSuccessListener {
            // Navigate to success
            val fragment = SuccessFragment()
            val args = Bundle()
            args.putSerializable("items", ArrayList(currentCartItems))
            fragment.arguments = args

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Checkout Failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
