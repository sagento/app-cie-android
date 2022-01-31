package it.ipzs.cieidsdk.common

/**
 * https://docs.italia.it/italia/cie/cie-manuale-tecnico-docs/it/master/_images/img_MSC_CIE_ibrido.png
 * Punto 26. Il padding fa parte di ciò che la funzione sign deve prendere in input.
 */


class Padding {
    companion object {
        val padding: ByteArray = byteArrayOf(
            48, 49.toByte(), 48, 13, 6, 9, 96, 134.toByte(), 72, 1, 101, 3, 4, 2, 1, 5, 0, 4, 32
        )
    }
}