package it.app.cie.lib

import android.annotation.SuppressLint
import android.view.View
import it.ipzs.cieidsdk.common.Callback
import it.ipzs.cieidsdk.common.OperativeMode
import it.ipzs.cieidsdk.event.Event
import it.ipzs.cieidsdk.util.CieIDSdkLogger
import it.ipzs.cieidsdk.util.variables
import it.ipzs.cieidsdk.util.variables.Companion.backButton
import it.ipzs.cieidsdk.util.variables.Companion.homeButton
import it.ipzs.cieidsdk.util.variables.Companion.textWebView
import it.ipzs.cieidsdk.util.variables.Companion.webView

class CallbackCie : Callback {


    @SuppressLint("SetTextI18n")
    override fun onEvent(event: Event) {
        CieIDSdkLogger.log("onEvent: $event", false)
        val activity = variables.activityList.last().activity
        activity.runOnUiThread {
            if (event.attempts == 0) {
                textWebView.text = "EVENT : $event"
            } else {
                textWebView.text = "EVENT : $event\nTentativi : ${event.attempts}"
            }
        }


    }

    @SuppressLint("SetTextI18n")
    override fun onError(error: Throwable) {
        if (error.localizedMessage != null) {
            CieIDSdkLogger.log("onError: " + error.localizedMessage, true)
            val activity = variables.activityList.last().activity
            activity.runOnUiThread {
                textWebView.text = "ERROR : $error.localizedMessage"
            }
        }
    }

    override fun onSuccess(url: String) {
        //rimostro la webview e gli passo la url da caricare
        webView.visibility = View.VISIBLE
        textWebView.visibility = View.GONE
        homeButton.visibility = View.VISIBLE
        backButton.visibility = View.VISIBLE


        if (variables.mode == OperativeMode.AUTH_WEBVIEW)
            webView.loadUrl(url)

    }
}