package it.ipzs.cieidsdk.util

import android.app.Activity
import android.util.Log
import android.widget.Toast
import it.ipzs.cieidsdk.common.CieIDSdk

class CieIDSdkLogger {

    companion object {

        private const val TAG: String = "CieIDSdkLogger"

        fun log(message: String, toast: Boolean) {
            if (CieIDSdk.enableLog) {
                log2(message, toast)

            }
        }

        private fun log2(message: String, toast: Boolean) {
            Log.d(TAG, message)
            println(message)

            var activity: Activity? = null
            try {
                activity = variables.activityList.last().activity
            } catch (e: Exception) {

            }

            if (toast && activity != null) {
                try {
                    activity.runOnUiThread {
                        Toast.makeText(
                            activity,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }


                } catch (e: Exception) {
                    log(e, false)
                }
            }
        }


        fun log(e: Throwable, toast: Boolean) {
            if (e.message != null)
                if (CieIDSdk.enableLog) {
                    log2(e.message.toString(), toast)
                }
        }
    }
}