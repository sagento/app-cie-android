package it.app.cie.lib


import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import it.app.cie.activity.pin.askPinActivity
import it.ipzs.cieidsdk.util.variables

class utils {
    companion object {
        const val rubrica_delimiter: String = "--@@#|#@@--"
        const val filename_rubrica: String = "rubrica.txt"


        fun insertPin(

            startForResult: ActivityResultLauncher<Intent>
        ) {


            startForResult.launch(
                Intent(
                    variables.activityList.last().context,
                    askPinActivity::class.java
                )
            )
        }


    }

}

