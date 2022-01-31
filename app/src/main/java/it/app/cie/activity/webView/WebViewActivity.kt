package it.app.cie.activity.webView

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import it.ipzs.cieidsdk.common.CieIDSdk
import it.ipzs.cieidsdk.common.CieIDSdk.insertPin
import it.ipzs.cieidsdk.common.CieIDSdk.startNfcAndDoActionOnSuccess
import it.ipzs.cieidsdk.common.valuesPassed
import it.ipzs.cieidsdk.nfc.common.nfcCore.detectNfcStatus
import it.ipzs.cieidsdk.nfc.common.nfcCore.startNFCListening
import it.ipzs.cieidsdk.nfc.common.nfcCore.stopNFCListening
import it.app.cie.R
import it.app.cie.activity.menu.MenuActivity
import it.app.cie.lib.CallbackCie


class WebViewActivity : AppCompatActivity() {

    lateinit var webView: WebView
    lateinit var text: TextView
    lateinit var homeButton: Button
    lateinit var backButton: Button
    lateinit var context: Context
    lateinit var activity: AppCompatActivity

    private var homepage = "https://www.google.it/"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        webView = findViewById(R.id.webview1)
        text = findViewById(R.id.textView)
        homeButton = findViewById(R.id.homebutton_id)
        backButton = findViewById(R.id.backbutton_id)
        context = this
        activity = this


        val buttonBackToMenu: Button = findViewById(R.id.button_webview_backtomenu)
        buttonBackToMenu.setOnClickListener {
            val i = Intent(this, MenuActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i)
        }


        //opzioni sicurezza webview
        webView.settings.apply {
            javaScriptEnabled = true
            allowContentAccess = false
            allowFileAccess = false
            allowFileAccessFromFileURLs = false
            allowUniversalAccessFromFileURLs = false
        }

        detectNfcStatus(this, this, showToastOnFail = true, openSettingsNFC_onFail = true)


        //inserire url service provider
        webView.loadUrl(homepage)

        backButton.setOnClickListener { webView.goBack() }
        homeButton.setOnClickListener { webView.loadUrl(homepage) }

        text.visibility = GONE

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                // The webView is about to navigate to the specified host.
                if (url.toString().contains("OpenApp")) {

                    if (detectNfcStatus(
                            context, activity,
                            showToastOnFail = true,
                            openSettingsNFC_onFail = true
                        )
                    ) {

                        //settare la url caricata dalla webview su /OpenApp
                        CieIDSdk.setUrl(url.toString())

                        val callbackCie = CallbackCie(activity)
                        callbackCie.backButton = backButton
                        callbackCie.homeButton = homeButton
                        callbackCie.webView = webView
                        callbackCie.text = text

                        val valuesPassed = valuesPassed(activity, context, callbackCie)

                        insertPin(::startNFC, valuesPassed)
                        return true
                    }


                }
                return super.shouldOverrideUrlLoading(view, url)
            }

        }


    }


    private fun startNFC(valuesPassed: valuesPassed): Boolean {
        //configurazione cieidsdk

        webView.visibility = GONE
        homeButton.visibility = GONE
        backButton.visibility = GONE
        text.visibility = VISIBLE

        startNfcAndDoActionOnSuccess(valuesPassed)
        return true
    }


    override fun onResume() {
        super.onResume()
        //faccio partire l'ascolto dell'nfc
        startNFCListening(this)
    }


    override fun onPause() {
        super.onPause()
        //stop l'ascolto dell'nfc
        stopNFCListening(this)
    }


}