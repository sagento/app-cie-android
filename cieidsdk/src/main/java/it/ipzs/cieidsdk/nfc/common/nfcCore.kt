package it.ipzs.cieidsdk.nfc.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.nfc.Tag
import android.nfc.TagLostException
import android.nfc.tech.IsoDep
import android.provider.Settings
import android.widget.Toast
import it.ipzs.cieidsdk.common.CieIDSdk
import it.ipzs.cieidsdk.common.OperativeMode
import it.ipzs.cieidsdk.common.valuesPassed
import it.ipzs.cieidsdk.event.*
import it.ipzs.cieidsdk.exceptions.BlockedPinException
import it.ipzs.cieidsdk.exceptions.NoCieException
import it.ipzs.cieidsdk.exceptions.PinInputNotValidException
import it.ipzs.cieidsdk.exceptions.PinNotValidException
import it.ipzs.cieidsdk.nfc.Ias
import it.ipzs.cieidsdk.util.CieIDSdkLogger

object nfcCore : NfcAdapter.ReaderCallback {

    var valuePassed: valuesPassed? = null
    var isNfcOn: Boolean = false

    override fun onTagDiscovered(tag: Tag?) {
        try {
            CieIDSdk.callback?.onEvent(Event(EventTag.ON_TAG_DISCOVERED))
            val isoDep = IsoDep.get(tag)
            isoDep.timeout = CieIDSdk.isoDepTimeout
            isoDep.connect()

            if (isoDep.isExtendedLengthApduSupported) {
                CieIDSdkLogger.log("isExtendedLengthApduSupported : true", null)
            } else {
                CieIDSdkLogger.log("isExtendedLengthApduSupported : false", null)
                CieIDSdk.callback?.onEvent(Event(EventSmartphone.EXTENDED_APDU_NOT_SUPPORTED))
                return
            }

            CieIDSdk.ias = Ias(isoDep)

            val message = "Tag discovered. Mode: " + CieIDSdk.mode.toString()
            CieIDSdkLogger.log(message, null)

            valuePassed?.getActivity()?.runOnUiThread {
                CieIDSdk.textViewOtpResult?.text = message
            }



            if (CieIDSdk.mode == OperativeMode.AUTH_IBRIDO) {
                CieIDSdk.loginIbrido(null)
            } else if (CieIDSdk.mode == OperativeMode.AUTH_WEBVIEW) {
                CieIDSdk.callWebView(null)
            }


        } catch (throwable: Throwable) {
            CieIDSdkLogger.log(throwable.toString(), null)

            valuePassed?.getActivity()?.runOnUiThread {
                CieIDSdk.textViewOtpResult?.text = throwable.toString()
            }

            when (throwable) {
                is PinNotValidException -> CieIDSdk.callback?.onEvent(
                    Event(
                        EventCard.ON_PIN_ERROR,
                        throwable.tentativi
                    )
                )
                is PinInputNotValidException -> CieIDSdk.callback?.onEvent(Event(EventError.PIN_INPUT_ERROR))
                is BlockedPinException -> CieIDSdk.callback?.onEvent(Event(EventCard.ON_CARD_PIN_LOCKED))
                is NoCieException -> CieIDSdk.callback?.onEvent(Event(EventTag.ON_TAG_DISCOVERED_NOT_CIE))
                is TagLostException -> CieIDSdk.callback?.onEvent(Event(EventTag.ON_TAG_LOST))
                else -> CieIDSdk.callback?.onError(throwable)
            }
        }
    }


    /**
     * Call on Resume of NFC Activity
     */
    fun startNFCListening(activity: Activity) {
        try {
            if (!activity.isFinishing && isNfcOn) {
                CieIDSdk.nfcAdapter?.enableReaderMode(
                    activity,
                    this,
                    NfcAdapter.FLAG_READER_NFC_A or
                            NfcAdapter.FLAG_READER_NFC_B or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                    null
                )
            }
        } catch (throwable: Throwable) {
            CieIDSdk.callback?.onEvent(Event(EventError.START_NFC_ERROR))
        }

    }

    /**
     * Call on Pause Of NFC Activity
     */
    fun stopNFCListening(activity: Activity) {
        try {
            CieIDSdk.nfcAdapter?.disableReaderMode(activity)

        } catch (throwable: Throwable) {
            CieIDSdk.callback?.onEvent(Event(EventError.STOP_NFC_ERROR))
        }
    }

    /**
     *  Return true if device has NFC supports
     */
    private fun hasFeatureNFC(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_NFC)
    }

    /**
     *  Return true if NFC is enabled on device
     */
    private fun isNFCEnabled(context: Context): Boolean {
        val manager = context.getSystemService(Context.NFC_SERVICE) as NfcManager
        val adapter = manager.defaultAdapter
        val enabled = adapter?.isEnabled ?: false

        return hasFeatureNFC(context) && enabled
    }

    /**
    Open NFC Settings PAge
     */
    private fun openNFCSettings(activity: Activity) {
        activity.startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
    }

    fun detectNfcStatus(
        context: Context,
        activity: Activity,
        showToastOnFail: Boolean,
        openSettingsNFC_onFail: Boolean
    ): Boolean {

        return if (isNFCEnabled(context)) {
            true
        } else {
            if (showToastOnFail) {
                Toast.makeText(
                    context, "NFC is off.",
                    Toast.LENGTH_LONG
                ).show()
            }

            if (openSettingsNFC_onFail)
                openNFCSettings(activity)

            false
        }
    }


}