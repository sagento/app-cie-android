package it.app.cie.lib

import android.annotation.SuppressLint
import android.view.View
import it.ipzs.cieidsdk.common.Callback
import it.ipzs.cieidsdk.common.OperativeMode
import it.ipzs.cieidsdk.event.Event
import it.ipzs.cieidsdk.util.CieIDSdkLogger
import it.ipzs.cieidsdk.util.utils.Companion.updateText
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
        val toSend: String = if (event.attempts == 0 || event.attempts == null) {
            "EVENT : $event"
        } else {
            "EVENT : $event\nTentativi : ${event.attempts}"
        }

        when (variables.mode) {
            OperativeMode.AUTH_IBRIDO -> updateText(variables.textViewOtpResult, toSend, activity)
            OperativeMode.AUTH_WEBVIEW -> updateText(textWebView, toSend, activity)
            else -> {}
        }


    }

    @SuppressLint("SetTextI18n")
    override fun onError(error: Throwable) {
        if (error.localizedMessage != null) {
            val toSend = "onError: " + error.localizedMessage
            CieIDSdkLogger.log(toSend, true)
            val activity = variables.activityList.last().activity

            when (variables.mode) {
                OperativeMode.AUTH_IBRIDO -> updateText(
                    variables.textViewOtpResult,
                    toSend,
                    activity
                )
                OperativeMode.AUTH_WEBVIEW -> updateText(textWebView, toSend, activity)
                else -> {}
            }
        }
    }

    override fun onSuccess(url: String) {
        //rimostro la webview e gli passo la url da caricare

        try {
            webView.visibility = View.VISIBLE
            textWebView.visibility = View.GONE
            homeButton.visibility = View.VISIBLE
            backButton.visibility = View.VISIBLE
        } catch (e: Exception) {

        }

        try {
            if (variables.mode == OperativeMode.AUTH_WEBVIEW)
                webView.loadUrl(url)
        } catch (e: Exception) {

        }
    }
}