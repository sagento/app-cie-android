package it.app.cie.lib


import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import it.app.cie.activity.pin.askPinActivity
import it.ipzs.cieidsdk.common.valuesPassed

class utils {
    companion object {
        const val rubrica_delimiter: String = "--@@#|#@@--"
        const val filename_rubrica: String = "rubrica.txt"

        fun insertPin(
            valuesPassed: valuesPassed,
            startForResult: ActivityResultLauncher<Intent>
        ) {

            askPinActivity.context = valuesPassed.getContext()

            startForResult.launch(Intent(valuesPassed.getContext(), askPinActivity::class.java))


        }


    }

}

