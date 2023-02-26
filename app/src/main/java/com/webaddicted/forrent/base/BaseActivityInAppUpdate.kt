package com.webaddicted.forrent.base

import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.Task
import com.webaddicted.forrent.R


abstract class BaseActivityInAppUpdate(layoutId: Int? = null) : BaseActivity(layoutId), InstallStateUpdatedListener {
    private var appUpdateManager: AppUpdateManager? = null
    private var snackbar: Snackbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkForUpdate()
        Toast.makeText(this@BaseActivityInAppUpdate, "checkForUpdate", Toast.LENGTH_LONG).show()

    }

    private fun checkForUpdate() {
        // Creates instance of the manager.
        appUpdateManager = AppUpdateManagerFactory.create(this)

        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask: Task<AppUpdateInfo> = appUpdateManager?.appUpdateInfo as Task<AppUpdateInfo>
        askForUpdate(appUpdateInfoTask)
    }

    private fun askForUpdate(appUpdateInfoTask: Task<AppUpdateInfo>) {
        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() === UpdateAvailability.UPDATE_AVAILABLE // For a flexible update, use AppUpdateType.FLEXIBLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                // Request the update.
                // Log.e(TAG, "Update Available" );
                Toast.makeText(this@BaseActivityInAppUpdate, "Update Available UpdateAvailability.UPDATE_AVAILABLE", Toast.LENGTH_LONG).show()
                try {
                    registerInstallStateListener(appUpdateManager)
                    appUpdateManager?.startUpdateFlowForResult( // Pass the intent that is returned by 'getAppUpdateInfo()'.
                        appUpdateInfo,  // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                        AppUpdateType.FLEXIBLE,  // The current activity making the update request.
                        this@BaseActivityInAppUpdate,  // Include a request code to later monitor this update request.
                        UPDATE_APP_REQUEST_CODE
                    )
                } catch (e: SendIntentException) {
                    Toast.makeText(this@BaseActivityInAppUpdate, "Update Available Exception - ${e.message}", Toast.LENGTH_LONG).show()
                    Log.d("Test", "Lag + ${e.message}")
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
            Toast.makeText(this@BaseActivityInAppUpdate, "checkIfUpdateIsStalled - ${appUpdateInfo.updateAvailability()}", Toast.LENGTH_LONG).show()

            if (appUpdateInfo.installStatus() === InstallStatus.DOWNLOADED) {
                    //  Toast.makeText(BaseActivityInAppUpdate.this, "On resume check shows installation already downloaded", Toast.LENGTH_LONG).show();
                    popupSnackbarForCompleteUpdate(this.getString(R.string.update_download_install))
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            UPDATE_APP_REQUEST_CODE -> {
                Toast.makeText(this@BaseActivityInAppUpdate, "onActivityResult - UPDATE_APP_REQUEST_CODE", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun popupSnackbarForCompleteUpdate(message: String) {
        Toast.makeText(this@BaseActivityInAppUpdate, "PopupSnackbarForCompleteUpdate complete update $message", Toast.LENGTH_LONG).show()

        /*if (findViewById(R.id.place_snackbar) != null) {
            snackbar = Snackbar.make(findViewById(R.id.place_snackbar), message, Snackbar.LENGTH_LONG);
            snackbar.setAction(getString(R.string.restart), v -> {
                appUpdateManager.completeUpdate();
            });
            snackbar.setActionTextColor(getApplicationContext().getResources().getColor(R.color.color_00b9f5));
            View view = snackbar.getView();
            view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
            TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
            ((TextView) (view.findViewById(com.google.android.material.R.id.snackbar_action))).setAllCaps(false);
            Typeface custom_font = FontUtility.Companion.getTypeFaceForFont(FontConstants.FONT_BOLD);
            tv.setTypeface(custom_font);
            tv.setTextColor(Color.WHITE);
            snackbar.show();
        } else {
            showSnackbarAtBottom(message);
        }*/
    }

    private fun showSnackbarAtBottom(message: String) {
//        if (netscape.javascript.JSObject.getWindow().getDecorView().getRootView() != null) {
//            val textView =
//                snackbarView.findViewById(android.support.design.R.id.snackbar_text)
//            snackbar = Snackbar.make(this.findViewById(R.id.RL),
//                message,
//                Snackbar.LENGTH_LONG
//            )
//            snackbar?.setAction(getString(R.string.restart)) { v -> appUpdateManager?.completeUpdate() }
//            snackbar?.setActionTextColor(
//                ApplicationProvider.getApplicationContext<Context>().getResources()
//                    .getColor(R.color.color_00b9f5)
//            )
//            val view: View = snackbar!!.view
//            view.setBackgroundColor(
//                ContextCompat.getColor(
//                    ApplicationProvider.getApplicationContext<Context>(),
//                    R.color.black
//                )
//            )
//            val tv: TextView = view.findViewById(com.google.android.material.R.id.snackbar_text)
//            (view.findViewById(com.google.android.material.R.id.snackbar_action) as TextView).isAllCaps =
//                false
//            tv.setTextColor(Color.WHITE)
//            snackbar!!.show()
//        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (snackbar != null) {
            try {
                val view: View = snackbar!!.view
                val rect = Rect()
                view.getGlobalVisibleRect(rect)
                if (!rect.contains(event.x.toInt(), event.y.toInt())) {
                    if (snackbar!!.isShown) {
                        snackbar!!.dismiss()
                    }
                }
            } catch (e: Exception) {
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onStateUpdate(installState: InstallState) {
        if (installState.installStatus() === InstallStatus.DOWNLOADED) {
            // After the update is downloaded, show a notification
            // and request user confirmation to restart the app.
            popupSnackbarForCompleteUpdate(this.getString(R.string.update_download_install))
        }
    }

    companion object {
        private const val TAG = "BaseActivityInAppUpdate"
        private const val UPDATE_APP_REQUEST_CODE = 10101
    }
}