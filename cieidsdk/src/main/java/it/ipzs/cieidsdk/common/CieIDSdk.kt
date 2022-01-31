package it.ipzs.cieidsdk.common

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.text.InputType
import android.widget.EditText
import android.widget.TextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import it.ipzs.cieidsdk.event.Event
import it.ipzs.cieidsdk.event.EventCertificate
import it.ipzs.cieidsdk.event.EventError
import it.ipzs.cieidsdk.network.NetworkClient
import it.ipzs.cieidsdk.network.service.IdpService
import it.ipzs.cieidsdk.nfc.AppUtil
import it.ipzs.cieidsdk.nfc.Ias
import it.ipzs.cieidsdk.nfc.algorithms.Sha256
import it.ipzs.cieidsdk.nfc.common.nfcCore.startNFCListening
import it.ipzs.cieidsdk.url.DeepLinkInfo
import it.ipzs.cieidsdk.util.CieIDSdkLogger
import okhttp3.ResponseBody
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLProtocolException

val CERTIFICATE_EXPIRED: CharSequence = "SSLV3_ALERT_CERTIFICATE_EXPIRED"
val CERTIFICATE_REVOKED: CharSequence = "SSLV3_ALERT_CERTIFICATE_REVOKED"


interface Callback {

    fun onSuccess(url: String)
    fun onError(error: Throwable)
    fun onEvent(event: Event)
}

@SuppressLint("StaticFieldLeak")
object CieIDSdk {

    var textViewOtpResult: TextView? = null
    lateinit var qrCodeUrlScanned: String
    var mode: OperativeMode? = null
    private lateinit var idpService: IdpService
    internal var nfcAdapter: NfcAdapter? = null
    internal var callback: Callback? = null
    internal var deepLinkInfo: DeepLinkInfo = DeepLinkInfo()

    lateinit var ias: Ias
    var enableLog: Boolean = false
    private var ciePin = ""

    // the timeout of transceive(byte[]) in milliseconds (https://developer.android.com/reference/android/nfc/tech/IsoDep#setTimeout(int))
    // a longer timeout may be useful when performing transactions that require a long processing time on the tag such as key generation.
    internal const val isoDepTimeout: Int = 10000

    private val ciePinRegex = Regex("^[0-9]{8}$")

    // pin property
    // 'set' checks if the given value has a valid pin cie format (string, 8 length, all chars are digits)
    private var pin: String
        get() = ciePin
        set(value) {
            require(ciePinRegex.matches(value)) { "the given cie PIN has no valid format" }
            ciePin = value
        }


    @SuppressLint("CheckResult")
    fun callWebView() {

        ias.getIdServizi()
        ias.startSecureChannel(ciePin)
        val certificate = ias.readCertCie()
        ias.startKeepAlive()

        val idpService: IdpService = NetworkClient(certificate).idpService
        val mapValues = hashMapOf<String, String>().apply {
            put(deepLinkInfo.name!!, deepLinkInfo.value!!)
            put(IdpService.authnRequest, deepLinkInfo.authnRequest ?: "")
            put(IdpService.generaCodice, "1")
        }

        // handling all swallowed exception
        RxJavaPlugins.setErrorHandler { error ->
            run {
                CieIDSdkLogger.log("error handled by RxJavaPlugins $error")
                callback?.onError(error)
            }
        }

        idpService.callIdp(mapValues).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object :
                DisposableSingleObserver<Response<ResponseBody>>() {
                override fun onSuccess(idpResponse: Response<ResponseBody>) {
                    if (idpResponse.isSuccessful) {
                        CieIDSdkLogger.log("onSuccess")
                        if (idpResponse.body() != null) {
                            val codiceServer =
                                idpResponse.body()!!.string().split(":".toRegex())
                                    .dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                            if (!checkCodiceServer(codiceServer)) {
                                callback?.onEvent(Event(EventError.GENERAL_ERROR))
                            }
                            val url =
                                deepLinkInfo.nextUrl + "?" + deepLinkInfo.name + "=" + deepLinkInfo.value + "&login=1&codice=" + codiceServer
                            callback?.onSuccess(url)

                        } else {
                            callback?.onEvent(Event(EventError.AUTHENTICATION_ERROR))
                        }
                    } else {
                        CieIDSdkLogger.log("onError")
                        callback?.onEvent(Event(EventError.AUTHENTICATION_ERROR))
                    }

                }

                override fun onError(e: Throwable) {
                    CieIDSdkLogger.log("onError")

                    when (e) {
                        is SocketTimeoutException, is UnknownHostException -> {
                            CieIDSdkLogger.log("SocketTimeoutException or UnknownHostException")
                            callback?.onEvent(Event(EventError.ON_NO_INTERNET_CONNECTION))

                        }
                        is SSLProtocolException -> {

                            CieIDSdkLogger.log("SSLProtocolException")
                            e.message?.let {
                                when {
                                    it.contains(CERTIFICATE_EXPIRED) -> callback?.onEvent(
                                        Event(
                                            EventCertificate.CERTIFICATE_EXPIRED
                                        )
                                    )
                                    it.contains(CERTIFICATE_REVOKED) -> callback?.onEvent(
                                        Event(
                                            EventCertificate.CERTIFICATE_REVOKED
                                        )
                                    )
                                    else -> callback?.onError(e)
                                }
                            }

                        }
                        else -> callback?.onError(e)
                    }
                }
            })
    }

    private fun checkCodiceServer(codiceServer: String): Boolean {
        val regex = Regex("^[0-9]{16}$")
        return regex.matches(codiceServer)
    }


    /**
     * Set the SDK callback and init NFC adapter.
     * start method must be called before accessing nfc features
     * */
    private fun start(activity: Activity, cb: Callback) {
        callback = cb
        nfcAdapter = (activity.getSystemService(Context.NFC_SERVICE) as NfcManager).defaultAdapter
    }


    fun setUrl(url: String) {
        val appLinkData = Uri.parse(url)
        deepLinkInfo = DeepLinkInfo(
            value = appLinkData.getQueryParameter(DeepLinkInfo.KEY_VALUE),
            name = appLinkData.getQueryParameter(DeepLinkInfo.KEY_NAME),
            authnRequest = appLinkData.getQueryParameter(DeepLinkInfo.KEY_AUTHN_REQUEST_STRING),
            nextUrl = appLinkData.getQueryParameter(DeepLinkInfo.KEY_NEXT_UTL),
            opText = appLinkData.getQueryParameter(DeepLinkInfo.KEY_OP_TEXT),
            host = appLinkData.host ?: "",
            logo = appLinkData.getQueryParameter(DeepLinkInfo.KEY_LOGO)
        )

    }


    private fun authQR(scannedUrl: String): String {

        CieIDSdkLogger.log("starting asking the CIE to sign the challenge...")

        ias.getIdServizi()
        ias.startSecureChannel(pin)
        val certificate = ias.readCertCie()

        val sha256 = Sha256
        val bytes: ByteArray = scannedUrl.toByteArray(Charsets.UTF_8)

        val b: ByteArray = AppUtil.appendByteArray(
            Padding.padding, sha256.encrypt(bytes)
        )

        val v: ByteArray = ias.sign(b)

        val hex: String = AppUtil.bytesToHex(v)


        try {
            idpService = NetworkClient(certificate).idpService
        } catch (e: Exception) {
            CieIDSdkLogger.log(e)
        }

        return hex
    }

    internal fun loginIbrido() {
        val uri: Uri =
            Uri.parse(qrCodeUrlScanned)

        val opId = uri.getQueryParameter("opId") ?: return
        /*
        val opType = uri.getQueryParameter("opType")
        val SPName = uri.getQueryParameter("SPName")
        val IdPName = uri.getQueryParameter("IdPName")
        val userId = uri.getQueryParameter("userId")
        val opText = uri.getQueryParameter("opText")

         */

        val hex: String = authQR(qrCodeUrlScanned)

        CieIDSdkLogger.log("sign successful: hex=$hex")

        loginIbridoPost(hex, opId)
    }

    fun startNfcAndDoActionOnSuccess(
        valuesPassed: valuesPassed
    ): Boolean {

        if (valuesPassed.getActivity() == null || valuesPassed.getCallback() == null)
            return false

        CieIDSdkLogger.log("starting nfc scan...")


        start(valuesPassed.getActivity()!!, valuesPassed.getCallback()!!)
        startNFCListening(valuesPassed.getActivity()!!)
        return true

    }

    fun insertPin(
        functionToRun: (valuesPassed) -> Boolean,
        valuesPassed: valuesPassed
    ) {

        if (pin.isEmpty()) {
            val builder = AlertDialog.Builder(valuesPassed.getContext()!!)
            builder.setTitle("Inserisci PIN")

            // Set up the input
            val input = EditText(valuesPassed.getContext())
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.inputType = InputType.TYPE_CLASS_NUMBER
            builder.setView(input)

            // Set up the buttons
            builder.setPositiveButton(
                "OK"
            ) { dialog, _ ->
                if (input.text != null && input.text.isNotEmpty() && input.text.length == 8) {
                    pin = input.text.toString()
                    functionToRun(valuesPassed)
                } else {
                    dialog.cancel()
                }
            }
            builder.setNegativeButton(
                "Cancel"
            ) { dialog, _ -> dialog.cancel() }

            builder.show()
        } else {
            functionToRun(valuesPassed)
        }
    }


    @SuppressLint("CheckResult")
    private fun loginIbridoPost(hex: String, opId: String) {
        val mapValues = hashMapOf<String, String>()
        mapValues.apply {
            put("hashJsonFirmato", hex)
            put("opId", opId)
        }

        CieIDSdkLogger.log("sending the challenge signed to the server...")

        idpService.callIdpQr(mapValues).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object :
                DisposableSingleObserver<Response<ResponseBody>>() {
                override fun onSuccess(idpResponse: Response<ResponseBody>) {
                    val r = idpResponse.body()?.string()

                    CieIDSdkLogger.log("server reply was: $r")

                    if (r != null) {
                        val codice: String

                        try {
                            codice = r.split(":")[1]
                            textViewOtpResult?.text = codice
                            CieIDSdkLogger.log("codice otp: $codice")
                        } catch (e: Exception) {
                            CieIDSdkLogger.log("exception $e")
                        }

                    }
                }

                override fun onError(e: Throwable) {
                    CieIDSdkLogger.log(e)
                }
            })
    }


}
