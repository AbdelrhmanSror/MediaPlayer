package com.example.mediaplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mediaplayer.databinding.ActivityMainBinding
import com.example.mediaplayer.ui.favourite.FavouriteFragment
import com.example.mediaplayer.ui.playlist.PlayListFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        ////reference to nav controller
        navController = findNavController(R.id.nav_host_fragment)
        checkPermission()
        appBarConfiguration = AppBarConfiguration.Builder(setOf(R.id.placeholderFragment)).build()
        //reference to toolBar
        val toolbar = binding.toolbar
        //set toolbar as default action bar
        setSupportActionBar(toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
        //to disable the action bar title and use my own custom title.
        this.disableActionBarTitle()


    }

    companion object {
        const val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 5
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }



    private fun checkPermission() {
        // Here, thisActivity is the current activity
        if (!application.isAudioFilesPermissionGranted()) {
            requestPermission()
        } else {
            navigateToStartDestination()
        }

    }

    private fun navigateToStartDestination() {
        val inflater = navController.navInflater
        val graph = inflater.inflate(R.navigation.navigaion)
        //show the bottom nav view after permission is granted
       // binding.bottomNavView.visibility = View.VISIBLE
        graph.startDestination = R.id.placeholderFragment
        navController.setGraph(graph, null)
    }

    private fun requestPermission() = ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
    )

    //show snack bar to tell user to direct user to settings to enable permission
    private fun showPermissionEnableSnackBar() {
        Snackbar.make(
                binding.coordinator,
                getString(R.string.enable_permission_settings),
                Snackbar.LENGTH_INDEFINITE
        ).setAction(getString(R.string.go_to_settings)) {
            val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + BuildConfig.APPLICATION_ID)
            )
            startActivity(intent)

        }.show()
    }

    private fun showPermissionAlertDialog() {
        MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.permission_denied))
                .setMessage(getString(R.string.permission_clarify))
                .setPositiveButton(getString(R.string.accept_permission)) { _, _ ->
                    requestPermission()
                }.setNegativeButton(getString(R.string.refuse_permission)) { dialog, _ ->
                    dialog.cancel()
                }
                .show()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        //if the permission is granted but gps is not enabled then ask user to enable it
        //else if the use did not enable it then go to default location
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                navigateToStartDestination()
            } else {
                // permission was not granted
                //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                // shouldShowRequestPermissionRationale will return true
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showPermissionAlertDialog()

                } //permission is denied (and never ask again is  checked)
                //shouldShowRequestPermissionRationale will return false
                else {
                    showPermissionEnableSnackBar()
                }
            }
        }
    }


}

