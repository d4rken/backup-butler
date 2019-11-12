package eu.darken.bb.common.file.core.local

import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream

fun FileDescriptor.copyTo(dest: File) {
    dest.tryMkFile()
    FileInputStream(this).source().use { source ->
        FileOutputStream(dest).sink().buffer().use { buffer ->
            buffer.writeAll(source)
        }
    }
}