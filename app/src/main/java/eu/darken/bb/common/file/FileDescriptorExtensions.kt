package eu.darken.bb.common.file

import okio.Okio
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream

fun FileDescriptor.copyTo(dest: File) {
    dest.tryMkFile()
    Okio.source(FileInputStream(this)).use { source ->
        Okio.buffer(Okio.sink(FileOutputStream(dest))).use { buffer ->
            buffer.writeAll(source)
        }
    }
}