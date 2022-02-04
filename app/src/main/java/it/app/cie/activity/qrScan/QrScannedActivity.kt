package it.app.cie.activity.qrScan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import it.app.cie.R
import it.app.cie.activity.menu.MenuActivity
import it.app.cie.lib.CallbackCie
import it.ipzs.cieidsdk.common.CieIDSdk
import it.ipzs.cieidsdk.common.CieIDSdk.startNfcAndDoActionOnSuccess
import it.ipzs.cieidsdk.common.valuesPassed
import it.ipzs.cieidsdk.nfc.common.nfcCore
import it.ipzs.cieidsdk.util.CieIDSdkLogger

class QrScannedActivity : AppCompatActivity() {

    private lateinit var textViewResult: TextView

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode_read)

        textViewResult = findViewById(R.id.textView_result)
        CieIDSdk.textViewOtpResult = textViewResult
        nfcCore.valuePassed = valuesPassed(this, this, null)

        val buttonBackToMenu: Button = findViewById(R.id.button_backtomenu2)
        buttonBackToMenu.setOnClickListener {
            val i = Intent(this, MenuActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i)
        }

        val callback = CallbackCie(this)
        try {
            startNfcAndDoActionOnSuccess(valuesPassed(this, this, callback))
        } catch (e: Exception) {
            CieIDSdkLogger.log(e, null)
        }
    }

    override fun onResume() {
        super.onResume()
        //faccio partire l'ascolto dell'nfc
        nfcCore.startNFCListening(this)
    }


    override fun onPause() {
        super.onPause()
        //stop l'ascolto dell'nfc
        nfcCore.stopNFCListening(this)
    }

}