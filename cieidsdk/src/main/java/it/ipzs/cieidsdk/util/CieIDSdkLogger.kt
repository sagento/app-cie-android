package it.ipzs.cieidsdk.util

import android.app.Activity
import android.util.Log
import android.widget.Toast
import it.ipzs.cieidsdk.common.CieIDSdk

class CieIDSdkLogger {

    companion object {

        private const val TAG: String = "CieIDSdkLogger"

        fun log(message: String, activity: Activity?) {
            if (CieIDSdk.enableLog) {
                log2(message, true, activity)

            }
        }

        private fun log2(message: String, toast: Boolean, activity: Activity?) {
            Log.d(TAG, message)
            println(message)
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
                    log(e, activity, toast = false)
                }
            }
        }

        private fun log(message: Exception, activity: Activity?, toast: Boolean) {
            if (CieIDSdk.enableLog && message.message != null) {
                log2(message.message.toString(), toast, activity)
            }
        }

        fun log(e: Throwable, activity: Activity?) {
            if (e.message != null)
                if (CieIDSdk.enableLog) {
                    log2(e.message.toString(), true, activity)
                }
        }
    }
}