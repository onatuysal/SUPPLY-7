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
import com.example.supply7.R
import com.example.supply7.databinding.FragmentEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    private var selectedImageUri: Uri? = null

    private val pickImageFromGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    selectedImageUri = uri
                    binding.imageProfile.setImageURI(uri)
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEditProfileBinding.bind(view)

        val user = auth.currentUser
        if (user == null) {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, WelcomeFragment())
                .commit()
            return
        }

        binding.textEmailValue.text = user.email ?: ""

        db.collection("users").document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                binding.editDisplayName.setText(doc.getString("displayName") ?: (user.displayName ?: ""))
                binding.editFaculty.setText(doc.getString("faculty") ?: "")
                binding.editDepartment.setText(doc.getString("department") ?: "")
            }

        binding.btnSelectPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            pickImageFromGallery.launch(intent)
        }

        binding.btnSave.setOnClickListener {
            val displayName = binding.editDisplayName.text.toString().trim()
            val faculty = binding.editFaculty.text.toString().trim()
            val department = binding.editDepartment.text.toString().trim()

            if (displayName.isBlank()) {
                Toast.makeText(requireContext(), "Display Name required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()

            user.updateProfile(profileUpdates)
                .addOnCompleteListener { updateTask ->
                    if (!updateTask.isSuccessful) {
                        Toast.makeText(requireContext(), "Name update failed", Toast.LENGTH_SHORT).show()
                        return@addOnCompleteListener
                    }

                    val data = hashMapOf(
                        "displayName" to displayName,
                        "faculty" to faculty,
                        "department" to department,
                        "email" to (user.email ?: "")
                    )

                    db.collection("users").document(user.uid)
                        .set(data)
                        .addOnSuccessListener {
                            auth.currentUser?.reload()
                            Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                            parentFragmentManager.popBackStack()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), e.message ?: "Error", Toast.LENGTH_LONG).show()
                        }
                }
        }

        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
