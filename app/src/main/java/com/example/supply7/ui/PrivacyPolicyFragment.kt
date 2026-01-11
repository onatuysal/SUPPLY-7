package com.example.supply7.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.supply7.R
import com.example.supply7.databinding.FragmentPrivacyPolicyBinding

class PrivacyPolicyFragment : Fragment(R.layout.fragment_privacy_policy) {

    private var _binding: FragmentPrivacyPolicyBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPrivacyPolicyBinding.bind(view)

        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        binding.txtPolicy.text =
            "Introduction\n\n" +
                    "This Privacy Policy describes how we collect, use, and protect your personal information when you use our application. We are committed to protecting your privacy and ensuring that your personal information is handled responsibly.\n\n" +
                    "Data Collection\n\n" +
                    "We collect various types of information, including personal information that you provide directly, such as your name, email address, and contact details. We also collect information automatically, such as your device information, usage data, and location data.\n\n" +
                    "Data Usage\n\n" +
                    "We use your personal information to provide and improve our services, personalize your experience, communicate with you, and comply with legal obligations. We may also use your information for research and analytics purposes.\n\n" +
                    "Data Protection\n\n" +
                    "We implement appropriate security measures to protect your personal information from unauthorized access, alteration, disclosure, or destruction. These measures include encryption, access controls, and regular security assessments.\n\n" +
                    "Policy Changes\n\n" +
                    "We may update this Privacy Policy from time to time to reflect changes in our practices or for legal requirements. We will notify you of any significant changes and obtain your consent if required by applicable law. Please review this policy periodically for the latest information."
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



