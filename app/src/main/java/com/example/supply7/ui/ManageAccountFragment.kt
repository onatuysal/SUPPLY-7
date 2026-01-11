package com.example.supply7.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.supply7.R
import com.example.supply7.databinding.FragmentSimplePageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ManageAccountFragment : Fragment(R.layout.fragment_simple_page) {

    private var _binding: FragmentSimplePageBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSimplePageBinding.bind(view)

        binding.txtTitle.text = "Manage Account"
        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        val user = auth.currentUser
        if (user == null) {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, WelcomeFragment())
                .commit()
            return
        }

        val name = user.displayName ?: user.email?.substringBefore("@") ?: "User"
        val email = user.email ?: "Unknown"
        val uid = user.uid

        binding.txtBody.text =
            "Account information:\n\n" +
                    "Name: $name\n" +
                    "Email: $email\n" +
                    "UID: $uid\n\n" +
                    "Actions:\n• Log out\n• Delete account (demo)"

        // Primary button = Delete Account
        binding.btnPrimary.visibility = View.VISIBLE
        binding.btnPrimary.text = "Delete Account"
        binding.btnPrimary.setOnClickListener {
            showDeleteDialog(uid)
        }

        // Long-press on title to logout (küçük kısa yol)
        binding.txtTitle.setOnLongClickListener {
            logoutToWelcome()
            true
        }
    }

    private fun showDeleteDialog(uid: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete account?")
            .setMessage("This will delete your user document from Firestore and attempt to delete your Auth account. This action cannot be undone.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                deleteAccount(uid)
            }
            .show()
    }

    private fun deleteAccount(uid: String) {
        val user = auth.currentUser ?: return

        // 1) Firestore user doc sil (demo)
        db.collection("users").document(uid)
            .delete()
            .addOnSuccessListener {
                // 2) Auth user delete dene
                user.delete()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Account deleted", Toast.LENGTH_SHORT).show()
                        goWelcome()
                    }
                    .addOnFailureListener { e ->
                        // Genelde: "Recent login required"
                        Toast.makeText(requireContext(), e.message ?: "Delete failed", Toast.LENGTH_LONG).show()
                        Toast.makeText(requireContext(), "If it says 'recent login required', log in again and retry.", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), e.message ?: "Firestore delete failed", Toast.LENGTH_LONG).show()
            }
    }

    private fun logoutToWelcome() {
        auth.signOut()
        Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
        goWelcome()
    }

    private fun goWelcome() {
        parentFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, WelcomeFragment())
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
