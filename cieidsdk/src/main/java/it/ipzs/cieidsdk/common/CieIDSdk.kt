package it.ipzs.cieidsdk.common

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.nfc.NfcManager
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
import it.ipzs.cieidsdk.nfc.algorithms.Sha256
import it.ipzs.cieidsdk.url.DeepLinkInfo
import it.ipzs.cieidsdk.util.ActivityType
import it.ipzs.cieidsdk.util.CieIDSdkLogger
import it.ipzs.cieidsdk.util.variables
import it.ipzs.cieidsdk.util.variables.Companion.callback
import it.ipzs.cieidsdk.util.variables.Companion.deepLinkInfo
import it.ipzs.cieidsdk.util.variables.Companion.idpService
import it.ipzs.cieidsdk.util.variables.Companion.nfcAdapter
import it.ipzs.cieidsdk.util.variables.Companion.qrCodeUrlScanned
import it.ipzs.cieidsdk.util.variables.Companion.textViewOtpResult
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
class CieIDSdk {

    companion object {
        var enableLog: Boolean = false

        // the timeout of transceive(byte[]) in milliseconds (https://developer.android.com/reference/android/nfc/tech/IsoDep#setTimeout(int))
        // a longer timeout may be useful when performing transactions that require a long processing time on the tag such as key generation.
        internal const val isoDepTimeout: Int = 10000
    }


    @SuppressLint("CheckResult")
    fun callWebView() {

        variables.ias.getIdServizi()
        variables.ias.startSecureChannel(variables.ciePin)
        val certificate = variables.ias.readCertCie()
        variables.ias.startKeepAlive()

        idpService = NetworkClient(certificate).idpService
        val mapValues = hashMapOf<String, String>().apply {
            put(
                deepLinkInfo.name ?: return@apply,
                deepLinkInfo.value ?: return@apply
            )
            put(IdpService.authnRequest, deepLinkInfo.authnRequest ?: "")
            put(IdpService.generaCodice, "1")
        }

        // handling all swallowed exception
        RxJavaPlugins.setErrorHandler { error ->
            run {
                CieIDSdkLogger.log("error handled by RxJavaPlugins $error", true)
                callback.onError(error)
            }
        }

        webViewPost(mapValues)
    }

    @SuppressLint("CheckResult")
    private fun webViewPost(mapValues: HashMap<String, String>) {
        val activity = variables.activityList.last().activity

        try {
            idpService.callIdp(mapValues).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object :
                    DisposableSingleObserver<Response<ResponseBody>>() {
                    override fun onSuccess(idpResponse: Response<ResponseBody>) {
                        webViewOnSuccess(idpResponse, activity)

                    }

                    override fun onError(e: Throwable) {
                        webViewOnError(e, activity)
                    }
                })
        } catch (e: Exception) {
            CieIDSdkLogger.log(e, true)
        }
    }

    internal fun webViewOnError(e: Throwable, activity: Activity?) {
        CieIDSdkLogger.log("onError", true)

        when (e) {
            is SocketTimeoutException, is UnknownHostException -> {
                CieIDSdkLogger.log(
                    "SocketTimeoutException or UnknownHostException", true
                )
                callback.onEvent(Event(EventError.ON_NO_INTERNET_CONNECTION))

            }
            is SSLProtocolException -> {

                CieIDSdkLogger.log("SSLProtocolException", true)
                e.message?.let {
                    when {
                        it.contains(CERTIFICATE_EXPIRED) -> callback.onEvent(
                            Event(
                                EventCertificate.CERTIFICATE_EXPIRED
                            )
                        )
                        it.contains(CERTIFICATE_REVOKED) -> callback.onEvent(
                            Event(
                                EventCertificate.CERTIFICATE_REVOKED
                            )
                        )
                        else -> callback.onError(e)
                    }
                }

            }
            else -> callback.onError(e)
        }
    }

    internal fun webViewOnSuccess(idpResponse: Response<ResponseBody>, activity: Activity?) {
        if (idpResponse.isSuccessful) {
            CieIDSdkLogger.log("onSuccess", true)
            if (idpResponse.body() != null) {
                val codiceServer =
                    (idpResponse.body() ?: return).string().split(":".toRegex())
                        .dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                if (!checkCodiceServer(codiceServer)) {
                    callback.onEvent(Event(EventError.GENERAL_ERROR))
                }
                val url =
                    deepLinkInfo.nextUrl + "?" + deepLinkInfo.name + "=" + deepLinkInfo.value + "&login=1&codice=" + codiceServer
                callback.onSuccess(url)

            } else {
                callback.onEvent(Event(EventError.AUTHENTICATION_ERROR))
            }
        } else {
            CieIDSdkLogger.log("onError", true)
            callback.onEvent(Event(EventError.AUTHENTICATION_ERROR))
        }
    }

    private fun checkCodiceServer(codiceServer: String): Boolean {
        val regex = Regex("^[0-9]{16}$")
        return regex.matches(codiceServer)
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

        CieIDSdkLogger.log("starting asking the CIE to sign the challenge...", true)

        variables.ias.getIdServizi()
        variables.ias.startSecureChannel(variables.ciePin)
        val certificate = variables.ias.readCertCie()

        val sha256 = Sha256
        val bytes: ByteArray = scannedUrl.toByteArray(Charsets.UTF_8)

        val b: ByteArray = AppUtil.appendByteArray(
            Padding.padding, sha256.encrypt(bytes)
        )

        val v: ByteArray = variables.ias.sign(b)

        val hex: String = AppUtil.bytesToHex(v)


        try {
            idpService = NetworkClient(certificate).idpService
        } catch (e: Exception) {
            CieIDSdkLogger.log(e, true)
        }

        return hex
    }

    internal fun loginIbrido(activity: Activity?) {
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

        CieIDSdkLogger.log("sign successful: hex=$hex", true)

        loginIbridoPost(hex, opId, activity)
    }

    fun startNfcAndDoActionOnSuccess(): Boolean {

        variables.isNfcOn = true
        CieIDSdkLogger.log("starting nfc scan...", true)

        val activity = variables.getActivity(ActivityType.NFC) ?: return false
        nfcAdapter = (activity.getSystemService(Context.NFC_SERVICE) as NfcManager).defaultAdapter
        variables.nfcCore.startNFCListening()
        return true

    }


    @SuppressLint("CheckResult")
    private fun loginIbridoPost(hex: String, opId: String, activity: Activity?) {
        val mapValues = hashMapOf<String, String>()
        mapValues.apply {
            put("hashJsonFirmato", hex)
            put("opId", opId)
        }

        CieIDSdkLogger.log("sending the challenge signed to the server...", true)

        idpService.callIdpQr(mapValues).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object :
                DisposableSingleObserver<Response<ResponseBody>>() {
                override fun onSuccess(idpResponse: Response<ResponseBody>) {
                    val r = idpResponse.body()?.string()

                    CieIDSdkLogger.log("server reply was: $r", true)

                    if (r != null) {
                        val codice: String

                        try {
                            codice = r.split(":")[1]

                            it.ipzs.cieidsdk.util.utils.updateText(
                                textViewOtpResult,
                                codice,
                                activity
                            )

                            variables.isNfcOn = false
                            if (activity != null) {
                                variables.nfcCore.stopNFCListening()
                            }
                            CieIDSdkLogger.log("codice otp: $codice", true)
                        } catch (e: Exception) {
                            CieIDSdkLogger.log("exception $e", true)
                        }

                    }
                }

                override fun onError(e: Throwable) {
                    CieIDSdkLogger.log(e, true)
                }
            })
    }


}
