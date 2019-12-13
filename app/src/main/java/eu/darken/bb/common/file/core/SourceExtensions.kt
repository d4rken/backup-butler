package eu.darken.bb.common.file.core

import eu.darken.bb.common.root.core.javaroot.fileops.RemoteInputStream
import eu.darken.bb.common.root.core.javaroot.fileops.remoteInputStream
import okio.Source
import okio.buffer
import java.io.InputStream

fun Source.inputStream(): InputStream {
    return buffer().inputStream()
}

fun Source.remoteInputStream(): RemoteInputStream {
    return inputStream().remoteInputStream()
}