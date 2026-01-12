package com.example.supply7.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.supply7.R
import com.example.supply7.databinding.FragmentWalletBinding
import com.example.supply7.viewmodel.WalletViewModel
import android.app.AlertDialog
import android.widget.EditText
import android.text.InputType

class WalletFragment : Fragment(R.layout.fragment_wallet) {

    private val viewModel: WalletViewModel by viewModels()
    private var binding: FragmentWalletBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bind = FragmentWalletBinding.bind(view)
        binding = bind

        bind.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        // Setup Adapter
        val adapter = CardAdapter() // Will create this inline or separately
        bind.recyclerCards.layoutManager = LinearLayoutManager(context)
        bind.recyclerCards.adapter = adapter

        // Observers
        viewModel.balance.observe(viewLifecycleOwner) { balance ->
            bind.textBalance.text = String.format("₺%.2f", balance)
        }

        viewModel.cards.observe(viewLifecycleOwner) { cards ->
            adapter.updateData(cards)
            bind.textNoCards.visibility = if (cards.isEmpty()) View.VISIBLE else View.GONE
        }
        
        // Actions
        bind.btnAddMoney.setOnClickListener {
            showAddMoneyDialog()
        }
        
        bind.btnAddCard.setOnClickListener {
            // Simplified "Add Card" flow for MVP
            showAddCardDialog()
        }

        viewModel.loadWalletData()
    }
    
    private fun showAddMoneyDialog() {
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.hint = "Amount (e.g. 100)"
        
        AlertDialog.Builder(requireContext())
            .setTitle("Add Money")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val amount = input.text.toString().toDoubleOrNull()
                if (amount != null && amount > 0) {
                    viewModel.addMoney(amount)
                    Toast.makeText(context, "Added ₺$amount", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showAddCardDialog() {
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.hint = "Card Number (Last 4 digits for demo)"
        
        AlertDialog.Builder(requireContext())
            .setTitle("Add New Card")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val num = input.text.toString()
                if (num.length >= 4) {
                    val card = com.example.supply7.data.Card(cardNumber = "**** **** **** $num", expiryDate = "12/28")
                    viewModel.addCard(card)
                    Toast.makeText(context, "Card Saved", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Invalid card number", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
