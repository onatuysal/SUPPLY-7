package com.example.supply7.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supply7.data.Chat
import com.example.supply7.data.ChatRepository
import com.example.supply7.data.Message
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val repository = ChatRepository()
    
    private val _chats = MutableLiveData<List<Chat>>()
    val chats: LiveData<List<Chat>> = _chats
    
    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages
    
    private var messagesListener: ListenerRegistration? = null
    private var chatsListener: ListenerRegistration? = null
    
    val currentUserId = repository.currentUserId

    private var originalChats = listOf<Chat>()

    fun loadChats() {
        if (chatsListener != null) return
        try {
            val query = repository.getChatsQuery()
            chatsListener = query.addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                val chatList = value?.toObjects(Chat::class.java) ?: emptyList()
                
                // Determine otherUserName based on participants
                val processedList = chatList.map { chat ->
                    // Find the ID that is NOT the current user
                    val otherId = chat.participants.firstOrNull { it != currentUserId } ?: "Unknown"
                    val displayName = if (chat.otherUserName.isNotBlank()) chat.otherUserName else "User ${otherId.take(4)}"
                    chat.copy(otherUserName = displayName)
                }
                originalChats = processedList
                _chats.value = processedList
            }
        } catch (e: Exception) {
            // Handle error (user not logged in etc)
        }
    }

    fun filterChats(query: String) {
        if (query.isBlank()) {
            _chats.value = originalChats
        } else {
            val q = query.lowercase()
            _chats.value = originalChats.filter { 
                it.otherUserName.lowercase().contains(q) || 
                it.lastMessage.lowercase().contains(q)
            }
        }
    }

    private val _chatId = MutableLiveData<String>()
    val chatId: LiveData<String> = _chatId

    fun getOrCreateChat(receiverId: String) {
        viewModelScope.launch {
            try {
                val id = repository.getOrCreateChat(receiverId)
                _chatId.value = id
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    fun loadMessages(chatId: String) {
        messagesListener?.remove()
        val query = repository.getMessagesQuery(chatId)
        messagesListener = query.addSnapshotListener { value, error ->
             if (error != null) return@addSnapshotListener
             val msgList = value?.toObjects(Message::class.java) ?: emptyList()
             _messages.value = msgList
        }
    }
    
    fun sendMessage(
        chatId: String, 
        content: String, 
        receiverId: String,
        offerAmount: String? = null,
        productTitle: String? = null
    ) {
        viewModelScope.launch {
            repository.sendMessage(chatId, content, receiverId, offerAmount, productTitle)
        }
    }

    fun respondToOffer(chatId: String, message: Message, accepted: Boolean) {
        viewModelScope.launch {
            val status = if (accepted) "accepted" else "declined"
            repository.updateMessageStatus(chatId, message.id, status)
            
            // Send system message
            val content = if (accepted) {
                "I accepted your offer of ₺${message.offerAmount} for ${message.productTitle}."
            } else {
                "I declined your offer for ${message.productTitle}."
            }
            // Sender of offer is the receiver of this response
            repository.sendMessage(chatId, content, message.senderId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        messagesListener?.remove()
        chatsListener?.remove()
    }
}
