package it.ipzs.cieidsdk.util

import android.app.Activity
import android.view.View
import android.widget.TextView

class utils {
    companion object {
        fun updateText(text: TextView?, toSend: String, activity: Activity?) {

            val activities = variables.activityList
            for (act in activities) {
                try {
                    act.activity.runOnUiThread {
                        updateText2(text, toSend)
                    }
                } catch (e: Exception) {
                    CieIDSdkLogger.log(e, false)
                }

                try {
                    act.context.run {
                        updateText2(text, toSend)
                    }
                } catch (e: Exception) {
                    CieIDSdkLogger.log(e, false)
                }
            }

            try {
                activity?.runOnUiThread {
                    updateText2(text, toSend)
                }
            } catch (e: Exception) {
                CieIDSdkLogger.log(e, false)
            }

            try {
                updateText2(text, toSend)
            } catch (e: Exception) {
                CieIDSdkLogger.log(e, false)
            }
        }

        private fun updateText2(text: TextView?, toSend: String) {
            try {
                text?.text = toSend
            } catch (e: Exception) {

            }

            try {
                text?.invalidate()
                text?.requestLayout()
            } catch (e: Exception) {

            }
            try {
                text?.textAlignment = View.TEXT_ALIGNMENT_CENTER
            } catch (e: Exception) {

            }


        }
    }
}