package it.ipzs.cieidsdk.util

import android.util.Log
import it.ipzs.cieidsdk.common.CieIDSdk

class CieIDSdkLogger {

    companion object {

        private const val TAG: String = "CieIDSdkLogger"

        fun log(message: String) {
            if (CieIDSdk.enableLog) {
                Log.d(TAG, message)
                println(message)
            }
        }

        fun log(message: Exception) {
            if (CieIDSdk.enableLog && message.message != null) {
                Log.d(TAG, message.message.toString())
                println(message.message.toString())
            }
        }

        fun log(e: Throwable) {
            if (e.message != null)
                if (CieIDSdk.enableLog) {
                    Log.d(TAG, e.message.toString())
                    println(e.message.toString())
                }
        }
    }
}