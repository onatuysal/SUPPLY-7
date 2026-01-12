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
                Toast.makeText(requireContext(), getString(R.string.msg_not_logged_in), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val email = user.email
            if (email.isNullOrBlank()) {
                Toast.makeText(requireContext(), getString(R.string.msg_email_not_found), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentPw = binding.edtCurrentPassword.text.toString()
            val newPw = binding.edtNewPassword.text.toString()
            val confirmPw = binding.edtConfirmPassword.text.toString()

            if (currentPw.isBlank() || newPw.isBlank() || confirmPw.isBlank()) {
                Toast.makeText(requireContext(), getString(R.string.msg_fill_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPw.length < 6) {
                Toast.makeText(requireContext(), getString(R.string.msg_password_min_length), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPw != confirmPw) {
                Toast.makeText(requireContext(), getString(R.string.msg_password_mismatch), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Re-authenticate then update password
            val credential = EmailAuthProvider.getCredential(email, currentPw)
            user.reauthenticate(credential)
                .addOnSuccessListener {
                    user.updatePassword(newPw)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), getString(R.string.msg_update_success), Toast.LENGTH_SHORT).show()
                            parentFragmentManager.popBackStack()
                        }
                        .addOnFailureListener { e ->
                            val fallback = getString(R.string.msg_update_failed)
                            Toast.makeText(requireContext(), e.message ?: fallback, Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), getString(R.string.msg_error_password_incorrect), Toast.LENGTH_LONG).show()
                }
        }

        binding.btnSendResetEmail.setOnClickListener {
            val email = auth.currentUser?.email
            if (email.isNullOrBlank()) {
                Toast.makeText(requireContext(), getString(R.string.msg_email_not_found), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), getString(R.string.msg_reset_email_sent), Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    val fallback = getString(R.string.msg_error_generic, "")
                    Toast.makeText(requireContext(), e.message ?: fallback, Toast.LENGTH_LONG).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
