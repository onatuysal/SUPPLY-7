package com.example.supply7.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.supply7.R
import com.example.supply7.databinding.FragmentDeliverySelectionBinding

class DeliverySelectionFragment : Fragment(R.layout.fragment_delivery_selection) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentDeliverySelectionBinding.bind(view)

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnConfirm.setOnClickListener {
            val selectedId = binding.radioGroupLocations.checkedRadioButtonId
            val selectedLocation = when (selectedId) {
                R.id.radioMeydan -> "Meydan"
                R.id.radioRektorluk -> "Rektörlük"
                R.id.radioSosyal -> "Sosyal Tesisler"
                R.id.radioGuzelSanatlar -> "Güzel Sanatlar Fakültesi"
                else -> null
            }

            if (selectedLocation != null) {
                parentFragmentManager.setFragmentResult("requestKeyDelivery", Bundle().apply {
                    putString("location", selectedLocation)
                })
                parentFragmentManager.popBackStack()
            }
        }
    }
}
