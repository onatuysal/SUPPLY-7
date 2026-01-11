package com.example.supply7.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.supply7.R
import com.example.supply7.databinding.FragmentSimplePageBinding

class HelpSupportFragment : Fragment(R.layout.fragment_simple_page) {

    private var _binding: FragmentSimplePageBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSimplePageBinding.bind(view)

        binding.txtTitle.text = "Help & Support"
        binding.txtBody.text = "Demo Support Page\n\n• FAQ: Coming soon\n• Contact: support@supply7.app"

        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
