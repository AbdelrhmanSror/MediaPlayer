package com.example.mediaplayer

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.mediaplayer.databinding.ActivityMainBinding
import com.example.mediaplayer.extensions.disableActionBarTitle
import com.example.mediaplayer.permissions.AudioPermission
import com.google.android.material.bottomsheet.BottomSheetBehavior


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var audioPermission: AudioPermission


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        audioPermission = AudioPermission(this) {
            navController = findNavController(R.id.nav_host_fragment)
            //reference to nav controller
            appBarConfiguration = AppBarConfiguration.Builder(setOf(R.id.playListFragment, R.id.favouriteFragment)).build()
            //reference to toolBar
            val toolbar = binding.toolbar
            //set toolbar as default action bar
            setSupportActionBar(toolbar)
            setupActionBarWithNavController(navController, appBarConfiguration)
            //to disable the action bar title and use my own custom title.
            this.disableActionBarTitle()
            binding.bottomNavView.setupWithNavController(navController)
            setUpPlayListBottomSheet()
            //to control the appearanve of bottom nav
            setUpBottomNavAppearance()

            navigateToStartDestination()
        }
        audioPermission.checkPermission()


    }

    private fun setUpPlayListBottomSheet() {
        binding.bottomSheetLayout.visibility = View.GONE
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetLayout)
        binding.bottomSheetLayout.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

    }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    //preventing bottom navigation  from showing anywhere other than the main destinations
    private fun setUpBottomNavAppearance() {
        navController.addOnDestinationChangedListener { _, nd: NavDestination, _ ->
            if (nd.id == R.id.playListFragment || nd.id == R.id.favouriteFragment) {
                with(binding.bottomNavView)
                {
                    //visibility = View.VISIBLE
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

    private fun navigateToStartDestination() {
        val inflater = navController.navInflater
        val graph = inflater.inflate(R.navigation.navigaion)
        //show the bottom nav view after permission is granted
        graph.startDestination = R.id.playListFragment
        navController.setGraph(graph, null)
    }


    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        audioPermission.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


}

