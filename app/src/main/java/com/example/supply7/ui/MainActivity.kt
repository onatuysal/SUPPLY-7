package com.example.supply7.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.supply7.R
import com.example.supply7.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNav = binding.bottomNavigation

        // Listen for navigation item selection
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.nav_favorites -> {
                    replaceFragment(FavoritesFragment())
                    true
                }
                R.id.nav_add -> {
                    replaceFragment(AddProductFragment())
                    true
                }
                R.id.nav_messages -> {
                    replaceFragment(MessagesFragment())
                    true
                }
                R.id.nav_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }

        // Initial Fragment
        if (savedInstanceState == null) {
            // Check if user is logged in (Usually handled in Splash or via LoginFragment check)
            // For now, we start with LoginFragment which handles its own logic.
            // If we want BottomNav to be visible only after login, we can control visibility.
            
            // Assuming Login is the entry point, we might hide BottomNav initially
            bottomNav.visibility = View.GONE
            
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, WelcomeFragment())
                .commit()
        }
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun showBottomNav(show: Boolean) {
        binding.bottomNavigation.visibility = if (show) View.VISIBLE else View.GONE
    }
}
