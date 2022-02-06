package it.ipzs.cieidsdk.util

import android.app.Activity
import android.nfc.NfcAdapter
import android.webkit.WebView
import android.widget.Button
import android.widget.TextView
import it.ipzs.cieidsdk.common.Callback
import it.ipzs.cieidsdk.common.CieIDSdk
import it.ipzs.cieidsdk.common.OperativeMode
import it.ipzs.cieidsdk.network.service.IdpService
import it.ipzs.cieidsdk.nfc.Ias
import it.ipzs.cieidsdk.nfc.common.nfcCore
import it.ipzs.cieidsdk.url.DeepLinkInfo

class variables {
    companion object {


        var cieIdSdk: CieIDSdk = CieIDSdk()
        var nfcCore: nfcCore = nfcCore()
        lateinit var ias: Ias
        var mode: OperativeMode? = null
        var ciePin: String = ""
        lateinit var callback: Callback
        var textViewOtpResult: TextView? = null
        lateinit var qrCodeUrlScanned: String


        lateinit var idpService: IdpService
        internal var nfcAdapter: NfcAdapter? = null


        internal var deepLinkInfo: DeepLinkInfo = DeepLinkInfo()
        var rubrica: HashMap<String, String>? = null
        lateinit var textError: TextView

        lateinit var webView: WebView
        lateinit var textWebView: TextView
        lateinit var homeButton: Button
        lateinit var backButton: Button

        var isNfcOn: Boolean = false

        var activityList: MutableList<ActivityInfo> = mutableListOf()

        fun getActivity(activityType: ActivityType): Activity? {
            var i = activityList.size - 1
            while (i >= 0) {

                if (activityList[i].activityType == activityType)
                    return activityList[i].activity

                i -= 1
            }

            return null
        }
    }
}