package eu.darken.bb.common

import eu.darken.bb.common.debug.logging.logTag
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object CheckSummer {
    internal val TAG = logTag("CheckSummer")
    internal const val HEXES = "0123456789ABCDEF"

    enum class Type constructor(internal val algo: String) {
        MD5("MD5"), SHA1("SHA-1"), SHA256("SHA-256"), BADALGO("badalgo")
    }

    private fun getHex(raw: ByteArray?): String? {
        if (raw == null) return null

        val hex = StringBuilder(2 * raw.size)
        for (b in raw) {
            hex.append(HEXES[b.toInt() and 0xF0 shr 4]).append(HEXES[b.toInt() and 0x0F])
        }
        return hex.toString()
    }

    fun calculate(string: String, type: Type): String {
        val digest: MessageDigest
        try {
            digest = MessageDigest.getInstance(type.algo)
        } catch (e: NoSuchAlgorithmException) {
            Timber.tag(TAG).e(e, "MessageDigest doesn't have %s", type)
            throw UnsupportedOperationException("Checksum type ${type.algo} is not supported")
        }

        digest.update(string.toByteArray())
        return getHex(digest.digest())!!
    }

    @Throws(IOException::class, UnsupportedOperationException::class)
    fun calculate(file: File, type: Type): String? {
        val start = System.currentTimeMillis()
        if (!file.isFile) return null

        val digest: MessageDigest
        try {
            digest = MessageDigest.getInstance(type.algo)
        } catch (e: NoSuchAlgorithmException) {
            Timber.tag(TAG).e(e, "MessageDigest doesn't have %s", type)
            throw UnsupportedOperationException("Checksum type ${type.algo} is not supported")
        }

        val fis = FileInputStream(file)
        val buffer = ByteArray(1024)
        var numRead: Int
        do {
            numRead = fis.read(buffer)
            if (numRead > 0) digest.update(buffer, 0, numRead)
        } while (numRead != -1)
        fis.close()

        val result = getHex(digest.digest())

        val stop = System.currentTimeMillis()
        Timber.tag(TAG).v("%s CHECKSUM is %s (%dms) for '%s'", type, result, stop - start, file)
        return result
    }

}