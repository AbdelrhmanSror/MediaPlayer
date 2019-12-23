package com.example.mediaplayer.permissions

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mediaplayer.BuildConfig
import com.example.mediaplayer.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class AudioPermission(private val activityCompat: AppCompatActivity
                      , private val onPermissionGranted: () -> Unit) {


    companion object {
        /**
         * for case when user rotate the phone and permissions was requested to avoid request it again
         */
        private var isPermissionRequested = false
        const val MY_PERMISSIONS_REQUEST = 5
    }


    fun checkPermission() {
        if (!isPermissionRequested) {
            if (!activityCompat.application.isPermissionGranted()) {
                requestPermission()
            } else {
                onPermissionGranted()
            }
        }

    }

    /**
     * extension function to check if user granted the permissions
     */
    private fun Application.isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH
                ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        isPermissionRequested = true
        ActivityCompat.requestPermissions(activityCompat,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.BLUETOOTH, Manifest.permission.RECORD_AUDIO),
                MY_PERMISSIONS_REQUEST
        )

    }

    //show snack bar to tell user to direct user to settings to enable permission
    private fun showPermissionEnableSnackBar() {
        Snackbar.make(
                activityCompat.window.decorView.findViewById(android.R.id.content),
                activityCompat.getString(R.string.enable_permission_settings),
                Snackbar.LENGTH_INDEFINITE
        ).setAction(activityCompat.getString(R.string.go_to_settings)) {
            val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + BuildConfig.APPLICATION_ID)
            )
            activityCompat.startActivity(intent)

        }.show()
    }

    private fun showPermissionAlertDialog() {
        MaterialAlertDialogBuilder(activityCompat)
                .setTitle(activityCompat.getString(R.string.permission_denied))
                .setMessage(activityCompat.getString(R.string.permission_clarify))
                .setPositiveButton(activityCompat.getString(R.string.accept_permission)) { _, _ ->
                    requestPermission()
                }.setNegativeButton(activityCompat.getString(R.string.refuse_permission)) { dialog, _ ->
                    dialog.cancel()
                    activityCompat.finish()
                }
                .show()
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        //if the permission is granted but gps is not enabled then ask user to enable it
        //else if the use did not enable it then go to default location
        if (requestCode == MY_PERMISSIONS_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED && grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                isPermissionRequested = false
                onPermissionGranted()
            } else {
                // permission was not granted
                //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                // shouldShowRequestPermissionRationale will return true
                if (ActivityCompat.shouldShowRequestPermissionRationale(activityCompat, Manifest.permission.READ_EXTERNAL_STORAGE)
                        || ActivityCompat.shouldShowRequestPermissionRationale(activityCompat, Manifest.permission.BLUETOOTH)
                        || ActivityCompat.shouldShowRequestPermissionRationale(activityCompat, Manifest.permission.RECORD_AUDIO) || ActivityCompat.shouldShowRequestPermissionRationale(activityCompat, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
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