package eu.darken.bb.common.moshi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import eu.darken.bb.App
import eu.darken.bb.common.file.tryMkFile
import okio.Okio
import timber.log.Timber
import java.io.File

val TAG: String = App.logTag("JsonAdapterExtensions")

fun <T> JsonAdapter<T>.toFile(value: T, file: File) {
    try {
        file.tryMkFile()
        JsonWriter.of(Okio.buffer(Okio.sink(file))).use {
            it.indent = "    "
            toJson(it, value)
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
    } finally {
        Timber.tag(TAG).v("fromFile(file=%s): %s", file, value)
    }
    return value
}