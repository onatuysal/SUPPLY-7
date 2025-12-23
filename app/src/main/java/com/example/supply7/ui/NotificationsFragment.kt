package com.example.supply7.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.supply7.R
import com.example.supply7.databinding.FragmentNotificationsBinding
import com.example.supply7.viewmodel.NotificationsViewModel

class NotificationsFragment : Fragment(R.layout.fragment_notifications) {

    private val viewModel: NotificationsViewModel by viewModels()
    private var binding: FragmentNotificationsBinding? = null
    
    private val adapter = NotificationsAdapter { notification ->
        // Mark as read
        viewModel.markAsRead(notification)
        
        // Action based on type
        if (notification.type == "OFFER" || notification.type == "MESSAGE") {
             // Go to Chat
             // Assuming relatedId is chatId or userId? 
             // Ideally relatedId should be enough to navigate.
             // If relatedId is chatId, we need a way to open chat by ID which ChatFragment supports via constructor logic update or just newInstance logic.
             // For now, let's assume relatedId is the ChatId.
             
             // We need to pass chatId to ChatFragment.
             // Our ChatFragment.newInstance currently takes (chatId, otherUserName, receiverId)
             // We might lack otherUserName here.
             // We can fetch chat details or just pass ID and let ChatFragment fetch details.
             // Let's modify ChatFragment slightly to handle ID-only load if possible or just pass what we have.
             
             // Simplification: Just go to MessagesFragment (Inbox) for now if complex.
             // Or better:
             parentFragmentManager.beginTransaction()
                 .replace(R.id.fragment_container, MessagesFragment()) // Go to inbox
                 .addToBackStack(null)
                 .commit()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bind = FragmentNotificationsBinding.bind(view)
        binding = bind

        bind.recyclerViewNotifications.adapter = adapter
        
        bind.btnClose.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        viewModel.notifications.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)
            bind.textEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
             bind.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.loadNotifications()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
