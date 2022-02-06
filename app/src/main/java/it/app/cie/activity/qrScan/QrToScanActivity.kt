package it.app.cie.activity.qrScan

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import it.app.cie.R
import it.app.cie.activity.menu.MenuActivity
import it.app.cie.lib.utils.Companion.insertPin
import it.ipzs.cieidsdk.nfc.common.nfcCore.Companion.detectNfcStatus
import it.ipzs.cieidsdk.util.ActivityInfo
import it.ipzs.cieidsdk.util.ActivityType
import it.ipzs.cieidsdk.util.CieIDSdkLogger
import it.ipzs.cieidsdk.util.variables
import it.ipzs.cieidsdk.util.variables.Companion.textError


class QrToScanActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode_reader)
        variables.activityList.add(ActivityInfo(this, ActivityType.NORMAL, this))

        val buttonBackToMenu: Button = findViewById(R.id.button_backtomenu)
        buttonBackToMenu.setOnClickListener {
            val i = Intent(this, MenuActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i)
        }

        textError = findViewById(R.id.textView_error)

        textError.visibility = GONE



        insertPin(startForResult)


    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                //  you will get result here in result.data


                startQR()

            }

        }


    private lateinit var intentIntegrator: IntentIntegrator

    private fun startQR(): Boolean {

        CieIDSdkLogger.log("starting qr code reader...", true)

        intentIntegrator = IntentIntegrator(variables.activityList.last().activity)
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
                    CieIDSdkLogger.log("scanned qr: " + intentResult.contents.toString(), true)
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
        if (!detectNfcStatus(showToastOnFail = true, openSettingsNFC_onFail = true))
            return

        variables.qrCodeUrlScanned = contents

        val intent = Intent(this, QrScannedActivity::class.java)
        startActivity(intent)
    }


}