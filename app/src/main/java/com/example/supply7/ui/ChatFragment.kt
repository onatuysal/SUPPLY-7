package com.example.supply7.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.supply7.R
import com.example.supply7.databinding.FragmentChatBinding
import com.example.supply7.viewmodel.ChatViewModel

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private val viewModel: ChatViewModel by viewModels()
    private var binding: FragmentChatBinding? = null

    private lateinit var chatAdapter: ChatAdapter
    private var chatId: String = ""
    private var otherUserName: String = ""

    companion object {
        const val ARG_CHAT_ID = "chat_id"
        const val ARG_OTHER_USER_NAME = "other_user_name"
        const val ARG_RECEIVER_ID = "receiver_id"
        const val ARG_OFFER_AMOUNT = "offer_amount"
        const val ARG_PRODUCT_TITLE = "product_title"
        const val ARG_PRODUCT_ID = "product_id"
        const val ARG_PRODUCT_IMAGE = "product_image"

        fun newInstance(
            chatId: String?, 
            otherUserName: String, 
            receiverId: String? = null,
            offerAmount: String? = null,
            productTitle: String? = null,
            productId: String? = null,
            productImageUrl: String? = null
        ): ChatFragment {
            val fragment = ChatFragment()
            val args = Bundle()
            if (chatId != null) args.putString(ARG_CHAT_ID, chatId)
            args.putString(ARG_OTHER_USER_NAME, otherUserName)
            if (receiverId != null) args.putString(ARG_RECEIVER_ID, receiverId)
            if (offerAmount != null) args.putString(ARG_OFFER_AMOUNT, offerAmount)
            if (productTitle != null) args.putString(ARG_PRODUCT_TITLE, productTitle)
            if (productId != null) args.putString(ARG_PRODUCT_ID, productId)
            if (productImageUrl != null) args.putString(ARG_PRODUCT_IMAGE, productImageUrl)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            chatId = it.getString(ARG_CHAT_ID, "")
            otherUserName = it.getString(ARG_OTHER_USER_NAME, "Chat")
            receiverId = it.getString(ARG_RECEIVER_ID, "")
            offerAmount = it.getString(ARG_OFFER_AMOUNT, "")
            productTitle = it.getString(ARG_PRODUCT_TITLE, "")
            productId = it.getString(ARG_PRODUCT_ID, "")
            productImageUrl = it.getString(ARG_PRODUCT_IMAGE, "")
        }
    }

    private var receiverId: String = ""
    private var offerAmount: String = ""
    private var productTitle: String = ""
    private var productId: String = ""
    private var productImageUrl: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bind = FragmentChatBinding.bind(view)
        binding = bind
        
        // Hide Bottom Nav
        (activity as? MainActivity)?.showBottomNav(false)

        bind.textNameHeader.text = otherUserName
        
        bind.btnBack.setOnClickListener {
             parentFragmentManager.popBackStack()
        }

        val currentUserId = viewModel.currentUserId ?: ""
        chatAdapter = ChatAdapter(currentUserId)

        bind.recyclerChat.layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true
        }

        chatAdapter.onAcceptOffer = { message ->
            if (chatId.isNotBlank()) {
                viewModel.respondToOffer(chatId, message, true)
            }
        }

        chatAdapter.onDeclineOffer = { message ->
            if (chatId.isNotBlank()) {
                viewModel.respondToOffer(chatId, message, false)
            }
        }

        chatAdapter.onBuyNowClick = { message ->
            // Construct CartItem for direct purchase
            val price = message.offerAmount?.toDoubleOrNull() ?: 0.0
            
            val item = com.example.supply7.data.CartItem(
                productId = message.productId, // This should be populated now
                productTitle = message.productTitle ?: "Unknown",
                price = price,
                imageUrl = message.productImageUrl ?: "",
                quantity = 1,
                sellerId = message.receiverId 
            )
            
            val items = ArrayList<com.example.supply7.data.CartItem>()
            items.add(item)
            
            // Navigate to Checkout
            val checkoutFragment = CheckoutFragment()
            val args = Bundle()
            args.putSerializable("items", items)
            checkoutFragment.arguments = args
            
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, checkoutFragment)
                .addToBackStack(null)
                .commit()
        }

        bind.recyclerChat.adapter = chatAdapter
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            chatAdapter.updateData(messages)
            if (messages.isNotEmpty()) {
                bind.recyclerChat.scrollToPosition(messages.size - 1)
            }
        }

        // Auto-send offer if present
        if (offerAmount.isNotBlank() && receiverId.isNotBlank()) {
            val content = getString(R.string.msg_offer_format, offerAmount, productTitle)
            // We need to wait for chatId to be ready if it's new
             if (chatId.isNotBlank()) {
                 viewModel.sendMessage(chatId, content, receiverId, offerAmount, productTitle, productId, productImageUrl)
                 offerAmount = "" // Clear to prevent resend on rotation
             } else {
                 // Wait for chatId observation
                 viewModel.chatId.observe(viewLifecycleOwner) { id ->
                     if (!id.isNullOrBlank() && offerAmount.isNotBlank()) {
                         val msg = "Offer: $offerAmount TL for $productTitle"
                         viewModel.sendMessage(id, msg, receiverId, offerAmount, productTitle, productId, productImageUrl)
                         offerAmount = ""
                     }
                 }
                 // Trigger creation
                 viewModel.getOrCreateChat(receiverId)
             }
        } else if (chatId.isBlank() && receiverId.isNotBlank()) {
            viewModel.getOrCreateChat(receiverId)
        } else if (chatId.isNotBlank()) {
            viewModel.loadMessages(chatId)
        }

        viewModel.chatId.observe(viewLifecycleOwner) { id ->
            if (!id.isNullOrBlank()) {
                chatId = id
                viewModel.loadMessages(id)
            }
        }

        bind.btnSend.setOnClickListener {
             sendMessage(bind)
        }

        bind.inputMessage.setOnEditorActionListener { _, actionId, event ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND ||
                (event != null && event.keyCode == android.view.KeyEvent.KEYCODE_ENTER && event.action == android.view.KeyEvent.ACTION_DOWN)) {
                sendMessage(bind)
                true
            } else {
                false
            }
        }
    }

    private fun sendMessage(bind: FragmentChatBinding) {
        val content = bind.inputMessage.text.toString()
        if (content.isNotBlank() && chatId.isNotBlank()) {
            val targetId = receiverId.ifBlank { "unknown" }
            viewModel.sendMessage(chatId, content, targetId)
            bind.inputMessage.text.clear()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        // Show Bottom Nav again
        (activity as? MainActivity)?.showBottomNav(true)
        binding = null
    }
}
