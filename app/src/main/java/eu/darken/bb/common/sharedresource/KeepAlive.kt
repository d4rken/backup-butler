package eu.darken.bb.common.sharedresource

import java.io.Closeable

interface KeepAlive : Closeable {
    val isClosed: Boolean

    override fun close()
}