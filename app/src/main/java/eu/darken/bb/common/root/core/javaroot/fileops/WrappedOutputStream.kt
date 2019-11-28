package eu.darken.bb.common.root.core.javaroot.fileops

import java.io.OutputStream

class WrappedOutputStream(
        private val stream: OutputStream,
        private val onPostClosed: () -> Unit
) : OutputStream() {

    override fun write(b: Int) {
        stream.write(b)
    }

    override fun write(b: ByteArray?) {
        stream.write(b)
    }

    override fun write(b: ByteArray?, off: Int, len: Int) {
        stream.write(b, off, len)
    }

    override fun flush() {
        stream.flush()
    }

    override fun close() {
        try {
            stream.close()
        } finally {
            onPostClosed()
        }
    }

}

fun OutputStream.wrap(onPostClosed: () -> Unit) = WrappedOutputStream(this, onPostClosed)