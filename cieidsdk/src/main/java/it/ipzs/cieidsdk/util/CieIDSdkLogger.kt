package it.ipzs.cieidsdk.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import it.ipzs.cieidsdk.common.CieIDSdk

class CieIDSdkLogger {

    companion object {

        private const val TAG: String = "CieIDSdkLogger"

        fun log(message: String, context: Context?) {
            if (CieIDSdk.enableLog) {
                log2(message, context)

            }
        }

        private fun log2(message: String, context: Context?) {
            Log.d(TAG, message)
            println(message)
            if (context != null) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }

        fun log(message: Exception, context: Context?) {
            if (CieIDSdk.enableLog && message.message != null) {
                log2(message.message.toString(), context)
            }
        }

        fun log(e: Throwable, context: Context?) {
            if (e.message != null)
                if (CieIDSdk.enableLog) {
                    log2(e.message.toString(), context)
                }
        }
    }
}