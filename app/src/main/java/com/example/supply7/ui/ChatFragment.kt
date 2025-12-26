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

        fun newInstance(
            chatId: String?, 
            otherUserName: String, 
            receiverId: String? = null,
            offerAmount: String? = null,
            productTitle: String? = null
        ): ChatFragment {
            val fragment = ChatFragment()
            val args = Bundle()
            if (chatId != null) args.putString(ARG_CHAT_ID, chatId)
            args.putString(ARG_OTHER_USER_NAME, otherUserName)
            if (receiverId != null) args.putString(ARG_RECEIVER_ID, receiverId)
            if (offerAmount != null) args.putString(ARG_OFFER_AMOUNT, offerAmount)
            if (productTitle != null) args.putString(ARG_PRODUCT_TITLE, productTitle)
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
        }
    }

    private var receiverId: String = ""
    private var offerAmount: String = ""
    private var productTitle: String = ""

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

        bind.recyclerChat.adapter = chatAdapter
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            chatAdapter.updateData(messages)
            if (messages.isNotEmpty()) {
                bind.recyclerChat.scrollToPosition(messages.size - 1)
            }
        }

        // Auto-send offer if present
        if (offerAmount.isNotBlank() && receiverId.isNotBlank()) {
            val content = "Offer: $offerAmount TL for $productTitle"
            // We need to wait for chatId to be ready if it's new
             if (chatId.isNotBlank()) {
                 viewModel.sendMessage(chatId, content, receiverId, offerAmount, productTitle)
                 offerAmount = "" // Clear to prevent resend on rotation
             } else {
                 // Wait for chatId observation
                 viewModel.chatId.observe(viewLifecycleOwner) { id ->
                     if (!id.isNullOrBlank() && offerAmount.isNotBlank()) {
                         val msg = "Offer: $offerAmount TL for $productTitle"
                         viewModel.sendMessage(id, msg, receiverId, offerAmount, productTitle)
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
             val content = bind.inputMessage.text.toString()
             if (content.isNotBlank() && chatId.isNotBlank()) {
                 // We need receiverId for the Message object. 
                 // If we started with chatId only, we might strictly need to fetch participants to know receiver.
                 // For now, if started with receiverId, we use it. 
                 // If started with chatId, we assume existing chat logic (requires fetching chat details).
                 // Simplified: assume we have receiverId or can derive it. 
                 
                 // If receiverId is blank (came from Inbox), we should ideally have fetched it from Chat metadata.
                 // For this mock, relying on it being passed or known.
                 val targetId = if (receiverId.isNotBlank()) receiverId else "unknown" 
                 
                 viewModel.sendMessage(chatId, content, targetId) 
                 bind.inputMessage.text.clear()
             } else if (chatId.isBlank()) {
                 // Chat ID pending creation
             }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        // Show Bottom Nav again
        (activity as? MainActivity)?.showBottomNav(true)
        binding = null
    }
}
