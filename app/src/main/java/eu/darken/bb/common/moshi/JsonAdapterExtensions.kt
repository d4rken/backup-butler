package eu.darken.bb.common.moshi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import eu.darken.bb.common.debug.logging.Logging.Priority.*
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.*
import eu.darken.bb.common.files.core.local.tryMkFile
import okio.*
import java.io.File
import java.io.IOException
import java.io.InterruptedIOException

val TAG: String = logTag("JsonAdapterExtensions")

fun <T> JsonAdapter<T>.into(value: T, output: Sink) = try {
    JsonWriter.of(output.buffer()).use {
        it.indent = "    "
        toJson(it, value)
    }
    log(TAG, VERBOSE) { "into(value=$value, output=$output)" }
} catch (e: Exception) {
    if (e !is InterruptedIOException) {
        log(TAG, WARN) { "into(value=$value, output=$output): $e" }
    }
    throw e
}

fun <T> JsonAdapter<T>.from(source: Source): T = try {
    val value = JsonReader.of(source.buffer()).use {
        return@use fromJson(it)
    }
    log(TAG, VERBOSE) { "from(source=$source): $value" }
    value ?: throw IOException("Can't read: $source")
} catch (e: Exception) {
    if (e !is InterruptedIOException) {
        log(TAG, WARN) { "from(source=$source): $e" }
    }
    throw e
}

fun <T> JsonAdapter<T>.toFile(value: T, file: File) = try {
    file.tryMkFile()
    file.sink().use { into(value, it) }
    log(TAG, VERBOSE) { "toFile(value=$value, file=$file)" }
} catch (e: Exception) {
    if (e !is InterruptedIOException) {
        log(TAG, WARN) { "toFile(value=$value, file=$file): $e" }
    }
    throw e
}

fun <T> JsonAdapter<T>.fromFile(file: File): T = try {
    if (!file.exists()) {
        throw ReadException(file)
    }
    val value = file.source().use { from(it) }

    log(TAG, VERBOSE) { "fromFile(file=$file): $value" }
    value ?: throw ReadException(file)
} catch (e: Exception) {
    if (e !is InterruptedIOException) {
        log(TAG, WARN) { "fromFile(file=$file): $e" }
    }
    throw e
}

suspend fun <T, P : APath> JsonAdapter<T>.toAPath(
    value: T,
    gateway: APathGateway<P, out APathLookup<P>>,
    file: P
) = try {
    if (!file.exists(gateway)) {
        gateway.createFile(file)
    }
    gateway.write(file).use {
        into(value, it)
    }
    log(TAG, VERBOSE) { "toAPath(value=$value, gateway=$gateway, file=$file)" }
} catch (e: Exception) {
    if (e !is InterruptedIOException) {
        log(TAG, WARN) { "toAPath(value=$value, gateway=$gateway, file=$file): $e" }
    }
    throw e
}

suspend fun <T, P : APath> JsonAdapter<T>.fromAPath(
    gateway: APathGateway<P, out APathLookup<P>>,
    file: P
): T = try {
    val value = gateway.read(file).use {
        from(it)
    }
    log(TAG, VERBOSE) { "fromAPath(file=$file, safGateway=$gateway): $value" }
    value ?: throw ReadException(file)
} catch (e: Exception) {
    if (e !is InterruptedIOException) {
        log(TAG, WARN) { "fromAPath(file=$file, , safGateway=$gateway): $e" }
    }
    throw e
}