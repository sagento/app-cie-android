package it.ipzs.cieidsdk.nfc.common

import android.annotation.SuppressLint
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
import it.ipzs.cieidsdk.event.*
import it.ipzs.cieidsdk.exceptions.BlockedPinException
import it.ipzs.cieidsdk.exceptions.NoCieException
import it.ipzs.cieidsdk.exceptions.PinInputNotValidException
import it.ipzs.cieidsdk.exceptions.PinNotValidException
import it.ipzs.cieidsdk.nfc.Ias
import it.ipzs.cieidsdk.util.ActivityType
import it.ipzs.cieidsdk.util.CieIDSdkLogger
import it.ipzs.cieidsdk.util.variables

@SuppressLint("StaticFieldLeak")
class nfcCore : NfcAdapter.ReaderCallback {

    companion object {
        /**
         *  Return true if device has NFC supports
         */
        private fun hasFeatureNFC(): Boolean {
            return variables.activityList.last().context.packageManager.hasSystemFeature(
                PackageManager.FEATURE_NFC
            )
        }

        /**
         *  Return true if NFC is enabled on device
         */
        private fun isNFCEnabled(): Boolean {
            val manager =
                variables.activityList.last().context.getSystemService(Context.NFC_SERVICE) as NfcManager
            val adapter = manager.defaultAdapter
            val enabled = adapter?.isEnabled ?: false

            return hasFeatureNFC() && enabled
        }

        /**
        Open NFC Settings PAge
         */
        fun openNFCSettings() {
            val activity = variables.activityList.last().activity
            activity.startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
        }

        fun detectNfcStatus(

            showToastOnFail: Boolean,
            openSettingsNFC_onFail: Boolean
        ): Boolean {

            return if (isNFCEnabled()) {
                true
            } else {
                if (showToastOnFail) {
                    Toast.makeText(
                        variables.activityList.last().context, "NFC is off.",
                        Toast.LENGTH_LONG
                    ).show()
                }

                if (openSettingsNFC_onFail)
                    openNFCSettings()

                false
            }
        }
    }


    override fun onTagDiscovered(tag: Tag?) {

        val activity = variables.getActivity(ActivityType.NFC)

        try {
            variables.callback.onEvent(Event(EventTag.ON_TAG_DISCOVERED))
            val isoDep = IsoDep.get(tag)
            isoDep.timeout = CieIDSdk.isoDepTimeout
            isoDep.connect()

            if (isoDep.isExtendedLengthApduSupported) {
                CieIDSdkLogger.log("isExtendedLengthApduSupported : true", false)
            } else {
                CieIDSdkLogger.log("isExtendedLengthApduSupported : false", false)
                variables.callback.onEvent(Event(EventSmartphone.EXTENDED_APDU_NOT_SUPPORTED))
                return
            }

            variables.ias = Ias(isoDep)

            val message = "Tag discovered. Mode: " + variables.mode.toString()
            CieIDSdkLogger.log(message, true)



            activity?.runOnUiThread {
                variables.textViewOtpResult?.text = message
            }



            if (variables.mode == OperativeMode.AUTH_IBRIDO) {
                variables.cieIdSdk.loginIbrido(activity)
            } else if (variables.mode == OperativeMode.AUTH_WEBVIEW) {
                variables.cieIdSdk.callWebView()
            }


        } catch (throwable: Throwable) {
            CieIDSdkLogger.log(throwable.toString(), true)

            activity?.runOnUiThread {
                variables.textViewOtpResult?.text = throwable.toString()
            }

            when (throwable) {
                is PinNotValidException -> variables.callback.onEvent(
                    Event(
                        EventCard.ON_PIN_ERROR,
                        throwable.tentativi
                    )
                )
                is PinInputNotValidException -> variables.callback.onEvent(Event(EventError.PIN_INPUT_ERROR))
                is BlockedPinException -> variables.callback.onEvent(Event(EventCard.ON_CARD_PIN_LOCKED))
                is NoCieException -> variables.callback.onEvent(Event(EventTag.ON_TAG_DISCOVERED_NOT_CIE))
                is TagLostException -> variables.callback.onEvent(Event(EventTag.ON_TAG_LOST))
                else -> variables.callback.onError(throwable)
            }
        }
    }


    /**
     * Call on Resume of NFC Activity
     */
    fun startNFCListening() {
        val activity: Activity = variables.getActivity(ActivityType.NFC) ?: return

        try {
            if (!(activity.isFinishing) && variables.isNfcOn) {
                variables.nfcAdapter?.enableReaderMode(
                    activity,
                    this,
                    NfcAdapter.FLAG_READER_NFC_A or
                            NfcAdapter.FLAG_READER_NFC_B or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                    null
                )
            }
        } catch (throwable: Throwable) {
            variables.callback.onEvent(Event(EventError.START_NFC_ERROR))
        }

    }

    /**
     * Call on Pause Of NFC Activity
     */
    fun stopNFCListening() {
        val activity: Activity = variables.getActivity(ActivityType.NFC) ?: return

        try {
            variables.nfcAdapter?.disableReaderMode(activity)

        } catch (throwable: Throwable) {
            variables.callback.onEvent(Event(EventError.STOP_NFC_ERROR))
        }
    }


}