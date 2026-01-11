package com.example.supply7.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.supply7.R
import com.example.supply7.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNav = binding.bottomNavigation

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment(), addToBackStack = false)
                    true
                }
                R.id.nav_favorites -> {
                    replaceFragment(FavoritesFragment(), addToBackStack = false)
                    true
                }
                R.id.nav_add -> {
                    replaceFragment(AddProductFragment(), addToBackStack = false)
                    true
                }
                R.id.nav_messages -> {
                    replaceFragment(MessagesFragment(), addToBackStack = false)
                    true
                }
                R.id.nav_profile -> {
                    replaceFragment(ProfileFragment(), addToBackStack = false)
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            // App entry
            bottomNav.visibility = View.GONE

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, WelcomeFragment())
                .commit()
        }
    }

    fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
    }

    fun showBottomNav(show: Boolean) {
        binding.bottomNavigation.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun setBottomNavToHome() {
        binding.bottomNavigation.selectedItemId = R.id.nav_home
    }
}

