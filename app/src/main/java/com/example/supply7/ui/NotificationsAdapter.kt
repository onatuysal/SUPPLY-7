package com.example.supply7.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.supply7.R
import com.example.supply7.data.Notification

class NotificationsAdapter(
    private var notifications: List<Notification> = emptyList(),
    private val onItemClick: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root: View = view
        val title: TextView = view.findViewById(R.id.textTitle)
        val body: TextView = view.findViewById(R.id.textBody)
        val date: TextView = view.findViewById(R.id.textDate)
        val icon: ImageView = view.findViewById(R.id.iconNotification)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.title.text = notification.title
        holder.body.text = notification.body
        holder.date.text = "Just now" // Simplified logic, implement real formatter later
        
        // Icon logic based on type could go here
        
        holder.root.setOnClickListener {
            onItemClick(notification)
        }
        
        // Visual indicator for unread? Card background color maybe.
        if (!notification.isRead) {
            holder.root.alpha = 1.0f
        } else {
            holder.root.alpha = 0.6f
        }
    }

    override fun getItemCount() = notifications.size

    fun updateData(newNotifications: List<Notification>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }
}
