package eu.darken.bb.common.file.core

import java.io.IOException

interface APathGateway<P : APath, PLU : APathLookup<P>> {
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

}