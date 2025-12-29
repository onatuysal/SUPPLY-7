package com.example.supply7.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.supply7.R
import com.example.supply7.databinding.FragmentFilterBinding

class FilterFragment : Fragment(R.layout.fragment_filter) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentFilterBinding.bind(view)

        // Geri ok butonu
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Reset butonu: tüm filtreleri sıfırla
        binding.btnReset.setOnClickListener {
            // Fiyat aralığını başa al
            binding.sliderPrice.values = listOf(0.0f, 1000.0f)
            binding.textMinPrice.text = "₺0"
            binding.textMaxPrice.text = "₺1000"

            // Condition temizle
            binding.radioGroupCondition.clearCheck()

            // Switch’ler kapalı
            binding.switchDiscounted.isChecked = false
            binding.switchTopSeller.isChecked = false
        }

        // Slider değişince altındaki min/max textleri güncelle
        binding.sliderPrice.addOnChangeListener { slider, _, _ ->
            val values = slider.values
            binding.textMinPrice.text = "₺${values[0].toInt()}"
            binding.textMaxPrice.text = "₺${values[1].toInt()}"
        }

        // Apply Filters butonu
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

            // İleride kategori / fakülte vs eklersek buraya da koyarız
            val bundle = Bundle().apply {
                putDouble("minPrice", minPrice)
                putDouble("maxPrice", maxPrice)
                putString("condition", condition)
            }

            parentFragmentManager.setFragmentResult("requestKeyFilters", bundle)
            parentFragmentManager.popBackStack()
        }
    }
}

