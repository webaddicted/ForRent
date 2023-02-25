package com.webaddicted.forrent.global

import android.app.Application
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager

/**
 * Created by Deepak Sharma on 01/07/23.
 */
class AppApplication : Application() {
    private val mNetworkReceiver = NetworkChangeReceiver()

    companion object {
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        checkInternetConnection()
    }

    private fun checkInternetConnection() {
        registerReceiver(mNetworkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }
}
