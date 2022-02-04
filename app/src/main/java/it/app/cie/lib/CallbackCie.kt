package it.app.cie.lib

import android.annotation.SuppressLint
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import it.ipzs.cieidsdk.common.Callback
import it.ipzs.cieidsdk.common.CieIDSdk
import it.ipzs.cieidsdk.common.OperativeMode
import it.ipzs.cieidsdk.event.Event
import it.ipzs.cieidsdk.util.CieIDSdkLogger

class CallbackCie(
    activityParam: AppCompatActivity
) : Callback {

    private var activityCompat = activityParam
    var text: TextView? = null
    var webView: WebView? = null
    var backButton: Button? = null
    var homeButton: Button? = null


    @SuppressLint("SetTextI18n")
    override fun onEvent(event: Event) {
        CieIDSdkLogger.log("onEvent: $event", activityCompat)
        activityCompat.runOnUiThread {
            if (event.attempts == 0) {
                text?.text = "EVENT : $event"
            } else {
                text?.text = "EVENT : $event\nTentativi : ${event.attempts}"
            }
        }


    }

    @SuppressLint("SetTextI18n")
    override fun onError(error: Throwable) {
        if (error.localizedMessage != null) {
            CieIDSdkLogger.log("onError: " + error.localizedMessage, activityCompat)
            activityCompat.runOnUiThread {
                text?.text = "ERROR : $error.localizedMessage"
            }
        }
    }

    override fun onSuccess(url: String) {
        //rimostro la webview e gli passo la url da caricare
        webView?.visibility = View.VISIBLE
        text?.visibility = View.GONE
        homeButton?.visibility = View.VISIBLE
        backButton?.visibility = View.VISIBLE


        if (CieIDSdk.mode == OperativeMode.AUTH_WEBVIEW)
            webView?.loadUrl(url)

    }
}