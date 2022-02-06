package it.app.cie.activity.qrScan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import it.app.cie.R
import it.app.cie.activity.menu.MenuActivity
import it.ipzs.cieidsdk.util.ActivityInfo
import it.ipzs.cieidsdk.util.ActivityType
import it.ipzs.cieidsdk.util.CieIDSdkLogger
import it.ipzs.cieidsdk.util.variables

class QrScannedActivity : AppCompatActivity() {


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode_read)
        variables.activityList.add(ActivityInfo(this, ActivityType.NFC, this))


        variables.textViewOtpResult = findViewById(R.id.textView_result)


        val buttonBackToMenu: Button = findViewById(R.id.button_backtomenu2)
        buttonBackToMenu.setOnClickListener {
            val i = Intent(this, MenuActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i)
        }

        try {
            variables.cieIdSdk.startNfcAndDoActionOnSuccess()
        } catch (e: Exception) {
            CieIDSdkLogger.log(e, true)
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