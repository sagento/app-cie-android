package it.app.cie.activity.menu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import it.app.cie.R
import it.app.cie.activity.qrScan.QrToScanActivity
import it.app.cie.activity.webView.WebViewActivity
import it.app.cie.lib.CallbackCie
import it.ipzs.cieidsdk.common.CieIDSdk
import it.ipzs.cieidsdk.common.OperativeMode
import it.ipzs.cieidsdk.nfc.common.nfcCore.Companion.detectNfcStatus
import it.ipzs.cieidsdk.nfc.common.nfcCore.Companion.openNFCSettings
import it.ipzs.cieidsdk.util.ActivityInfo
import it.ipzs.cieidsdk.util.ActivityType
import it.ipzs.cieidsdk.util.variables


class MenuActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        variables.activityList.add(ActivityInfo(this, ActivityType.NORMAL, this))

        CieIDSdk.enableLog = true
        variables.callback = CallbackCie()

        val buttonWebView: Button = findViewById(R.id.menu_button_webview)
        val buttonQRCode: Button = findViewById(R.id.menu_button_qrcode)
        val buttonNfc: Button = findViewById(R.id.button_nfc_settings)

        buttonWebView.setOnClickListener {
            if (detectNfcStatus(


                    showToastOnFail = true,
                    openSettingsNFC_onFail = true
                )
            ) {
                variables.mode = OperativeMode.AUTH_WEBVIEW
                val intent = Intent(this, WebViewActivity::class.java)
                startActivity(intent)
            }
        }

        buttonQRCode.setOnClickListener {
            if (detectNfcStatus(


                    showToastOnFail = true,
                    openSettingsNFC_onFail = true
                )
            ) {
                variables.mode = OperativeMode.AUTH_IBRIDO
                val intent = Intent(this, QrToScanActivity::class.java)
                startActivity(intent)
            }

        }


        buttonNfc.setOnClickListener { openNFCSettings() }

    }


}