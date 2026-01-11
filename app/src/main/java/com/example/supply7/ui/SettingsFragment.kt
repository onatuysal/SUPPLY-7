package com.example.supply7.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.supply7.R
import com.example.supply7.data.AuthRepository
import com.example.supply7.databinding.FragmentSettingsBinding
import com.example.supply7.databinding.ItemSettingRowBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val authRepository = AuthRepository()
    private val db = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        ensureUserDocExists()
        bindUserHeaderFromFirestore()

        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
        val languageValue = prefs.getString("language", "English") ?: "English"

        // ---- ACCOUNT ----
        setupRow(binding.rowManageAccount, R.drawable.ic_settings_24, "Manage Account") {
            openFragment(ManageAccountFragment())
        }

        setupRow(binding.rowPaymentMethods, R.drawable.ic_credit_card, "Payment Methods") {
            openFragment(PaymentMethodsFragment())
        }

        // ✅ Change Password artık yeni ekrana götürüyor
        setupRow(binding.rowChangePassword, R.drawable.ic_lock_24, "Change Password") {
            openFragment(ChangePasswordFragment())
        }

        setupRow(binding.rowPrivacySettings, R.drawable.ic_shield_24, "Privacy Settings") {
            openFragment(PrivacySettingsFragment())
        }

        // Notifications switch row
        setupRowAsSwitch(
            row = binding.rowNotifications,
            iconRes = R.drawable.ic_notifications_24,
            title = "Notifications",
            initial = notificationsEnabled
        ) { isChecked ->
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
            toast(if (isChecked) "Notifications Enabled" else "Notifications Disabled")
        }

        // Language value row
        setupRowWithValue(
            row = binding.rowLanguage,
            iconRes = R.drawable.ic_language_24,
            title = "Language",
            value = languageValue
        ) {
            openFragment(LanguageFragment())
        }

        // ---- SUPPORT & LEGAL ----
        setupRow(binding.rowHelpSupport, R.drawable.ic_help_24, "Help & Support") {
            openFragment(HelpSupportFragment())
        }

        setupRow(binding.rowTerms, R.drawable.ic_description_24, "Terms Of Service") {
            openFragment(TermsFragment())
        }

        setupRow(binding.rowPrivacyPolicy, R.drawable.ic_description_24, "Privacy Policy") {
            openFragment(PrivacyPolicyFragment())
        }

        // ---- ACCOUNT ACTIONS ----
        setupRow(binding.rowDownloadData, R.drawable.ic_download_24, "Download your data") {
            toast("Download your data (TODO)")
        }

        setupRow(binding.rowDeactivate, R.drawable.ic_block_24, "Deactivate Account") {
            toast("Deactivate Account (TODO)")
        }

        setupRow(binding.rowLogout, R.drawable.ic_logout, "Log Out") {
            authRepository.logout()
            openFragment(WelcomeFragment())
        }
    }

    private fun ensureUserDocExists() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid

        val userRef = db.collection("users").document(uid)
        userRef.get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) return@addOnSuccessListener

                val data = hashMapOf(
                    "fullName" to (user.displayName ?: user.email?.substringBefore("@") ?: "User"),
                    "email" to (user.email ?: "")
                )
                userRef.set(data)
            }
            .addOnFailureListener {
                // no-op
            }
    }

    private fun bindUserHeaderFromFirestore() {
        val user = FirebaseAuth.getInstance().currentUser

        val fallbackEmail = user?.email ?: "Guest"
        val fallbackName =
            user?.displayName
                ?: user?.email?.substringBefore("@")
                ?: "User"

        binding.txtUserName.text = fallbackName
        binding.txtUserEmail.text = fallbackEmail

        val uid = user?.uid ?: return

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener

                val fullName = doc.getString("fullName")
                val email = doc.getString("email")

                if (!fullName.isNullOrBlank()) binding.txtUserName.text = fullName
                if (!email.isNullOrBlank()) binding.txtUserEmail.text = email
            }
            .addOnFailureListener {
                // fallback already shown
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

    private fun setupRowWithValue(
        row: ItemSettingRowBinding,
        iconRes: Int,
        title: String,
        value: String,
        onClick: () -> Unit
    ) {
        row.imgIcon.setImageResource(iconRes)
        row.txtTitle.text = title

        row.txtValue.isVisible = true
        row.txtValue.text = value
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

        row.switchToggle.setOnCheckedChangeListener { _, isChecked ->
            onToggle(isChecked)
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





