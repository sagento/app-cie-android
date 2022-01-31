package it.app.cie.activity.qrScan

import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import it.ipzs.cieidsdk.common.Callback
import it.ipzs.cieidsdk.common.CieIDSdk
import it.ipzs.cieidsdk.common.CieIDSdk.insertPin
import it.ipzs.cieidsdk.common.valuesPassed
import it.ipzs.cieidsdk.nfc.common.nfcCore.detectNfcStatus
import it.ipzs.cieidsdk.util.CieIDSdkLogger
import it.app.cie.activity.menu.MenuActivity
import it.app.cie.R
import it.app.cie.lib.CallbackCie


class QrToScanActivity : AppCompatActivity() {

    private lateinit var callback: Callback
    private lateinit var textError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode_reader)

        val buttonBackToMenu: Button = findViewById(R.id.button_backtomenu)
        buttonBackToMenu.setOnClickListener {
            val i = Intent(this, MenuActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i)
        }

        textError = findViewById(R.id.textView_error)

        textError.visibility = GONE

        callback = CallbackCie(this)
        val valuesPassedVar = valuesPassed(this, this, callback)
        insertPin(::startQR, valuesPassedVar)


    }


    private lateinit var intentIntegrator: IntentIntegrator

    private fun startQR(valuesPassed: valuesPassed): Boolean {

        CieIDSdkLogger.log("starting qr code reader...")

        intentIntegrator = IntentIntegrator(valuesPassed.getActivity())
        intentIntegrator.setPrompt("Scan 'CIE' QR Code")
        intentIntegrator.setOrientationLocked(false)
        intentIntegrator.setBeepEnabled(false)
        intentIntegrator.captureActivity = QrScanningActivity::class.java
        intentIntegrator.initiateScan()

        textError.visibility = VISIBLE
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (intentResult != null) {
            if (intentResult.contents != null) {

                if (intentResult.contents != null) {
                    CieIDSdkLogger.log("scanned qr: " + intentResult.contents.toString())
                    processScannedQR_code(intentResult.contents)
                    return

                }

            } else {
                Toast.makeText(baseContext, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun processScannedQR_code(contents: String) {
        if (!detectNfcStatus(this, this, showToastOnFail = true, openSettingsNFC_onFail = true))
            return

        CieIDSdk.qrCodeUrlScanned = contents

        val intent = Intent(this, QrScannedActivity::class.java)
        startActivity(intent)
    }


}