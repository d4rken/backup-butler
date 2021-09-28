package eu.darken.bb.common.moshi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.ReadException
import eu.darken.bb.common.files.core.local.tryMkFile
import eu.darken.bb.common.files.core.saf.SAFGateway
import eu.darken.bb.common.files.core.saf.SAFPath
import okio.*
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.io.InterruptedIOException

val TAG: String = logTag("JsonAdapterExtensions")

fun <T> JsonAdapter<T>.into(value: T, output: Sink) {
    try {
        JsonWriter.of(output.buffer()).use {
            it.indent = "    "
            toJson(it, value)
        }
    } catch (e: Exception) {
        if (e !is InterruptedIOException) {
            Timber.w("into(value=%s, output=%s)", value, output)
        }
        throw e
    } finally {
        Timber.tag(TAG).v("into(value=%s, output=%s)", value, output)
    }
}

fun <T> JsonAdapter<T>.from(source: Source): T {
    try {

        val value = JsonReader.of(source.buffer()).use {
            return@use fromJson(it)
        }
        Timber.tag(TAG).v("from(source=%s): %s", source, value)
        return value ?: throw IOException("Can't read: $source")
    } catch (e: Exception) {
        if (e !is InterruptedIOException) {
            Timber.w("from(source=%s)", source)
        }
        throw e
    }
}

fun <T> JsonAdapter<T>.toFile(value: T, file: File) {
    try {
        file.tryMkFile()
        file.sink().use { into(value, it) }
    } catch (e: Exception) {
        if (e !is InterruptedIOException) {
            Timber.w("toFile(value=%s, file=%s)", value, file)
        }
        throw e
    } finally {
        Timber.tag(TAG).v("toFile(value=%s, file=%s)", value, file)
    }
}

fun <T> JsonAdapter<T>.fromFile(file: File): T {
    try {
        if (!file.exists()) {
            throw ReadException(file)
        }
        val value = file.source().use { from(it) }
        Timber.tag(TAG).v("fromFile(file=%s): %s", file, value)
        return value ?: throw ReadException(file)
    } catch (e: Exception) {
        if (e !is InterruptedIOException) {
            Timber.w("fromFile(file=%s)", file)
        }
        throw e
    }
}

fun <T> JsonAdapter<T>.toSAFFile(value: T, safGateway: SAFGateway, file: SAFPath) {
    if (!file.exists(safGateway)) {
        safGateway.createFile(file)
    }
    return safGateway.write(file).use {
        into(value, it)
    }
}

fun <T> JsonAdapter<T>.fromSAFFile(safGateway: SAFGateway, file: SAFPath): T {
    try {
        val value = safGateway.read(file).use {
            from(it)
        }
        Timber.tag(TAG).v("fromSAFFile(file=%s): %s", file, value)
        return value ?: throw ReadException(file)
    } catch (e: Exception) {
        if (e !is InterruptedIOException) {
            Timber.w("fromSAFFile(file=%s)", file)
        }
        throw e
    }
}