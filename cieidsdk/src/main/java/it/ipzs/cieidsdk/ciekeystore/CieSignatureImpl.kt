package it.ipzs.cieidsdk.ciekeystore

import it.ipzs.cieidsdk.util.CieIDSdkLogger
import it.ipzs.cieidsdk.util.variables
import java.security.*


internal open class CieSignatureImpl : SignatureSpi() {
    private var byteToSign: ByteArray = byteArrayOf()


    @Throws(InvalidKeyException::class)
    override fun engineInitVerify(publicKey: PublicKey) {
    }

    @Throws(InvalidKeyException::class)
    override fun engineInitSign(privateKey: PrivateKey) {
        byteToSign = byteArrayOf()

    }

    @Throws(SignatureException::class)
    override fun engineUpdate(b: Byte) {
    }

    @Throws(SignatureException::class)
    override fun engineUpdate(bytes: ByteArray?, off: Int, len: Int) {
        if (bytes != null) {
            byteToSign += bytes
        }

    }

    @Throws(NullPointerException::class)
    override fun engineSign(): ByteArray {

        return variables.ias.sign(byteToSign)

    }

    override fun engineVerify(bytes: ByteArray): Boolean {
        return false
    }

    @Throws(InvalidParameterException::class)
    override fun engineSetParameter(s: String, o: Any) {
    }

    @Throws(InvalidParameterException::class)
    override fun engineGetParameter(s: String): Any? {
        return null
    }

    class None : CieSignatureImpl() {
        init {
            CieIDSdkLogger.log("CieSignatureImpl NONE", true)
        }
    }
}
