package com.webaddicted.forrent.base

import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

import com.google.gson.Gson
import com.webaddicted.forrent.R
import com.webaddicted.forrent.global.GlobalUtility


abstract class BaseActivityInAppUpdate(layoutId: Int? = null) : BaseActivity(layoutId),
    InstallStateUpdatedListener {
    private var dialog: AlertDialog?= null
    private var appUpdateManager: AppUpdateManager? = null
private var updateType = AppUpdateType.FLEXIBLE
    companion object {
        private val TAG = BaseActivityInAppUpdate::class.java.simpleName
        private const val UPDATE_APP_REQUEST_CODE = 10101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateType = AppUpdateType.IMMEDIATE
        checkForUpdate()
    }

    private fun checkForUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask: Task<AppUpdateInfo> = appUpdateManager?.appUpdateInfo as Task<AppUpdateInfo>
        askForUpdate(appUpdateInfoTask)
    }

    private fun askForUpdate(appUpdateInfoTask: Task<AppUpdateInfo>) {
        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                // For a flexible update, use AppUpdateType.FLEXIBLE
                && appUpdateInfo.isUpdateTypeAllowed(updateType)
            ) {
                // Request the update.
                // Log.e(TAG, "Update Available" );
               dialog =  GlobalUtility.showOkCancelDialog(this@BaseActivityInAppUpdate,
                    getString(R.string.update_available),
                    getString(R.string.new_update_available),
                    okBtn = getString(R.string.download),
                    cancelBtn = getString(R.string.cancel),
                    { _, _ ->
                        try {
                            registerInstallStateListener(appUpdateManager)
                            appUpdateManager?.startUpdateFlowForResult( // Pass the intent that is returned by 'getAppUpdateInfo()'.
                                appUpdateInfo,  // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                                updateType,  // The current activity making the update request.
                                this@BaseActivityInAppUpdate,  // Include a request code to later monitor this update request.
                                UPDATE_APP_REQUEST_CODE
                            )
                        } catch (e: SendIntentException) {
                            Log.d(TAG, "AskForUpdate SendIntentException ${e.message}")
                        }
                    }
                ) { dialog, _ ->
                    dialog.dismiss()
                }
            }
        }
    }

    private fun registerInstallStateListener(appUpdateManager: AppUpdateManager?) {
        appUpdateManager?.registerListener(this)
    }

    override fun onDestroy() {
        appUpdateManager?.unregisterListener(this)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        checkIfUpdateIsStalled()
    }

    private fun checkIfUpdateIsStalled() {
        if (appUpdateManager == null) appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager?.appUpdateInfo?.addOnSuccessListener { appUpdateInfo ->
            // If the update is downloaded but not installed,
            // notify the user to complete the update.
            Log.d(TAG, "checkIfUpdateIsStalled + ${Gson().toJson(appUpdateInfo)}")
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                //  Toast.makeText(BaseActivityInAppUpdate.this, "On resume check shows installation already downloaded", Toast.LENGTH_LONG).show();
                dialogForCompleteUpdate(this.getString(R.string.update_download_install))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            UPDATE_APP_REQUEST_CODE -> {
                Toast.makeText(
                    this@BaseActivityInAppUpdate,
                    "onActivityResult - UPDATE_APP_REQUEST_CODE",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun dialogForCompleteUpdate(message: String) {
        dialog = GlobalUtility.showOkCancelDialog(this@BaseActivityInAppUpdate,
            getString(R.string.update_for_rent),
            "$message ${getString(R.string.update_desc)}",
            okBtn = getString(R.string.restart),
            cancelBtn = getString(R.string.cancel),
            { _, _ -> appUpdateManager?.completeUpdate() }) { dialog, _ -> dialog.dismiss() }
    }

    override fun onStateUpdate(installState: InstallState) {
//        Log.d(TAG, "onStateUpdate  ${Gson().toJson(installState)}")
        if (installState.installStatus() == InstallStatus.DOWNLOADING) {
            Log.d(TAG, "onStateUpdate DOWNLOADING  ${installState.totalBytesToDownload()} - " +
                    "${installState.bytesDownloaded()}" +
                    "${installState.installErrorCode()}" +
                    "----${installState.bytesDownloaded()/installState.totalBytesToDownload()}")
        }else if (installState.installStatus() == InstallStatus.DOWNLOADED) {
            // After the update is downloaded, show a notification
            // and request user confirmation to restart the app.
            dialogForCompleteUpdate(this.getString(R.string.update_download_install))
        }
    }

    override fun onPause() {
        super.onPause()
        dialog?.dismiss()
    }
}