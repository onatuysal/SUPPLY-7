package com.example.supply7.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.supply7.R
import com.example.supply7.databinding.FragmentSimplePageBinding

class LanguageFragment : Fragment(R.layout.fragment_simple_page) {

    private var _binding: FragmentSimplePageBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSimplePageBinding.bind(view)

        binding.txtTitle.text = "Language"

        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val current = prefs.getString("language", "English") ?: "English"

        binding.txtBody.text = "Current language: $current\n\nThis is a demo setting."

        binding.btnPrimary.visibility = View.VISIBLE
        binding.btnPrimary.text = if (current == "English") "Switch to Turkish" else "Switch to English"
        binding.btnPrimary.setOnClickListener {
            val newLang = if (current == "English") "Turkish" else "English"
            prefs.edit().putString("language", newLang).apply()
            parentFragmentManager.popBackStack()
        }

        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
