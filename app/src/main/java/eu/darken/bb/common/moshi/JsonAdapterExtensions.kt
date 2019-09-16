package eu.darken.bb.common.moshi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import eu.darken.bb.App
import eu.darken.bb.common.file.SAFFile
import eu.darken.bb.common.file.tryMkFile
import eu.darken.bb.storage.core.saf.SAFGateway
import okio.Okio
import timber.log.Timber
import java.io.*

val TAG: String = App.logTag("JsonAdapterExtensions")

fun <T> JsonAdapter<T>.toFile(value: T, file: File) {
    try {
        file.tryMkFile()
        JsonWriter.of(Okio.buffer(Okio.sink(file))).use {
            it.indent = "    "
            toJson(it, value)
        }
    } catch (e: Exception) {
        if (e is InterruptedIOException) {
            Timber.w("Interrupted! toFile(value=%s, file=%s)", value, file)
        } else {
            throw e
        }
    } finally {
        Timber.tag(TAG).v("toFile(value=%s, file=%s)", value, file)
    }
}

fun <T> JsonAdapter<T>.fromFile(file: File): T? {
    var value: T? = null
    try {
        if (file.exists()) {
            value = JsonReader.of(Okio.buffer(Okio.source(file))).use {
                return@use fromJson(it)
            }
        }
    } catch (e: Exception) {
        if (e is InterruptedIOException) {
            Timber.w("Interrupted! fromFile(value=%s, file=%s)", value, file)
        } else {
            throw e
        }
    } finally {
        Timber.tag(TAG).v("fromFile(file=%s): %s", file, value)
    }
    return value
}

fun <T> JsonAdapter<T>.toFileDescriptor(value: T, fileDescriptor: FileDescriptor) {
    try {
        JsonWriter.of(Okio.buffer(Okio.sink(FileOutputStream(fileDescriptor)))).use {
            it.indent = "    "
            toJson(it, value)
        }
    } catch (e: Exception) {
        if (e is InterruptedIOException) {
            Timber.w("Interrupted! toFile(value=%s, fileDescriptor=%s)", value, fileDescriptor)
        } else {
            throw e
        }
    } finally {
        Timber.tag(TAG).v("toFile(value=%s, fileDescriptor=%s)", value, fileDescriptor)
    }
}

fun <T> JsonAdapter<T>.fromFileDescriptor(fileDescriptor: FileDescriptor): T? {
    var value: T? = null
    try {
        value = JsonReader.of(Okio.buffer(Okio.source(FileInputStream(fileDescriptor)))).use {
            return@use fromJson(it)
        }
    } catch (e: Exception) {
        if (e is InterruptedIOException) {
            Timber.w("Interrupted! fromFile(value=%s, fileDescriptor=%s)", value, fileDescriptor)
        } else {
            throw e
        }
    } finally {
        Timber.tag(TAG).v("fromFile(fileDescriptor=%s): %s", fileDescriptor, value)
    }
    return value
}

fun <T> JsonAdapter<T>.toSAFFile(value: T, safGateway: SAFGateway, file: SAFFile) {
    if (!safGateway.exists(file)) {
        safGateway.create(file)
    }
    return safGateway.openFile(file, SAFGateway.FileMode.WRITE) {
        this.toFileDescriptor(value, it)
    }
}

fun <T> JsonAdapter<T>.fromSAFFile(safGateway: SAFGateway, file: SAFFile): T? {
    return safGateway.openFile(file, SAFGateway.FileMode.READ) {
        this.fromFileDescriptor(it)
    }
}