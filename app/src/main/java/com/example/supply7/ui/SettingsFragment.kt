package com.example.supply7.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
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

        setupRow(binding.rowManageAccount, R.drawable.ic_settings_24, getString(R.string.settings_manage_account)) {
            openFragment(ManageAccountFragment())
        }

        setupRow(binding.rowPaymentMethods, R.drawable.ic_credit_card, getString(R.string.settings_payment_methods)) {
            openFragment(WalletFragment())
        }

        setupRow(binding.rowChangePassword, R.drawable.ic_lock_24, getString(R.string.settings_change_password)) {
            openFragment(ChangePasswordFragment())
        }

        setupRow(binding.rowPrivacySettings, R.drawable.ic_shield_24, getString(R.string.settings_privacy)) {
            openFragment(PrivacySettingsFragment())
        }

        setupRowAsSwitch(
            row = binding.rowNotifications,
            iconRes = R.drawable.ic_notifications_24,
            title = getString(R.string.settings_notifications),
            initial = notificationsEnabled
        ) { isChecked ->
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
            toast(if (isChecked) "Notifications Enabled" else "Notifications Disabled")
        }

        setupRowWithValue(
            row = binding.rowLanguage,
            iconRes = R.drawable.ic_language_24,
            title = getString(R.string.language),
            value = languageValue
        ) {
            openFragment(LanguageFragment())
        }

        setupRow(binding.rowHelpSupport, R.drawable.ic_help_24, getString(R.string.settings_help)) {
            openFragment(HelpSupportFragment())
        }

        setupRow(binding.rowTerms, R.drawable.ic_description_24, getString(R.string.settings_terms)) {
            openFragment(TermsFragment())
        }

        setupRow(binding.rowPrivacyPolicy, R.drawable.ic_description_24, getString(R.string.settings_privacy_policy)) {
            openFragment(PrivacyPolicyFragment())
        }

        setupRow(binding.rowDownloadData, R.drawable.ic_download_24, getString(R.string.settings_download_data)) {
            toast("Download your data (TODO)")
        }

        setupRow(binding.rowLogout, R.drawable.ic_logout, getString(R.string.settings_logout)) {
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
        binding.imgAvatar.setImageResource(R.drawable.generic_ava)

        val uid = user?.uid ?: return

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener

                val fullName = doc.getString("fullName")
                val email = doc.getString("email")
                val photoUrl = doc.getString("photoUrl")

                if (!fullName.isNullOrBlank()) binding.txtUserName.text = fullName
                if (!email.isNullOrBlank()) binding.txtUserEmail.text = email

                if (!photoUrl.isNullOrBlank()) {
                    Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.generic_ava)
                        .error(R.drawable.generic_ava)
                        .into(binding.imgAvatar)
                }
            }
            .addOnFailureListener { e ->
                Log.e("SETTINGS_HEADER", "Failed to load user header", e)
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







