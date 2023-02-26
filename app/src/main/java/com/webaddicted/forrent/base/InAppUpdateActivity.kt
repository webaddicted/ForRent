package com.webaddicted.forrent.base

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.databinding.ViewDataBinding
import com.webaddicted.forrent.BuildConfig
import com.webaddicted.forrent.MainActivity
import com.webaddicted.forrent.R
import com.webaddicted.forrent.databinding.ActivityInAppUpdateBinding
import com.webaddicted.forrent.global.GlobalUtility

class InAppUpdateActivity : BaseActivityInAppUpdate(R.layout.activity_in_app_update) {

    private lateinit var mBinding: ActivityInAppUpdateBinding

    companion object {
        val TAG = InAppUpdateActivity::class.java.simpleName
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
        mBinding.txtMsg.text = "Welcome ${BuildConfig.VERSION_CODE}"
    }

    private fun clickListener() {
        mBinding.parentLinear.setOnClickListener(this)
        mBinding.imgShare.setOnClickListener(this)

    }

    override fun onClick(v: View) {
        super.onClick(v)
        when (v.id) {
            R.id.parent_linear -> startActivity(Intent(this,MainActivity::class.java))
            R.id.img_share -> GlobalUtility.rateUsApp(this)

        }
    }
}