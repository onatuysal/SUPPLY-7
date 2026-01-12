package com.example.supply7.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.supply7.R
import com.example.supply7.databinding.FragmentMessagesBinding
import com.example.supply7.viewmodel.ChatViewModel

class MessagesFragment : Fragment(R.layout.fragment_messages) {

    private val viewModel: ChatViewModel by viewModels()
    private var binding: FragmentMessagesBinding? = null

    private val selectedIds = linkedSetOf<String>()
    private fun isSelectionMode(): Boolean = selectedIds.isNotEmpty()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bind = FragmentMessagesBinding.bind(view)
        binding = bind

        var adapterRef: MessagesAdapter? = null

        val adapter = MessagesAdapter(
            chats = emptyList(),
            onChatClick = { chat ->
                val a = adapterRef ?: return@MessagesAdapter

                if (isSelectionMode()) {
                    toggleSelection(chat.id)
                    a.setSelectedIds(selectedIds)
                } else {
                    parentFragmentManager.beginTransaction()
                        .replace(
                            R.id.fragment_container,
                            ChatFragment.newInstance(chat.id, chat.otherUserName)
                        )
                        .addToBackStack(null)
                        .commit()
                }
            },
            onChatLongClick = { chat ->
                val a = adapterRef ?: return@MessagesAdapter
                toggleSelection(chat.id)
                a.setSelectedIds(selectedIds)
            }
        )

        adapterRef = adapter

        bind.recyclerMessages.layoutManager = LinearLayoutManager(context)
        bind.recyclerMessages.adapter = adapter

        viewModel.chats.observe(viewLifecycleOwner) { chats ->
            adapter.updateData(chats)

            val idsInList = chats.map { it.id }.toSet()
            selectedIds.retainAll(idsInList)

            adapter.setSelectedIds(selectedIds)
            updateTrashState()
        }

        // Search
        bind.searchChat.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                viewModel.filterChats(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Tabs (UI only)
        updateTabs(bind, "All")
        bind.tabAll.setOnClickListener {
            updateTabs(bind, "All")
            viewModel.filterChats("")
        }
        bind.tabPurchases.setOnClickListener { updateTabs(bind, "Purchases") }
        bind.tabSales.setOnClickListener { updateTabs(bind, "Sales") }
        bind.tabTrades.setOnClickListener { updateTabs(bind, "Trades") }

        updateTrashState()

        // ✅ DELETE: UI'dan anında kaldır + Firestore'dan sil
        bind.btnDelete.setOnClickListener {
            if (selectedIds.isEmpty()) return@setOnClickListener

            val idsToDelete = selectedIds.toSet()

            // 1) UI'dan anında kaldır (optimistic)
            adapter.deleteByIds(idsToDelete)

            // 2) Firestore'dan sil (kalıcı)
            viewModel.deleteChats(idsToDelete)

            Toast.makeText(requireContext(), getString(R.string.msg_deleted_count, idsToDelete.size), Toast.LENGTH_SHORT).show()

            selectedIds.clear()
            adapter.setSelectedIds(emptySet())
            updateTrashState()
        }

        viewModel.loadChats()
    }

    private fun toggleSelection(chatId: String) {
        if (selectedIds.contains(chatId)) selectedIds.remove(chatId) else selectedIds.add(chatId)
        updateTrashState()
    }

    private fun updateTrashState() {
        val bind = binding ?: return
        val enabled = selectedIds.isNotEmpty()
        bind.btnDelete.isEnabled = enabled
        bind.btnDelete.alpha = if (enabled) 1f else 0.35f
    }

    private fun updateTabs(bind: FragmentMessagesBinding, selected: String) {
        val selectedBg = R.drawable.bg_tab_selected
        val unselectedBg = R.drawable.bg_tab_unselected

        fun setTab(view: android.widget.TextView, isSelected: Boolean) {
            view.setBackgroundResource(if (isSelected) selectedBg else unselectedBg)
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





