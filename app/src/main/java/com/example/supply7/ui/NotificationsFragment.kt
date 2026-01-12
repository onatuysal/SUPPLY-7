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
         // Action based on type
         if (notification.type == "OFFER" || notification.type == "MESSAGE") {
             // relatedId is the chatId
             parentFragmentManager.beginTransaction()
                 .replace(
                     R.id.fragment_container, 
                     ChatFragment.newInstance(
                         chatId = notification.relatedId,
                         otherUserName = "Chat", // We don't have name here, but ChatFragment fetches it if ID is present
                         receiverId = null 
                     )
                 )
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
