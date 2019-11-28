package eu.darken.bb.common.root.core.javaroot.fileops

import java.io.InputStream

class WrappedInputStream(
        private val inputStream: InputStream,
        private val onPostClosed: () -> Unit
) : InputStream() {

    override fun skip(n: Long): Long {
        return inputStream.skip(n)
    }

    override fun available(): Int {
        return inputStream.available()
    }

    override fun reset() {
        inputStream.reset()
    }

    override fun close() {
        try {
            inputStream.close()
        } finally {
            onPostClosed()
        }
    }

    override fun mark(readlimit: Int) {
        inputStream.mark(readlimit)
    }

    override fun markSupported(): Boolean {
        return inputStream.markSupported()
    }

    override fun read(): Int {
        return inputStream.read()
    }

    override fun read(b: ByteArray?): Int {
        return inputStream.read(b)
    }

    override fun read(b: ByteArray?, off: Int, len: Int): Int {
        return inputStream.read(b, off, len)
    }
}

fun InputStream.wrap(onPostClosed: () -> Unit) = WrappedInputStream(this, onPostClosed)