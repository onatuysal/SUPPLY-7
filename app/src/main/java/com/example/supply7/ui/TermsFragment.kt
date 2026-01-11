package com.example.supply7.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.supply7.R
import com.example.supply7.databinding.FragmentSimplePageBinding

class TermsFragment : Fragment(R.layout.fragment_simple_page) {

    private var _binding: FragmentSimplePageBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSimplePageBinding.bind(view)

        binding.txtTitle.text = "Terms Of Service"
        binding.txtBody.text = "Demo Terms Summary:\n\n• No prohibited items\n• Respect community rules\n• Payments not enabled in demo"

        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
