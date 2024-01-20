package com.example.eperpus

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.eperpus.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var sidebarNavigation: NavigationView
    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            sidebarNavigation = findViewById(R.id.navigation_view)
            navigateToFragment(HomeFragment())
            sidebarNavigation.setNavigationItemSelectedListener  { menuItem ->
                when (menuItem.itemId) {
                    R.id.navigation_home -> navigateToFragment(HomeFragment())
                    R.id.navigation_riwayat -> navigateToFragment(RiwayatFragment())
                    R.id.navigation_akun -> navigateToFragment(ProfilFragment())
                }
                true
            }
        } else {
            bottomNavigation = findViewById(R.id.bottom_navigasi)
            navigateToFragment(HomeFragment())
            bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.navigation_home -> navigateToFragment(HomeFragment())
                    R.id.navigation_riwayat -> navigateToFragment(RiwayatFragment())
                    R.id.navigation_akun -> navigateToFragment(ProfilFragment())
                }
                true
            }
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

    }

    fun navigateToFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

}