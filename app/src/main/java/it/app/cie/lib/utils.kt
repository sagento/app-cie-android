package it.app.cie.lib

import android.content.Intent
import it.app.cie.activity.pin.askPinActivity
import it.ipzs.cieidsdk.common.valuesPassed

class utils {
    companion object{
        val rubrica_delimiter: String = "--@@#|#@@--"
        val filename_rubrica: String = "rubrica.txt"

        fun insertPin(kFunction1: (valuesPassed) -> Boolean, valuesPassed: valuesPassed) {

            askPinActivity.valuesPassed = valuesPassed
            askPinActivity.functionToRun = kFunction1
            val intent = Intent(valuesPassed.getContext(), askPinActivity::class.java)
            valuesPassed.getActivity()?.startActivity(intent)
        }

    }
}