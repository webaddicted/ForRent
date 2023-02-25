package com.webaddicted.forrent.base

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.webaddicted.forrent.R
import com.webaddicted.forrent.global.GlobalUtility
import com.webaddicted.forrent.global.NetworkChangeReceiver
import com.webaddicted.forrent.global.isNetworkAvailable

abstract class BaseActivity(private val layoutId: Int?) : AppCompatActivity(), View.OnClickListener {

    companion object {
        val TAG = BaseActivity::class.java.simpleName
    }

    abstract fun onBindTo(binding: ViewDataBinding)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out)
        supportActionBar?.hide()
//        setNavigationColor(resources.getColor(R.color.app_color))
        fullScreen()
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_SECURE,
//            WindowManager.LayoutParams.FLAG_SECURE
//        )
        GlobalUtility.hideKeyboard(this)
        val binding: ViewDataBinding?
        if (layoutId != 0) {
            try {
                binding = layoutId?.let { DataBindingUtil.setContentView(this, it) }
                binding?.let { onBindTo(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        getNetworkStateReceiver()
    }

    private fun fullScreen() {
        val window = window
        if (window != null) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    protected fun setNavigationColor(color: Int) {
        window?.navigationBarColor = color
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out)
    }

    override fun onClick(v: View) {
        GlobalUtility.avoidDoubleClicks(v)
    }

    override fun onResume() {
        super.onResume()
        if (!isNetworkAvailable()) GlobalUtility.initSnackBar(this@BaseActivity, false)
    }

    /**
     * broadcast receiver for check internet connectivity
     *
     * @return
     */
    private fun getNetworkStateReceiver() {
        NetworkChangeReceiver.isInternetAvailable(object :
            NetworkChangeReceiver.ConnectivityReceiverListener {
            override fun onNetworkConnectionChanged(networkConnected: Boolean) {
                try {
                    GlobalUtility.initSnackBar(this@BaseActivity, networkConnected)
                } catch (exception: Exception) {
                    Log.d(TAG, "getNetworkStateReceiver : $exception")
                }
            }
        })
    }


    fun navigateFragment(layoutContainer: Int, fragment: Fragment, isEnableBackStack: Boolean) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
//        fragmentTransaction.setCustomAnimations(R.anim.trans_left_in, R.anim.trans_left_out, R.anim.trans_right_in, R.anim.trans_right_out)
//        fragmentTransaction.setCustomAnimations(
//            R.animator.fragment_slide_left_enter,
//            R.animator.fragment_slide_left_exit,
//            R.animator.fragment_slide_right_enter,
//            R.animator.fragment_slide_right_exit
//        )
        fragmentTransaction.replace(layoutContainer, fragment)
        if (isEnableBackStack) fragmentTransaction.addToBackStack(fragment.javaClass.simpleName)
        fragmentTransaction.commitAllowingStateLoss()

    }

    fun navigateAddFragment(layoutContainer: Int, fragment: Fragment, isEnableBackStack: Boolean) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(
            R.anim.trans_left_in,
            R.anim.trans_left_out,
            R.anim.trans_right_in,
            R.anim.trans_right_out
        )
//        fragmentTransaction.setCustomAnimations(
//            R.animator.fragment_slide_left_enter,
//            R.animator.fragment_slide_left_exit,
//            R.animator.fragment_slide_right_enter,
//            R.animator.fragment_slide_right_exit
//        )
        fragmentTransaction.add(layoutContainer, fragment)
        if (isEnableBackStack) fragmentTransaction.addToBackStack(fragment.javaClass.simpleName)
        fragmentTransaction.commitAllowingStateLoss()
    }
}
