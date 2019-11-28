package eu.darken.bb.common.file.core

import eu.darken.bb.common.SharedResource
import okio.Sink
import okio.Source
import java.io.IOException

interface APathGateway<P : APath, PLU : APathLookup<P>> {

    val resourceTokens: SharedResource<out APathGateway<P, PLU>>

    @Throws(IOException::class)
    fun createDir(path: P): Boolean

    @Throws(IOException::class)
    fun createFile(path: P): Boolean

    @Throws(IOException::class)
    fun lookup(path: P): PLU

    @Throws(IOException::class)
    fun lookupFiles(path: P): List<PLU>

    @Throws(IOException::class)
    fun listFiles(path: P): List<P>

    @Throws(IOException::class)
    fun exists(path: P): Boolean

    @Throws(IOException::class)
    fun canWrite(path: P): Boolean

    @Throws(IOException::class)
    fun canRead(path: P): Boolean

    @Throws(IOException::class)
    fun read(path: P): Source

    @Throws(IOException::class)
    fun write(path: P): Sink

    @Throws(IOException::class)
    fun delete(path: P): Boolean

}