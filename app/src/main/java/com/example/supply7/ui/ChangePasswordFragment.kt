package com.example.supply7.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.supply7.R
import com.example.supply7.databinding.FragmentChangePasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordFragment : Fragment(R.layout.fragment_change_password) {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChangePasswordBinding.bind(view)

        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        binding.btnSave.setOnClickListener {
            val user = auth.currentUser
            if (user == null) {
                Toast.makeText(requireContext(), "Not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val email = user.email
            if (email.isNullOrBlank()) {
                Toast.makeText(requireContext(), "Email not found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentPw = binding.edtCurrentPassword.text.toString()
            val newPw = binding.edtNewPassword.text.toString()
            val confirmPw = binding.edtConfirmPassword.text.toString()

            if (currentPw.isBlank() || newPw.isBlank() || confirmPw.isBlank()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPw.length < 6) {
                Toast.makeText(requireContext(), "New password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPw != confirmPw) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Re-authenticate then update password
            val credential = EmailAuthProvider.getCredential(email, currentPw)
            user.reauthenticate(credential)
                .addOnSuccessListener {
                    user.updatePassword(newPw)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Password updated", Toast.LENGTH_SHORT).show()
                            parentFragmentManager.popBackStack()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), e.message ?: "Update failed", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Current password is incorrect", Toast.LENGTH_LONG).show()
                }
        }

        binding.btnSendResetEmail.setOnClickListener {
            val email = auth.currentUser?.email
            if (email.isNullOrBlank()) {
                Toast.makeText(requireContext(), "Email not found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Reset email sent", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), e.message ?: "Error", Toast.LENGTH_LONG).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
