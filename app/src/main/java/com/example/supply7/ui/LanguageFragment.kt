package com.example.supply7.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.supply7.R
import com.example.supply7.databinding.FragmentLanguageBinding
import com.example.supply7.util.LocaleHelper

class LanguageFragment : Fragment(R.layout.fragment_language) {

    private var _binding: FragmentLanguageBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLanguageBinding.bind(view)

        val currentLang = LocaleHelper.getLanguage(requireContext())
        updateSelection(currentLang)

        binding.containerEnglish.setOnClickListener {
            handleLanguageSelection("en")
        }

        binding.containerTurkish.setOnClickListener {
            handleLanguageSelection("tr")
        }

        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
    }

    private fun handleLanguageSelection(langCode: String) {
        val currentLang = LocaleHelper.getLanguage(requireContext())
        if (currentLang == langCode) return

        LocaleHelper.setLocale(requireContext(), langCode)
        
        // Restart activity to apply language changes
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun updateSelection(langCode: String) {
        val isEnglish = langCode == "en"
        
        binding.imgCheckEnglish.setImageResource(
            if (isEnglish) R.drawable.ic_check_circle_active else R.drawable.ic_check_circle_inactive
        )
        binding.imgCheckTurkish.setImageResource(
            if (!isEnglish) R.drawable.ic_check_circle_active else R.drawable.ic_check_circle_inactive
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
