package com.example.supply7.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.supply7.R
import com.example.supply7.databinding.FragmentPrivacySettingsBinding
import com.example.supply7.databinding.ItemSettingRowBinding

class PrivacySettingsFragment : Fragment(R.layout.fragment_privacy_settings) {

    private var _binding: FragmentPrivacySettingsBinding? = null
    private val binding get() = _binding!!

    private val prefs by lazy {
        requireContext().getSharedPreferences("privacy_prefs", Context.MODE_PRIVATE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPrivacySettingsBinding.bind(view)

        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        val makePublic = prefs.getBoolean("make_public", false)
        val showPurchase = prefs.getBoolean("show_purchase_history", true)
        val showFollowing = prefs.getBoolean("show_following", true)
        val personalizedAds = prefs.getBoolean("personalized_ads", true)

        // Profile Visibility
        setupRowAsSwitch(
            row = binding.rowMakeProfilePublic,
            iconRes = R.drawable.ic_user_24,
            title = "Make profile public",
            initial = makePublic
        ) { checked ->
            prefs.edit().putBoolean("make_public", checked).apply()
            toast(if (checked) "Profile is public" else "Profile is private")
        }

        setupRowAsSwitch(
            row = binding.rowShowPurchaseHistory,
            iconRes = R.drawable.ic_description_24,
            title = "Show purchase history",
            initial = showPurchase
        ) { checked ->
            prefs.edit().putBoolean("show_purchase_history", checked).apply()
        }

        setupRowAsSwitch(
            row = binding.rowShowFollowing,
            iconRes = R.drawable.ic_user_24,
            title = "Show who I'm following",
            initial = showFollowing
        ) { checked ->
            prefs.edit().putBoolean("show_following", checked).apply()
        }

        // Data & Personalization
        setupRowAsSwitch(
            row = binding.rowPersonalizedAds,
            iconRes = R.drawable.ic_description_24,
            title = "Personalized Ads",
            initial = personalizedAds
        ) { checked ->
            prefs.edit().putBoolean("personalized_ads", checked).apply()
        }

        setupRow(
            row = binding.rowPrivacyPolicy,
            iconRes = R.drawable.ic_description_24,
            title = "Privacy Policy"
        ) {
            openFragment(PrivacyPolicyFragment())
        }

        // Communication
        setupRow(
            row = binding.rowBlockedUsers,
            iconRes = R.drawable.ic_block_24,
            title = "Blocked users"
        ) {
            toast("Blocked users (TODO)")
        }
    }

    private fun setupRow(
        row: ItemSettingRowBinding,
        iconRes: Int,
        title: String,
        onClick: () -> Unit
    ) {
        row.imgIcon.setImageResource(iconRes)
        row.txtTitle.text = title

        row.txtValue.isVisible = false
        row.switchToggle.isVisible = false
        row.imgChevron.isVisible = true

        row.root.setOnClickListener { onClick() }
    }

    private fun setupRowAsSwitch(
        row: ItemSettingRowBinding,
        iconRes: Int,
        title: String,
        initial: Boolean,
        onToggle: (Boolean) -> Unit
    ) {
        row.imgIcon.setImageResource(iconRes)
        row.txtTitle.text = title

        row.txtValue.isVisible = false
        row.imgChevron.isVisible = false

        row.switchToggle.isVisible = true
        row.switchToggle.isChecked = initial

        row.root.setOnClickListener {
            row.switchToggle.isChecked = !row.switchToggle.isChecked
        }

        row.switchToggle.setOnCheckedChangeListener { _, checked ->
            onToggle(checked)
        }
    }

    private fun openFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



