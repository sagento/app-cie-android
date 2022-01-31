package it.ipzs.cieidsdk.nfc

internal class ApduResponse {
    class SwError {
        companion object {
            const val SW_WRONG_P1P2 = "6b00"
            const val SW_END_OF_FILE = "6282"
            const val SW_NO_ERROR = "9000"
        }
    }

    var response: ByteArray = byteArrayOf()
    var swByte: ByteArray = byteArrayOf()

    val swShort: String
        @Throws(Exception::class)
        get() = bytesToHex(this.swByte)
    val swInt: Int
        @Throws(Exception::class)
        get() = AppUtil.toUint(this.swByte)


    @Throws(Exception::class)
    constructor(fullResponse: ByteArray) {
        this.response = fullResponse.copyOfRange(0, fullResponse.size - 2)
        this.swByte = fullResponse.copyOfRange(fullResponse.size - 2, fullResponse.size)
    }

    @Throws(Exception::class)
    constructor(res: ByteArray, sw: ByteArray) {
        this.response = res
        this.swByte = sw
    }

    @Throws(Exception::class)
    private fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder(bytes.size * 2)
        for (i in bytes.indices) {
            sb.append(String.format("%02x", bytes[i]))
        }
        return sb.toString()
    }

}
