package com.example.supply7.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.supply7.R
import com.example.supply7.databinding.FragmentSuccessBinding

class SuccessFragment : Fragment(R.layout.fragment_success) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bind = FragmentSuccessBinding.bind(view)

        bind.btnHome.setOnClickListener {
             // Go home, clear backstack
             parentFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
             
             // Or navigate to Home specifically
             (activity as? MainActivity)?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)?.selectedItemId = R.id.nav_home
             // Fragment replacement for home is handled by bottom nav listener usually, or we do manual:
             // If we just popped inclusive, we might be at empty container if root wasn't home.
             // Safer:
             parentFragmentManager.beginTransaction()
                 .replace(R.id.fragment_container, HomeFragment())
                 .commit()
        }
    }
}
