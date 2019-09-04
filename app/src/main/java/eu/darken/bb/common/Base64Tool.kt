package eu.darken.bb.common

import android.util.Base64

object Base64Tool {

    fun encode(value: String): String {
        val data: ByteArray = value.toByteArray(Charsets.UTF_8)
        return Base64.encodeToString(data, Base64.NO_WRAP)
    }

    fun decode(value: String): String {
        val decodedBytes = Base64.decode(value, Base64.NO_WRAP)
        return String(decodedBytes, Charsets.UTF_8)
    }

}