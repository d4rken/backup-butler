package eu.darken.bb.common.file.core

import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.InputStream
import java.io.OutputStream

fun InputStream.copyToAutoClose(outputStream: OutputStream) {
    source().buffer().use { source ->
        outputStream.sink().buffer().use { dest ->
            dest.writeAll(source)
        }
    }
}

fun InputStream.copyToAutoClose(file: File) {
    copyToAutoClose(file.outputStream())
}