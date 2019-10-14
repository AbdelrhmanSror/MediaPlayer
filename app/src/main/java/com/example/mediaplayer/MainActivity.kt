package com.example.mediaplayer

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.mediaplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var bundle: Bundle? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        //reference to nav host fragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        ////reference to nav controller
        navController = navHostFragment.navController
        val inflater = navController.navInflater
        val graph = inflater.inflate(R.navigation.navigaion)
        bundle = intent.extras
        if (bundle?.getString(FRAGMENT_PURPOSE) == PlayerActions.AUDIO_FOREGROUND_NOTIFICATION.value) {
            graph.startDestination = R.id.chosenSong_dest

        } else {
            graph.startDestination = R.id.playList_dest
        }
        navHostFragment.navController.setGraph(graph, bundle)

        appBarConfiguration = AppBarConfiguration.Builder(setOf(R.id.playList_dest, R.id.favourite_dest)).build()
        //reference to toolBar
        val toolbar = binding.toolbar
        //set toolbar as default action bar
        setSupportActionBar(toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
        setupBottomNavMenu()
        setUpBottomNavAppearance()

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        if (bundle?.getString(FRAGMENT_PURPOSE) == PlayerActions.AUDIO_FOREGROUND_NOTIFICATION.value) {
            navController.navigate(R.id.action_chosenSongFragment_to_playListFragment, null)
            return true
        }
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun setupBottomNavMenu() {
        val bottomNav = binding.bottomNavView
        bottomNav.setupWithNavController(navController)
    }

    //preventing bottom navigation  from showing anywhere other than the main destinations
    private fun setUpBottomNavAppearance() {
        navController.addOnDestinationChangedListener { _, nd: NavDestination, _ ->
            if (nd.id == R.id.playList_dest || nd.id == R.id.favourite_dest) {
                with(binding.bottomNavView)
                {
                    visibility = View.VISIBLE
                    animate()
                            .alpha(1f)
                }
            } else {
                with(binding.bottomNavView)
                {
                    visibility = View.GONE
                    animate()
                            .alpha(1f)
                }
            }
        }
    }
}

