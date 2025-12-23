package com.example.supply7.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.supply7.R
import com.example.supply7.databinding.FragmentMessagesBinding
import com.example.supply7.viewmodel.ChatViewModel

class MessagesFragment : Fragment(R.layout.fragment_messages) {

    private val viewModel: ChatViewModel by viewModels()
    private var binding: FragmentMessagesBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bind = FragmentMessagesBinding.bind(view)
        binding = bind

        val adapter = MessagesAdapter { chat ->
            // Navigate to ChatFragment
            // We need to pass chat ID and other user details
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ChatFragment.newInstance(chat.id, chat.otherUserName)) // Simplified
                .addToBackStack(null)
                .commit()
        }

        bind.recyclerMessages.layoutManager = LinearLayoutManager(context)
        bind.recyclerMessages.adapter = adapter

        viewModel.chats.observe(viewLifecycleOwner) { chats ->
            adapter.updateData(chats)
        }
        
        // Load chats
        // Check Chat.kt first to avoid crashes if properties missing
        // Search Logic
        bind.searchChat.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                viewModel.filterChats(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Initial Tab State
        updateTabs(bind, "All")

        bind.tabAll.setOnClickListener { 
            updateTabs(bind, "All")
            viewModel.filterChats("") // Reset or specific logic if we added type filtering
        }
        bind.tabPurchases.setOnClickListener { 
            updateTabs(bind, "Purchases") 
            // In future: viewModel.filterByType("Purchase")
        }
        bind.tabSales.setOnClickListener { updateTabs(bind, "Sales") }
        bind.tabTrades.setOnClickListener { updateTabs(bind, "Trades") }

        // Mock Data Loading for visual check if viewModel empty
        // ... (Optional)
        viewModel.loadChats()
    }

    private fun updateTabs(bind: FragmentMessagesBinding, selected: String) {
        val selectedBg = R.drawable.bg_tab_selected
        val unselectedBg = R.drawable.bg_tab_unselected
        // We can just rely on textColor to distinguish further if needed, but the bg is main.
        
        fun setTab(view: android.widget.TextView, isSelected: Boolean) {
            view.setBackgroundResource(if (isSelected) selectedBg else unselectedBg)
            // If selected: pink bg, dark text? Or White text? Design had Pink BG.
            // Screenshot "All" is Pink. Let's assume standard text color is fine.
        }

        setTab(bind.tabAll, selected == "All")
        setTab(bind.tabPurchases, selected == "Purchases")
        setTab(bind.tabSales, selected == "Sales")
        setTab(bind.tabTrades, selected == "Trades")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
