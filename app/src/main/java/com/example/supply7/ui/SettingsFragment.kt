package com.example.supply7.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.supply7.R
import com.example.supply7.data.AuthRepository
import com.example.supply7.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var binding: FragmentSettingsBinding? = null
    private val authRepository = AuthRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bind = FragmentSettingsBinding.bind(view)
        binding = bind

        bind.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        bind.btnLogout.setOnClickListener {
            // Confirm logout? For now, direct action.
            authRepository.logout()
            
            // Navigate to Welcome/Login
            // Since we are likely in a single Activity, we might need to reset navigation
            // Best way: Start MainActivity fresh or navigate to generic start destination
            // But if MainActivity hosts everything and checks auth on start, we can restart it.
            
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }

        val prefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
        bind.switchNotifications.isChecked = notificationsEnabled

        bind.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
            
            val msg = if (isChecked) "Notifications Enabled" else "Notifications Disabled"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
