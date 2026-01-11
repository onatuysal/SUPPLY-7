package com.example.supply7.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.supply7.R
import com.example.supply7.databinding.FragmentLoginBinding
import com.example.supply7.viewmodel.AuthViewModel

class LoginFragment : Fragment(R.layout.fragment_login) {

    private val viewModel: AuthViewModel by viewModels()
    private var binding: FragmentLoginBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bind = FragmentLoginBinding.bind(view)
        binding = bind

        bind.buttonLogin.setOnClickListener {
            val email = bind.editEmail.text.toString()
            val password = bind.editPassword.text.toString()
            if (email.isNotBlank() && password.isNotBlank()) {
                viewModel.login(email, password)
            }
        }

        // Password Toggle Logic (Previous Code)

        bind.btnForgotPassword.setOnClickListener {
            val email = bind.editEmail.text.toString()
            if (email.isBlank()) {
                Toast.makeText(context, "Please enter your email first", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.resetPassword(email, 
                    onSuccess = {
                        Toast.makeText(context, "Reset email sent! Check your inbox.", Toast.LENGTH_LONG).show()
                    },
                    onError = { msg ->
                        Toast.makeText(context, "Error: $msg", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
        bind.editPassword.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                val drawableRight = 2
                if (event.rawX >= (bind.editPassword.right - bind.editPassword.compoundDrawables[drawableRight].bounds.width())) {
                    val selection = bind.editPassword.selectionEnd
                    if (bind.editPassword.transformationMethod == android.text.method.PasswordTransformationMethod.getInstance()) {
                        bind.editPassword.transformationMethod = android.text.method.HideReturnsTransformationMethod.getInstance()
                    } else {
                        bind.editPassword.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
                    }
                    bind.editPassword.setSelection(selection)
                    return@setOnTouchListener true
                }
            }
            false
        }

        bind.textGoToRegister.setOnClickListener {
            // Navigate to Register Fragment
            // For simplicity in this non-Navigation Component setup:
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }

        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .commit()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            bind.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            bind.buttonLogin.isEnabled = !isLoading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
