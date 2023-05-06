package it.macgood.core.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo

fun Activity.isConnectedToInternet(): Boolean {
    val connectivityManager =
        applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (connectivityManager != null) {
        val info = connectivityManager.allNetworkInfo
        if (info != null) {
            for (networkInfo in info) {
                if (networkInfo.state == NetworkInfo.State.CONNECTED) {
                    return true
                }
            }
        }
    }
    return false
}

fun Activity.restartApp(intent: Intent) {
    finish()
    startActivity(intent)
    overridePendingTransition(0,0)
}
