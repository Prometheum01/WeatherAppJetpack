package com.rurouni.weatherapp.ui.view

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority

import com.rurouni.weatherapp.databinding.ActivityMainBinding
import com.rurouni.weatherapp.service.location.LocationPreferences
import com.rurouni.weatherapp.ui.permission.PermissionHandler
import com.rurouni.weatherapp.ui.view.components.CustomSnackbar
import com.rurouni.weatherapp.ui.view.components.Messages
import com.rurouni.weatherapp.ui.view_model.HomeViewModel
import com.rurouni.weatherapp.ui.view_model.LocationViewModel
import com.rurouni.weatherapp.utils.Utils.toApiFormat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @Inject lateinit var locationPreferences: LocationPreferences

    private val locationViewModel: LocationViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    private lateinit var permissionHandler: PermissionHandler

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                locationViewModel.openLocationService(this)
            } else {
                Messages.noLocationPermissionMessage(this)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setStatusBarTheme()

        checkPermission()
        observeLocationAndForecast()
    }

    //This functions checks the location permission
    private fun checkPermission() {
        permissionHandler = PermissionHandler(this)

        permissionHandler.checkPermission(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            requestPermissionLauncher,
            onRationale = {
                CustomSnackbar.showActionSnackbar(binding.root, "We have to know your location to show weather data for you", "Give Permission", action = {
                    requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                })
            },
            onPermissionGranted = {
                locationViewModel.openLocationService(this)
            }
        )
    }

    //This functions observe the location data from location view model and if it get any data then call getForecast function
    private fun observeLocationAndForecast() {
        try {
            locationViewModel.currentLocation.observe(this) { location ->
                getForecast(location.toApiFormat())
            }
        } catch (e: Exception) {
            e.stackTrace
        }
    }

    //This functions get forecast data from home view model
    private fun getForecast(location: String) {
        try {
            homeViewModel.getForecast(location)
        } catch (e: Exception) {
            e.stackTrace
        }
    }

    //This function gets the result of open location dialog
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == locationViewModel.REQUEST_CHECK_SETTINGS && resultCode == Activity.RESULT_OK) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000)
                .setMaxUpdateDelayMillis(10000)
                .build()
            locationViewModel.getCurrentLocation(locationRequest)
        } else {
            if (locationPreferences.getSavedLocation() != null) {
                getForecast(locationPreferences.getSavedLocation()!!.toApiFormat())
            } else {
                Messages.openLocationMessage(binding.root) {
                    locationViewModel.openLocationService(this)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationViewModel.stopLocationUpdates()
    }

    //This function sets transparent status bar
    private fun setStatusBarTheme() {
        window.apply {
            decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            statusBarColor = Color.TRANSPARENT
        }

        val isDarkTheme =
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        if (isDarkTheme) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        } else {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
}