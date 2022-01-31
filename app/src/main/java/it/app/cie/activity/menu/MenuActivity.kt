package it.app.cie.activity.menu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import it.ipzs.cieidsdk.common.CieIDSdk
import it.ipzs.cieidsdk.common.OperativeMode
import it.ipzs.cieidsdk.nfc.common.nfcCore.detectNfcStatus
import it.app.cie.R
import it.app.cie.activity.qrScan.QrToScanActivity
import it.app.cie.activity.webView.WebViewActivity


class MenuActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)


        CieIDSdk.enableLog = true

        val buttonWebView: Button = findViewById(R.id.menu_button_webview)
        val buttonQRCode: Button = findViewById(R.id.menu_button_qrcode)

        buttonWebView.setOnClickListener {
            if (detectNfcStatus(
                    this,
                    this,
                    showToastOnFail = true,
                    openSettingsNFC_onFail = true
                )
            ) {
                CieIDSdk.mode = OperativeMode.AUTH_WEBVIEW
                val intent = Intent(this, WebViewActivity::class.java)
                startActivity(intent)
            }
        }

        buttonQRCode.setOnClickListener {
            if (detectNfcStatus(
                    this,
                    this,
                    showToastOnFail = true,
                    openSettingsNFC_onFail = true
                )
            ) {
                CieIDSdk.mode = OperativeMode.AUTH_IBRIDO
                val intent = Intent(this, QrToScanActivity::class.java)
                startActivity(intent)
            }

        }

 

    }


}