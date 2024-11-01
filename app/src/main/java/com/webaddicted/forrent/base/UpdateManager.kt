package com.webaddicted.forrent.base

import android.content.IntentSender
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.webaddicted.forrent.base.UpdateManagerConstant.FLEXIBLE
import java.lang.ref.WeakReference


class UpdateManager private constructor(activity: AppCompatActivity) : LifecycleObserver {
    private val mActivityWeakReference: WeakReference<AppCompatActivity> = WeakReference(activity)

    // Default mode is FLEXIBLE
    private var mode = FLEXIBLE
    // Creates instance of the manager.
    private val appUpdateManager: AppUpdateManager?
    // Returns an intent object that you use to check for an update.
    private val appUpdateInfoTask: Task<AppUpdateInfo>
    private var flexibleUpdateDownloadListener: FlexibleUpdateDownloadListener? = null

    fun mode(mode: Int): UpdateManager {
        val strMode = if (mode == FLEXIBLE) "FLEXIBLE" else "IMMEDIATE"
        Log.d(TAG, "Set update mode to : $strMode")
        this.mode = mode
        return this
    }

    fun start() {
        if (mode == FLEXIBLE) {
            setUpListener()
        }
        checkUpdate()
    }

    private fun checkUpdate() {
        // Checks that the platform will allow the specified type of update.
        Log.d(TAG, "Checking for updates")
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(mode)) {
                // Request the update.
                Log.d(TAG, "Update available")
                startUpdate(appUpdateInfo)
            } else {
                Log.d(TAG, "No Update available")
            }
        }
    }

    private fun startUpdate(appUpdateInfo: AppUpdateInfo) {
        try {
            Log.d(TAG, "Starting update")
            activity?.let {
                appUpdateManager?.startUpdateFlowForResult(
                    appUpdateInfo,
                    mode,
                    it,
                    UpdateManagerConstant.REQUEST_CODE
                )
            }
        } catch (e: IntentSender.SendIntentException) {
            Log.d(TAG, "" + e.message)
        }
    }

    //    public static void handleResult(int requestCode, int resultCode){
    //        Log.d("LIBRARY_ZMA", "Req code Update : " + requestCode);
    //        if (requestCode == UpdateManagerConstant.REQUEST_CODE) {
    //            Log.d("LIBRARY_ZMA", "Result code Update : " + resultCode);
    //            if (resultCode != RESULT_OK) {
    //                Log.d("LIBRARY_ZMA", "Update flow failed! Result code: " + resultCode);
    //                // If the update is cancelled or fails,
    //                // you can request to start the update again.
    //            }
    //        }
    //    }
    private val listener: InstallStateUpdatedListener = InstallStateUpdatedListener { installState ->
            if (installState.installStatus() == InstallStatus.DOWNLOADING) {
                val bytesDownloaded = installState.bytesDownloaded()
                val totalBytesToDownload = installState.totalBytesToDownload()
                if (flexibleUpdateDownloadListener != null) {
                    flexibleUpdateDownloadListener!!.onDownloadProgress(
                        bytesDownloaded,
                        totalBytesToDownload
                    )
                }
            }
            if (installState.installStatus() == InstallStatus.DOWNLOADED) {
                // After the update is downloaded, show a notification
                // and request user confirmation to restart the app.
                Log.d(TAG, "An update has been downloaded")
                popupSnackbarForCompleteUpdate()
            }
        }

    init {
        appUpdateManager = this.activity?.let { AppUpdateManagerFactory.create(it) }
        appUpdateInfoTask = appUpdateManager?.appUpdateInfo as Task<AppUpdateInfo>
        activity.lifecycle.addObserver(this)
    }

    private fun setUpListener() {
        appUpdateManager!!.registerListener(listener!!)
    }

    private fun continueUpdate() {
        if (instance?.mode == FLEXIBLE) {
            continueUpdateForFlexible()
        } else {
            continueUpdateForImmediate()
        }
    }

    private fun continueUpdateForFlexible() {
        instance?.appUpdateManager
            ?.appUpdateInfo
            ?.addOnSuccessListener { appUpdateInfo -> // If the update is downloaded but not installed,
                // notify the user to complete the update.
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    Log.d(TAG, "An update has been downloaded")
                    instance!!.popupSnackbarForCompleteUpdate()
                }
            }
    }

    private fun continueUpdateForImmediate() {
        instance?.appUpdateManager
            ?.appUpdateInfo
            ?.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    // If an in-app update is already running, resume the update.
                    try {
                        activity?.let {
                            instance?.appUpdateManager?.startUpdateFlowForResult(
                                appUpdateInfo,
                                instance?.mode!!,
                                it,
                                UpdateManagerConstant.REQUEST_CODE
                            )
                        }
                    } catch (e: IntentSender.SendIntentException) {
                        Log.d(TAG, "" + e.message)
                    }
                }
            }
    }

    private fun popupSnackbarForCompleteUpdate() {
        val snackbar = Snackbar.make(
            activity!!.window.decorView.findViewById(android.R.id.content),
            "An update has just been downloaded.",
            Snackbar.LENGTH_INDEFINITE
        )
        snackbar.setAction("RESTART") { appUpdateManager?.completeUpdate() }
        snackbar.show()
    }

    fun addUpdateInfoListener(updateInfoListener: UpdateInfoListener) {
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                // Request the update.
                Log.d(TAG, "Update available")
                val availableVersionCode = appUpdateInfo.availableVersionCode()
                val stalenessDays =
                    if (appUpdateInfo.clientVersionStalenessDays() != null) appUpdateInfo
                        .clientVersionStalenessDays()!! else -1
                updateInfoListener.onReceiveVersionCode(availableVersionCode)
                updateInfoListener.onReceiveStalenessDays(stalenessDays)
            } else {
                Log.d(TAG, "No Update available")
            }
        }
    }

    fun addFlexibleUpdateDownloadListener(flexibleUpdateDownloadListener: FlexibleUpdateDownloadListener?) {
        this.flexibleUpdateDownloadListener = flexibleUpdateDownloadListener
    }

    private val activity: AppCompatActivity? get() = mActivityWeakReference.get()

    private fun unregisterListener() {
        if (appUpdateManager != null && listener != null) {
            appUpdateManager.unregisterListener(listener)
            Log.d(TAG, "Unregistered the install state listener")
        }
    }

    interface UpdateInfoListener {
        fun onReceiveVersionCode(code: Int)
        fun onReceiveStalenessDays(days: Int)
    }

    interface FlexibleUpdateDownloadListener {
        fun onDownloadProgress(bytesDownloaded: Long, totalBytes: Long)
    }

    @OnLifecycleEvent(Event.ON_RESUME)
    private fun onResume() {
        continueUpdate()
    }

    @OnLifecycleEvent(Event.ON_DESTROY)
    private fun onDestroy() {
        unregisterListener()
    }

    companion object {
        private const val TAG = "InAppUpdateManager"
        private var instance: UpdateManager? = null
        fun Builder(activity: AppCompatActivity): UpdateManager? {
            if (instance == null) {
                instance = UpdateManager(activity)
            }
            Log.d(TAG, "Instance created")
            return instance
        }
    }
}
object UpdateManagerConstant {
    const val REQUEST_CODE = 781
    const val FLEXIBLE = AppUpdateType.FLEXIBLE
    const val IMMEDIATE = AppUpdateType.IMMEDIATE
}
