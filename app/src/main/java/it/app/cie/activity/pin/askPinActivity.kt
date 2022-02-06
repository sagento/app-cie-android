package it.app.cie.activity.pin

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import it.app.cie.R
import it.app.cie.lib.utils
import it.ipzs.cieidsdk.util.ActivityInfo
import it.ipzs.cieidsdk.util.ActivityType
import it.ipzs.cieidsdk.util.CieIDSdkLogger
import it.ipzs.cieidsdk.util.variables
import it.ipzs.cieidsdk.util.variables.Companion.rubrica
import java.io.File


class askPinActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ask_pin)
        variables.activityList.add(ActivityInfo(this, ActivityType.PIN, this))

        val button: Button = findViewById(R.id.button_askpin_continua)
        button.setOnClickListener {
            clickedButton()
        }

        val buttonLoadRubrica: Button = findViewById(R.id.button_pin_carica)
        buttonLoadRubrica.setOnClickListener { loadRubrica() }

        val buttonSalvaRubrica: Button = findViewById(R.id.button_pin_salva)
        buttonSalvaRubrica.setOnClickListener { salvaRubrica() }

        val buttonSvuotaRubrica: Button = findViewById(R.id.button_svuotarubrica)
        buttonSvuotaRubrica.setOnClickListener { svuotaRubrica() }
    }

    private fun svuotaRubrica() {

        val dialogClickListener =
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {

                        try {
                            val file = File(this.filesDir, utils.filename_rubrica)
                            file.delete()
                        } catch (e: Exception) {
                            CieIDSdkLogger.log(e, true)
                        }

                        rubrica = null

                    }
                    DialogInterface.BUTTON_NEGATIVE -> {}
                }
            }

        val builder = AlertDialog.Builder(this)
        builder.setMessage("Sei sicuro di svuotare la rubrica?")
            .setPositiveButton("Sì", dialogClickListener)
            .setNegativeButton("No", dialogClickListener).show()


    }

    private fun salvaRubrica() {

        if (rubrica == null)
            rubrica = hashMapOf()

        val textEditPin: EditText = findViewById(R.id.editTextNumber_pin)
        val textEditNomeRubrica: EditText = findViewById(R.id.edittext_nome_rubrica)
        if (textEditNomeRubrica.text.toString().isNotEmpty()
            && textEditPin.text.toString().length == 8
        ) {
            rubrica?.put(textEditNomeRubrica.text.toString(), textEditPin.text.toString())
        }

        try {
            val filename = utils.filename_rubrica
            variables.activityList.last().context.openFileOutput(filename, Context.MODE_PRIVATE)
                .use {

                    if (it != null) {
                        val whatToWrite = getWhatToWrite()
                        for (line in whatToWrite) {
                            it.write(line.toByteArray())
                            it.write("\n".toByteArray())
                        }
                    }

                }

        } catch (e: Exception) {
            CieIDSdkLogger.log(e, true)
        }
    }


    private fun getWhatToWrite(): MutableList<String> {
        val list: MutableList<String> = mutableListOf()
        if (rubrica != null) {
            for (key in rubrica!!.keys) {
                val line = key + utils.rubrica_delimiter + rubrica!![key]
                list.add(line)
            }
        }

        return list
    }

    private fun loadRubrica() {
        try {
            val file = File(this.filesDir, utils.filename_rubrica)
            val lines = file.readLines()
            rubrica = HashMap()
            for (line in lines) {
                if (line.isNotEmpty()) {
                    val lineExtracted: Pair<String, String> = extract(line)
                    if (rubrica?.containsKey(lineExtracted.first) == true) {
                        rubrica?.set(lineExtracted.first, lineExtracted.second)
                    } else {
                        rubrica?.put(lineExtracted.first, lineExtracted.second)
                    }
                }
            }
        } catch (e: Exception) {
            CieIDSdkLogger.log(e, true)
        }

        val array: Array<String>? = rubrica?.keys?.toList()?.toTypedArray()
        if (array != null && array.isNotEmpty()) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Scegli")
            builder.setItems(array) { _, which ->
                // the user clicked on array[which]
                val key = array[which]
                val value = rubrica?.get(key)

                val textEditPin: EditText = findViewById(R.id.editTextNumber_pin)
                textEditPin.setText(value)

                val textEditNomeRubrica: EditText = findViewById(R.id.edittext_nome_rubrica)
                textEditNomeRubrica.setText(key)
            }
            builder.show()
        }
    }

    private fun extract(line: String): Pair<String, String> {
        val split = line.split(utils.rubrica_delimiter)
        return Pair(split[0].trim(), split[1].trim())
    }

    private fun clickedButton() {
        val textEdit: EditText = findViewById(R.id.editTextNumber_pin)
        val value = textEdit.text?.toString()
        if (value != null && value.length == 8) {
            variables.ciePin = value

            variables.activityList.removeLast()
            setResult(Activity.RESULT_OK)
            this.finish()
        }
    }
}