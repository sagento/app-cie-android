package it.app.cie.activity.pin

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import it.app.cie.R
import it.app.cie.lib.utils
import it.ipzs.cieidsdk.common.CieIDSdk
import it.ipzs.cieidsdk.common.valuesPassed
import it.ipzs.cieidsdk.util.CieIDSdkLogger
import java.io.File


class askPinActivity : AppCompatActivity() {

    companion object {
        var functionToRun: ((valuesPassed) -> Boolean)? = null
        var valuesPassed: valuesPassed? = null
        var rubrica: HashMap<String, String>? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ask_pin)

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
        try {
            val file = File(this.filesDir, utils.filename_rubrica)
            file.delete()
        } catch (e: Exception) {
            CieIDSdkLogger.log(e, this)
        }

        rubrica = null
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
            valuesPassed?.getContext()?.openFileOutput(filename, Context.MODE_PRIVATE).use {

                if (it != null) {
                    val whatToWrite = getWhatToWrite()
                    for (line in whatToWrite) {
                        it.write(line.toByteArray())
                        it.write("\n".toByteArray())
                    }
                }

            }

        } catch (e: Exception) {
            CieIDSdkLogger.log(e, this)
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
            CieIDSdkLogger.log(e, this)
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
        val textEdit: EditText = findViewById<EditText>(R.id.editTextNumber_pin)
        val value = textEdit.text?.toString()
        if (value != null && value.length == 8) {
            CieIDSdk.pin = value

            this.finish()

            if (functionToRun != null && valuesPassed != null) {
                functionToRun?.invoke(valuesPassed ?: return)
            }
        }
    }
}