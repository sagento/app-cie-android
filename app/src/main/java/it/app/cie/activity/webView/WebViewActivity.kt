package it.app.cie.activity.webView

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import it.app.cie.R
import it.app.cie.activity.menu.MenuActivity
import it.app.cie.lib.utils
import it.ipzs.cieidsdk.nfc.common.nfcCore.Companion.detectNfcStatus
import it.ipzs.cieidsdk.util.ActivityInfo
import it.ipzs.cieidsdk.util.ActivityType
import it.ipzs.cieidsdk.util.variables
import it.ipzs.cieidsdk.util.variables.Companion.backButton
import it.ipzs.cieidsdk.util.variables.Companion.homeButton
import it.ipzs.cieidsdk.util.variables.Companion.textWebView
import it.ipzs.cieidsdk.util.variables.Companion.webView


class WebViewActivity : AppCompatActivity() {


    private var homepage = "https://www.google.it/"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        variables.activityList.add(ActivityInfo(this, ActivityType.NFC, this))


        webView = findViewById(R.id.webview1)
        textWebView = findViewById(R.id.textView)
        homeButton = findViewById(R.id.homebutton_id)
        backButton = findViewById(R.id.backbutton_id)


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

        detectNfcStatus(showToastOnFail = true, openSettingsNFC_onFail = true)


        //inserire url service provider
        webView.loadUrl(homepage)

        backButton.setOnClickListener { webView.goBack() }
        homeButton.setOnClickListener { webView.loadUrl(homepage) }

        textWebView.visibility = GONE

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                // The webView is about to navigate to the specified host.
                if (url.toString().contains("OpenApp")) {

                    if (detectNfcStatus(

                            showToastOnFail = true,
                            openSettingsNFC_onFail = true
                        )
                    ) {

                        //settare la url caricata dalla webview su /OpenApp
                        variables.cieIdSdk.setUrl(url.toString())

                        utils.insertPin(startForResult)
                        return true
                    }


                }
                return super.shouldOverrideUrlLoading(view, url)
            }

        }


    }

    val startForResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                //  you will get result here in result.data


                webView.visibility = GONE
                homeButton.visibility = GONE
                backButton.visibility = GONE
                textWebView.visibility = VISIBLE

                variables.cieIdSdk.startNfcAndDoActionOnSuccess()

            }

        }


    override fun onResume() {
        super.onResume()
        //faccio partire l'ascolto dell'nfc
        variables.nfcCore.startNFCListening()
    }


    override fun onPause() {
        super.onPause()
        //stop l'ascolto dell'nfc
        variables.nfcCore.stopNFCListening()
    }


}