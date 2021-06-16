/*
 * Copyright 2019 Abdelrhman Sror. All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.example.mediaplayer

import android.os.Bundle
import android.util.Log
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
import com.example.mediaplayer.permissions.AudioPermission


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
            setUpBottomNavAppearance()

            setupActionBarWithNavController(navController, appBarConfiguration)
            //to disable the action bar title and use my own custom title.
            // this.disableActionBarTitle()
            binding.bottomNavView.setupWithNavController(navController)
            //setUpPlayListBottomSheet()
            //to control the appearanve of bottom nav

            // navigateToStartDestination()
        }
        audioPermission.checkPermission()


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
                    Log.v("navigationchanged", "visible")
                    supportActionBar?.show()

                    visibility = View.VISIBLE

                }
            } else {
                with(binding.bottomNavView)
                {
                    Log.v("navigationchanged", "invisible")
                    supportActionBar?.hide()

                    visibility = View.GONE

                }
            }
        }
    }

    /* private fun navigateToStartDestination() {
         val inflater = navController.navInflater
         val graph = inflater.inflate(R.navigation.navigaion)
         //show the bottom nav view after permission is granted
         graph.startDestination = R.id.playListFragment
         navController.setGraph(graph, null)
     }*/


    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        audioPermission.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


}

