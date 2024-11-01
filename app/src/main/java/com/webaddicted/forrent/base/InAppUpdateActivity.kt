package com.webaddicted.forrent.base

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.databinding.ViewDataBinding
import com.webaddicted.forrent.BuildConfig
import com.webaddicted.forrent.MainActivity
import com.webaddicted.forrent.R
import com.webaddicted.forrent.base.UpdateManager.FlexibleUpdateDownloadListener
import com.webaddicted.forrent.base.UpdateManager.UpdateInfoListener
import com.webaddicted.forrent.databinding.ActivityInAppUpdateBinding
import com.webaddicted.forrent.global.GlobalUtility


class InAppUpdateActivity : BaseActivity(R.layout.activity_in_app_update) {

    private lateinit var mBinding: ActivityInAppUpdateBinding
    var mUpdateManager: UpdateManager? = null
    var txtFlexibleUpdateProgress: TextView? = null

    companion object {
        val TAG: String = InAppUpdateActivity::class.java.simpleName
        fun newIntent(activity: Activity) {
            activity.startActivity(Intent(activity, InAppUpdateActivity::class.java))
        }
    }

    override fun onBindTo(binding: ViewDataBinding) {
        mBinding = binding as ActivityInAppUpdateBinding
        init()
        clickListener()
    }

    private fun init() {
        mBinding.txtMsg.text = "Build VERSION_CODE : ${BuildConfig.VERSION_CODE}"
        // Initialize the Update Manager with the Activity and the Update Mode
        // Initialize the Update Manager with the Activity and the Update Mode
        mUpdateManager = UpdateManager.Builder(this)

        // Callback from UpdateInfoListener
        // You can get the available version code of the apk in Google Play
        // Number of days passed since the user was notified of an update through the Google Play

        // Callback from UpdateInfoListener
        // You can get the available version code of the apk in Google Play
        // Number of days passed since the user was notified of an update through the Google Play
        mUpdateManager?.addUpdateInfoListener(object : UpdateInfoListener {
            override fun onReceiveVersionCode(code: Int) {
                mBinding.txtAvailableVersion.text = "Available Version : $code"
            }

            override fun onReceiveStalenessDays(days: Int) {
                mBinding.txtStalenessDays.text = "Staleness Days : $days"
            }
        })

        // Callback from Flexible Update Progress
        // This is only available for Flexible mode
        // Find more from https://developer.android.com/guide/playcore/in-app-updates#monitor_flexible

        // Callback from Flexible Update Progress
        // This is only available for Flexible mode
        // Find more from https://developer.android.com/guide/playcore/in-app-updates#monitor_flexible
        mUpdateManager?.addFlexibleUpdateDownloadListener(object : FlexibleUpdateDownloadListener {
            override fun onDownloadProgress(bytesDownloaded: Long, totalBytes: Long) {
                txtFlexibleUpdateProgress?.text = "Downloading: $bytesDownloaded / $totalBytes"
            }
        })
    }

    private fun clickListener() {
        mBinding.parentLinear.setOnClickListener(this)
        mBinding.imgShare.setOnClickListener(this)
        mBinding.btnImmediateUpdate.setOnClickListener(this)
        mBinding.btnFlexibleUpdate.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        super.onClick(v)
        when (v.id) {
            R.id.parent_linear -> startActivity(Intent(this, MainActivity::class.java))
            R.id.img_share -> GlobalUtility.rateUsApp(this)
            R.id.btn_flexible_update -> {
                mUpdateManager?.mode(UpdateManagerConstant.FLEXIBLE)?.start()
                txtFlexibleUpdateProgress?.visibility = View.VISIBLE
            }
            R.id.btn_immediate_update -> mUpdateManager?.mode(UpdateManagerConstant.IMMEDIATE)?.start()
        }
    }
}