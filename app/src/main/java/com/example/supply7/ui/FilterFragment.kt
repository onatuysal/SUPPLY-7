package com.example.supply7.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.supply7.R
import com.example.supply7.databinding.FragmentFilterBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

// Using BottomSheet for nicer effect or standard Fragment? design looks like full screen dialog.
// Let's use standard Fragment for simplicity in navigation stack, 
// or maybe DialogFragment. The design has "X" top left, looks like a modal.
// Stick to Fragment for consistency with replaceFragment used so far.

class FilterFragment : Fragment(R.layout.fragment_filter) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentFilterBinding.bind(view)

        binding.btnClose.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnReset.setOnClickListener {
            binding.sliderPrice.values = listOf(0.0f, 1000.0f)
            binding.radioGroupCondition.clearCheck()
            binding.switchDiscounted.isChecked = false
            binding.switchTopSeller.isChecked = false
        }

        binding.sliderPrice.addOnChangeListener { slider, value, fromUser ->
            val values = slider.values
            binding.textMinPrice.text = "₺${values[0].toInt()}"
            binding.textMaxPrice.text = "₺${values[1].toInt()}"
        }

        binding.btnApplyFilters.setOnClickListener {
            val minPrice = binding.sliderPrice.values[0].toDouble()
            val maxPrice = binding.sliderPrice.values[1].toDouble()
            
            val selectedConditionId = binding.radioGroupCondition.checkedRadioButtonId
            val condition = when (selectedConditionId) {
                R.id.radioNew -> "New"
                R.id.radioLikeNew -> "Like-New"
                R.id.radioUsed -> "Used-Good"
                else -> null
            }

            val bundle = Bundle().apply {
                putDouble("minPrice", minPrice)
                putDouble("maxPrice", maxPrice)
                putString("condition", condition)
                // category logic pending UI expansion
            }
            
            parentFragmentManager.setFragmentResult("requestKeyFilters", bundle)
            parentFragmentManager.popBackStack()
        }
    }
}
