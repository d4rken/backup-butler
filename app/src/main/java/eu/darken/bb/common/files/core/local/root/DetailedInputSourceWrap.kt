package eu.darken.bb.common.files.core.local.root

import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.common.files.core.remoteInputStream
import okio.Source

data class DetailedInputSourceWrap(
    val path: LocalPath,
    val input: Source,
    val length: Long = -1
) : DetailedInputSource.Stub() {

    override fun path(): LocalPath = path

    override fun input(): RemoteInputStream = input.remoteInputStream()

    override fun length(): Long = length

}