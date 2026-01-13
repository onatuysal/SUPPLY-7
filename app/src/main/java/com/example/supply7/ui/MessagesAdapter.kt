package com.example.supply7.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.supply7.R
import com.example.supply7.data.Chat
import com.google.firebase.firestore.FirebaseFirestore

class MessagesAdapter(
    private var chats: List<Chat> = emptyList(),
    private val onChatClick: (Chat) -> Unit,
    private val onChatLongClick: ((Chat) -> Unit)? = null
) : RecyclerView.Adapter<MessagesAdapter.ChatViewHolder>() {

    private var selectedIds: Set<String> = emptySet()

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageAvatar: ImageView = view.findViewById(R.id.imageAvatar)
        val textName: TextView = view.findViewById(R.id.textName)
        val textLastMessage: TextView = view.findViewById(R.id.textLastMessage)
        val textTime: TextView = view.findViewById(R.id.textTime)
        val badgeUnread: TextView = view.findViewById(R.id.badgeUnread)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_row, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]

        holder.textName.text = chat.otherUserName
        holder.textLastMessage.text = chat.lastMessage
        holder.textTime.text = if (chat.lastMessageTimestamp > 0) "10:23" else ""
        
        // Load profile photo
        loadUserAvatar(chat, holder.imageAvatar)

        if (chat.unreadCount > 0) {
            holder.badgeUnread.visibility = View.VISIBLE
            holder.textTime.visibility = View.GONE
            holder.badgeUnread.text = chat.unreadCount.toString()
        } else {
            holder.badgeUnread.visibility = View.GONE
            holder.textTime.visibility = View.VISIBLE
        }

        if (chat.unreadCount > 0) {
            holder.textLastMessage.setTextColor(android.graphics.Color.parseColor("#598CD8"))
        } else {
            holder.textLastMessage.setTextColor(android.graphics.Color.parseColor("#757575"))
        }

        // ✅ Selection: ripple bozulmasın diye background'la oynamıyoruz
        val isSelected = selectedIds.contains(chat.id)
        holder.itemView.alpha = if (isSelected) 0.75f else 1f

        holder.itemView.setOnClickListener { onChatClick(chat) }
        holder.itemView.setOnLongClickListener {
            onChatLongClick?.invoke(chat)
            true
        }
    }

    override fun getItemCount(): Int = chats.size

    fun updateData(newChats: List<Chat>) {
        chats = newChats
        notifyDataSetChanged()
    }

    fun setSelectedIds(ids: Set<String>) {
        selectedIds = ids
        notifyDataSetChanged()
    }

    fun deleteByIds(ids: Set<String>) {
        if (ids.isEmpty()) return
        chats = chats.filterNot { ids.contains(it.id) }
        notifyDataSetChanged()
    }
    
    private fun loadUserAvatar(chat: Chat, imageView: ImageView) {
        val context = imageView.context
        val pinkColor = androidx.core.content.ContextCompat.getColor(context, R.color.primary_pink)

        if (chat.otherUserImage.isNotBlank()) {
            imageView.imageTintList = null // Clear tint for real photo
            Glide.with(context)
                .load(chat.otherUserImage)
                .placeholder(R.drawable.user_male)
                .error(R.drawable.user_male) // Revert to user_male on error
                .circleCrop()
                .into(imageView)
            return
        }

        // Fallback: Fetch from Firestore
        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        val otherUserId = chat.participants.firstOrNull { it != currentUserId } ?: return
        
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(otherUserId)
            .get()
            .addOnSuccessListener { doc ->
                val photoUrl = doc.getString("photoUrl")
                if (!photoUrl.isNullOrBlank()) {
                    imageView.imageTintList = null // Clear tint
                    Glide.with(context)
                        .load(photoUrl)
                        .placeholder(R.drawable.user_male)
                        .circleCrop()
                        .into(imageView)
                } else {
                    imageView.setImageResource(R.drawable.user_male)
                    imageView.setColorFilter(pinkColor)
                }
            }
            .addOnFailureListener {
                imageView.setImageResource(R.drawable.user_male)
                imageView.setColorFilter(pinkColor)
            }
    }
}




