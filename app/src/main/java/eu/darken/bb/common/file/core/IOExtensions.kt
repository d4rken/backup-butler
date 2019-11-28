package eu.darken.bb.common.file.core

import okio.Sink
import okio.Source
import okio.buffer
import okio.sink
import java.io.File

fun Source.copyToAutoClose(output: Sink) {
    buffer().use { source ->
        output.buffer().use { dest ->
            dest.writeAll(source)
        }
    }
}

fun Source.copyToAutoClose(file: File) {
    copyToAutoClose(file.sink())
}