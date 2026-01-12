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

        // Selection Logic Variables
        var selectedDepartment = "All"
        var selectedBrand = "All"
        var selectedCity = "All"

        binding.textDepartment.setOnClickListener {
            val items = arrayOf(
                getString(R.string.filter_all),
                "Mühendislik",
                "Mimarlık",
                "Tıp",
                "Sanat",
                "İşletme"
            )
            showSelectionDialog(getString(R.string.filter_select_department), items) { selected ->
                selectedDepartment = selected
                binding.textDepartment.text = "${getString(R.string.label_department)}: $selected"
            }
        }

        binding.textBrand.setOnClickListener {
            val items = arrayOf(
                getString(R.string.filter_all),
                "Apple",
                "Samsung",
                "Nike",
                "Adidas",
                "Sony",
                "Zara"
            )
            showSelectionDialog(getString(R.string.filter_select_brand), items) { selected ->
                selectedBrand = selected
                binding.textBrand.text = "${getString(R.string.label_brand)}: $selected"
            }
        }

        binding.textCity.setOnClickListener {
            val items = arrayOf(
                getString(R.string.filter_all),
                "İstanbul",
                "Ankara",
                "İzmir",
                "Bursa",
                "Antalya"
            )
            showSelectionDialog(getString(R.string.filter_select_city), items) { selected ->
                selectedCity = selected
                binding.textCity.text = "${getString(R.string.filter_city).substringBefore(':')}: $selected"
            }
        }

        // Apply Filters butonu
        binding.btnApplyFilters.setOnClickListener {
            val minPrice = binding.sliderPrice.values[0].toDouble()
            val maxPrice = binding.sliderPrice.values[1].toDouble()

            val selectedConditionId = binding.radioGroupCondition.checkedRadioButtonId
            val condition = when (selectedConditionId) {
                R.id.radioNew -> getString(R.string.filter_condition_new)
                R.id.radioLikeNew -> getString(R.string.filter_condition_like_new)
                R.id.radioUsed -> getString(R.string.filter_condition_used)
                else -> null
            }

            val bundle = Bundle().apply {
                putDouble("minPrice", minPrice)
                putDouble("maxPrice", maxPrice)
                putString("condition", condition)
                putString("department", if (selectedDepartment != getString(R.string.filter_all)) selectedDepartment else null)
                putString("brand", if (selectedBrand != getString(R.string.filter_all)) selectedBrand else null)
                putString("city", if (selectedCity != getString(R.string.filter_all)) selectedCity else null)
            }

            parentFragmentManager.setFragmentResult("requestKeyFilters", bundle)
            parentFragmentManager.popBackStack()
        }
    }

    private fun showSelectionDialog(title: String, items: Array<String>, onSelected: (String) -> Unit) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setItems(items) { _, which ->
                onSelected(items[which])
            }
            .show()
    }
}

