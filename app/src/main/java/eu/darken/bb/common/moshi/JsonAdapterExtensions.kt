package eu.darken.bb.common.moshi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import eu.darken.bb.App
import eu.darken.bb.common.ReadException
import eu.darken.bb.common.file.core.local.tryMkFile
import eu.darken.bb.common.file.core.saf.SAFGateway
import eu.darken.bb.common.file.core.saf.SAFPath
import okio.buffer
import okio.sink
import okio.source
import timber.log.Timber
import java.io.*

val TAG: String = App.logTag("JsonAdapterExtensions")

fun <T> JsonAdapter<T>.toFile(value: T, file: File) {
    try {
        file.tryMkFile()
        JsonWriter.of(file.sink().buffer()).use {
            it.indent = "    "
            toJson(it, value)
        }
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
        val value = JsonReader.of(file.source().buffer()).use {
            return@use fromJson(it)
        }
        Timber.tag(TAG).v("fromFile(file=%s): %s", file, value)
        return value ?: throw ReadException(file)
    } catch (e: Exception) {
        if (e !is InterruptedIOException) {
            Timber.w("fromFile(file=%s)", file)
        }
        throw e
    }
}

fun <T> JsonAdapter<T>.toFileDescriptor(value: T, fileDescriptor: FileDescriptor) {
    try {
        JsonWriter.of(FileOutputStream(fileDescriptor).sink().buffer()).use {
            it.indent = "    "
            toJson(it, value)
        }
        Timber.tag(TAG).v("toFile(value=%s, fileDescriptor=%s)", value, fileDescriptor)
    } catch (e: Exception) {
        if (e !is InterruptedIOException) {
            Timber.w(e, "toFileDescriptor(value=%s, fileDescriptor=%s)", value, fileDescriptor)
        }
        throw e
    }
}

fun <T> JsonAdapter<T>.fromFileDescriptor(fileDescriptor: FileDescriptor): T? {
    try {
        val value = JsonReader.of(FileInputStream(fileDescriptor).source().buffer()).use {
            return@use fromJson(it)
        }
        Timber.tag(TAG).v("fromFileDescriptor(fileDescriptor=%s): %s", fileDescriptor, value)
        return value
    } catch (e: Exception) {
        if (e !is InterruptedIOException) {
            Timber.w(e, "fromFileDescriptor(fileDescriptor=%s)", fileDescriptor)
        }
        throw e
    }
}

fun <T> JsonAdapter<T>.toSAFFile(value: T, safGateway: SAFGateway, file: SAFPath) {
    if (!safGateway.exists(file)) {
        safGateway.createFile(file)
    }
    return safGateway.openFile(file, SAFGateway.FileMode.WRITE) {
        this.toFileDescriptor(value, it)
    }
}

fun <T> JsonAdapter<T>.fromSAFFile(safGateway: SAFGateway, file: SAFPath): T {
    try {
        val value = safGateway.openFile(file, SAFGateway.FileMode.READ) {
            this.fromFileDescriptor(it)
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