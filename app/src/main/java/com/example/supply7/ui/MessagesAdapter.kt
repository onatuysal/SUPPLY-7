package com.example.supply7.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.supply7.R
import com.example.supply7.data.Chat

class MessagesAdapter(
    private var chats: List<Chat> = emptyList(),
    private val onChatClick: (Chat) -> Unit
) : RecyclerView.Adapter<MessagesAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textName: TextView = view.findViewById(R.id.textName)
        val textLastMessage: TextView = view.findViewById(R.id.textLastMessage)
        val textTime: TextView = view.findViewById(R.id.textTime)
        val badgeUnread: TextView = view.findViewById(R.id.badgeUnread)
        val imageAvatar: android.widget.ImageView = view.findViewById(R.id.imageAvatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_row, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        holder.textName.text = chat.otherUserName
        holder.textLastMessage.text = chat.lastMessage
        
        holder.textTime.text = "12:30" // Placeholder time
        holder.textTime.text = if (chat.lastMessageTimestamp > 0) "10:23" else "" 

        // Initial placeholder logic removed.
        // We rely on the logic below based on data.
        // If position is 2 (Mert Demir in example), show badge
        // Badge Logic
        if (chat.unreadCount > 0) {
            holder.badgeUnread.visibility = View.VISIBLE
            holder.textTime.visibility = View.GONE
            holder.badgeUnread.text = chat.unreadCount.toString()
            holder.textLastMessage.setTextColor(android.graphics.Color.parseColor("#E73F98")) // Pink text
        } else {
            holder.badgeUnread.visibility = View.GONE
            holder.textTime.visibility = View.VISIBLE
            holder.textLastMessage.setTextColor(android.graphics.Color.parseColor("#598CD8")) // Blue/Gray text
        }

        // Color logic from screenshot: 
        // Some are blue (question?), some gray (answer?). 
        // I'll stick to blue for now or alternating.
        // Text Color Logic based on unread status
        if (chat.unreadCount > 0) {
             holder.textLastMessage.setTextColor(android.graphics.Color.parseColor("#598CD8"))
        } else {
             holder.textLastMessage.setTextColor(android.graphics.Color.parseColor("#757575"))
        }
        
        holder.itemView.setOnClickListener { onChatClick(chat) }
    }

    override fun getItemCount() = chats.size

    fun updateData(newChats: List<Chat>) {
        chats = newChats
        notifyDataSetChanged()
    }
}
