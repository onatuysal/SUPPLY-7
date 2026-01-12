package com.example.supply7.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.supply7.R
import com.example.supply7.databinding.FragmentRegisterBinding
import com.example.supply7.viewmodel.AuthViewModel

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private val viewModel: AuthViewModel by viewModels()
    private var binding: FragmentRegisterBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bind = FragmentRegisterBinding.bind(view)
        binding = bind

        bind.buttonRegister.setOnClickListener {
            val name = bind.editName.text.toString()
            val email = bind.editEmail.text.toString()
            val password = bind.editPassword.text.toString()
            
            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                Toast.makeText(context, getString(R.string.msg_fill_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            viewModel.register(email, password, name)
        }

        // Password Toggle Logic
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

        bind.textGoToLogin.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                Toast.makeText(requireContext(), getString(R.string.msg_register_success), Toast.LENGTH_SHORT).show()
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
            bind.buttonRegister.isEnabled = !isLoading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
