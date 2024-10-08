package com.rurouni.weatherapp.utils

import android.app.AlertDialog
import android.content.Context
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.ContextCompat
import com.rurouni.weatherapp.R

object Utils {
    //This function checks internet connection
    fun hasInternetConnection(context: Context?): Boolean {
        try {
            if (context == null)
                return false
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                    as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } catch (e: Exception) {
            return false
        }
    }

    //This function shows alert dialog
    fun showAlertDialog(context: Context,message:String){
        try {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.app_name)
            builder.setMessage(message)
            builder.setIcon(android.R.drawable.ic_dialog_alert)
            builder.setPositiveButton("OK"){dialogInterface, which ->
                dialogInterface.dismiss()
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        } catch (e: Exception) {
            e.stackTrace
        }
    }

    //This function convert code to custom icon resource id
    fun codeToIconId(context: Context, code: Int): Int? {
        val resourceName = "ic_$code"
        val resourceId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)
        return if (resourceId != 0) resourceId else null
    }

    //This function gets the color from resource id
    fun getColor(context: Context, colorResId: Int): Int {
        return ContextCompat.getColor(context, colorResId)
    }

    //This function transform from location to api format
    fun Location.toApiFormat() : String {
        return "${latitude}, ${longitude}"
    }
}