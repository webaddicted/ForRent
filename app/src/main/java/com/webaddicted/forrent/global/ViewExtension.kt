package com.webaddicted.forrent.global

import android.content.Context
import android.net.ConnectivityManager
import android.view.View
import android.widget.Toast
import com.webaddicted.forrent.R

fun View.visible() {
    visibility = View.VISIBLE
}
fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun Context.isNetworkAvailable(): Boolean {
    val connMgr =
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connMgr.activeNetworkInfo
    return activeNetwork != null && activeNetwork.isAvailable && activeNetwork.isConnected
}

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

/**
 * show internet connection toast
 */
fun Context.showNoNetworkToast() {
    showToast(resources.getString(R.string.no_network_msg))
}

/**
 * show internet connection toast
 */
fun Context.showSomethingWrongToast() {
    showToast(resources.getString(R.string.something_went_wrong))
}

