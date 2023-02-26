package com.webaddicted.forrent.global

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.webaddicted.forrent.R
import com.webaddicted.forrent.base.InAppUpdateActivity
import com.webaddicted.forrent.global.AppApplication.Companion.context
import java.util.*

/**
 * Created by Deepak Sharma on 01/07/23.
 */
class GlobalUtility {
    companion object {
        private val TAG = GlobalUtility::class.java.simpleName
        private const val NOTIFICATION_CHANNEL_ID = "com.icc.cruuui"
        private var snackbar: Snackbar? = null
        fun print(tag: String?, msg: String) = Log.d(tag, msg)

        fun showToast(message: String) =Toast.makeText(context, message, Toast.LENGTH_LONG).show()

        fun initSnackBar(context: Activity, networkConnected: Boolean) {
            if (networkConnected && snackbar != null && snackbar?.isShown!!) {
                updateSnackbar(snackbar!!)
                return
            }
            snackbar =
                Snackbar.make(
                    context.findViewById(android.R.id.content),
                    "",
                    Snackbar.LENGTH_INDEFINITE
                )//.setBehavior(NoSwipeBehavior())
            snackbar?.let {
                val layoutParams =
                    (it.view.layoutParams as FrameLayout.LayoutParams)
                        .also { lp -> lp.setMargins(0, 0, 0, 0) }
                it.view.layoutParams = layoutParams
                it.view.alpha = 0.90f
                it.view.elevation = 0f
                val message = "no internet connection"
                if (networkConnected) updateSnackbar(it)
                else it.view.setBackgroundColor(Color.RED)
                val spannableString = SpannableString(message).apply {
                    setSpan(ForegroundColorSpan(Color.WHITE), 0, message.length, 0)
                }
                it.setText(spannableString)
                it.show()
            }
        }

        private fun updateSnackbar(view: Snackbar) {
            val color = arrayOf(
                ColorDrawable(Color.RED),
                ColorDrawable(Color.GREEN)
            )
            val trans = TransitionDrawable(color)
            view.view.background = (trans)
            trans.startTransition(500)
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({ view.dismiss() }, 1300)
            view.setText("back online")
        }

        //    {START HIDE SHOW KEYBOARD}
        fun hideKeyboard(activity: Activity) {
            try {
                val inputManager =
                    activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
            } catch (ignored: Exception) {
                ignored.printStackTrace()
                Log.d(TAG, "hideKeyboard: ${ignored.message}")
            }

        }

        fun showKeyboard(activity: Activity?) {
            if (activity != null) {
                val imm =
                    activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.toggleSoftInput(
                    InputMethodManager.SHOW_FORCED,
                    InputMethodManager.HIDE_IMPLICIT_ONLY
                )
            }
        }
//    {END HIDE SHOW KEYBOARD}

        fun handleBlockUI(activity: Activity, view: View, isBlockUi: Boolean) {
            if (isBlockUi) {
                activity.window.setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                )
                view.visibility = View.VISIBLE
            } else {
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                view.visibility = View.GONE
            }
        }

        /**
         * provide binding of layout
         *
         * @param context reference of activity
         * @param layout  layout
         * @return viewBinding
         */
        fun getLayoutBinding(context: Context?, layout: Int): ViewDataBinding {
            return DataBindingUtil.inflate(
                LayoutInflater.from(context),
                layout,
                null, false
            )
        }

        /**
         * two digit random number
         *
         * @return random number
         */
        fun getTwoDigitRandomNo(): Int {
            return Random().nextInt(90) + 10
        }

        /**
         * button click fade animation
         *
         * @param view view reference
         */
        fun btnClickAnimation(view: View) {
            val fadeAnimation = AnimationUtils.loadAnimation(view.context, R.anim.fade_in)
            view.startAnimation(fadeAnimation)
        }


        fun showOfflineNotification(context: Context, title: String, description: String) {
            val intent = Intent(context, InAppUpdateActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            if (intent != null) {
                val pendingIntent = PendingIntent.getActivity(
                    context, getTwoDigitRandomNo(), intent,
                    PendingIntent.FLAG_ONE_SHOT
                )
                val defaultSoundUri =
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val notificationBuilder = NotificationCompat.Builder(context)
                notificationBuilder.setSmallIcon(R.mipmap.ic_launcher_round)
                notificationBuilder.setLargeIcon(
                    BitmapFactory.decodeResource(
                        context.resources,
                        R.mipmap.ic_launcher
                    )
                )
                notificationBuilder.setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
                notificationBuilder.setContentTitle(title)
                if (description != null) {
                    notificationBuilder.setContentText(description)
                    notificationBuilder.setStyle(
                        NotificationCompat.BigTextStyle().bigText(description)
                    )
                }
                notificationBuilder.setAutoCancel(true)
                notificationBuilder.setSound(defaultSoundUri)
                notificationBuilder.setVibrate(longArrayOf(1000, 1000))
                notificationBuilder.setContentIntent(pendingIntent)
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val importance = NotificationManager.IMPORTANCE_HIGH
                    val notificationChannel = NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        "NOTIFICATION_CHANNEL_NAME",
                        importance
                    )
                    notificationChannel.enableLights(true)
                    notificationChannel.lightColor = Color.RED
                    notificationChannel.enableVibration(true)
                    notificationChannel.vibrationPattern = longArrayOf(1000, 1000)
                    notificationBuilder.setChannelId(NOTIFICATION_CHANNEL_ID)
                    notificationManager.createNotificationChannel(notificationChannel)
                }
                notificationManager.notify(
                    getTwoDigitRandomNo()/*Id of Notification*/,
                    notificationBuilder.build()
                )
            }
        }


        fun isWifiConnected(activity: Activity): String {
            val cm =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo
            if (activeNetwork != null) { // connected to the internet
                if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) { // connected to wifi
                    return activity.resources
                        .getString(R.string.wifi)
                } else if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) { // connected to the mobile provider's data plan
                    return activity.resources.getString(R.string.network)
                }
            } else return activity.resources.getString(R.string.unavailable)
            return ""
        }

        fun avoidDoubleClicks(view: View) {
            val DELAY_IN_MS: Long = 500
            if (!view.isClickable) {
                return
            }
            view.isClickable = false
            view.postDelayed({ view.isClickable = true }, DELAY_IN_MS)
        }

        fun rateUsApp(mActivity: Activity) {
//            var packageName= "com.quixom.deviceinfo"
            val packageName = mActivity.packageName
            val uri = Uri.parse("market://details?id=$packageName")
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            goToMarket.addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            )
            try {
                mActivity.startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {
                mActivity.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    )
                )
            }
        }

        fun setEnableView(edtView: TextInputEditText, isViewEditable: Boolean) {
            if (isViewEditable) {
                edtView.isClickable = true
                edtView.isLongClickable = true
                edtView.isFocusableInTouchMode = true
                edtView.setTextColor(ContextCompat.getColor(context, R.color.black))
            } else {
                edtView.isClickable = false
                edtView.isLongClickable = false
                edtView.isFocusableInTouchMode = false
                edtView.setTextColor(ContextCompat.getColor(context, R.color.gray))
            }
        }
    }
}
