package com.example.supply7.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.supply7.R
import com.example.supply7.databinding.FragmentAddProductBinding
import com.example.supply7.viewmodel.AddProductViewModel

class AddProductFragment : Fragment(R.layout.fragment_add_product) {

    private val viewModel: AddProductViewModel by viewModels()
    private var binding: FragmentAddProductBinding? = null
    private var selectedImageUri: Uri? = null


    private val pickImageFromGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    selectedImageUri = uri
                    binding?.imagePreview?.setImageURI(uri)
                }
            }
        }

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && selectedImageUri != null) {
                binding?.imagePreview?.setImageURI(selectedImageUri)
            }
        }

    private fun createImageUri(): Uri? {
        val imageFile = java.io.File(
            requireContext().getExternalFilesDir(null),
            "camera_photo_${System.currentTimeMillis()}.jpg"
        )
        return androidx.core.content.FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            imageFile
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bind = FragmentAddProductBinding.bind(view)
        binding = bind


        bind.btnAddFromGallery.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            intent.type = "image/*"
            pickImageFromGallery.launch(intent)
        }


        bind.btnAddFromCamera.setOnClickListener {
            val uri = createImageUri()
            if (uri != null) {
                selectedImageUri = uri
                takePicture.launch(uri)
            } else {
                Toast.makeText(context, getString(R.string.msg_error_image), Toast.LENGTH_SHORT).show()

            }
        }
        bind.btnSubmit.setOnClickListener {
            Toast.makeText(requireContext(), "Button clicked", Toast.LENGTH_SHORT).show()
        }
        bind.btnSubmit.setOnClickListener {
            val title = bind.editTextTitle.text.toString()
            val priceStr = bind.editTextPrice.text.toString()
            val description = bind.editTextDescription.text.toString()
            val faculty = bind.editFaculty.text.toString()
            val category = bind.editCategory.text.toString()
            val department = bind.editDepartment.text.toString()
            val brand = bind.editBrand.text.toString()
            val color = bind.editColor.text.toString()
            val condition = bind.editCondition.text.toString()

            if (title.isBlank() || priceStr.isBlank()) {
                Toast.makeText(context, getString(R.string.msg_fill_required), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = priceStr.toDoubleOrNull() ?: 0.0
            viewModel.addProduct(
                title,
                description,
                price,
                faculty,
                category,
                department,
                brand,
                color,
                condition,
                selectedImageUri
            )
        }

        viewModel.uploadStatus.observe(viewLifecycleOwner) { result ->
            if (result.isSuccess) {
                Toast.makeText(context, getString(R.string.msg_product_posted), Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } else {
                Toast.makeText(
                    context,
                    "Error: ${result.exceptionOrNull()?.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            bind.btnSubmit.isEnabled = !isLoading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

