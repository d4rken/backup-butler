package eu.darken.bb.common.files.core

import eu.darken.bb.common.HasSharedResource
import okio.Sink
import okio.Source
import java.io.IOException
import java.util.*

interface APathGateway<P : APath, PLU : APathLookup<P>> : HasSharedResource<Any> {

    @Throws(IOException::class)
    suspend fun createDir(path: P): Boolean

    @Throws(IOException::class)
    suspend fun createFile(path: P): Boolean

    @Throws(IOException::class)
    suspend fun lookup(path: P): PLU

    @Throws(IOException::class)
    suspend fun lookupFiles(path: P): List<PLU>

    @Throws(IOException::class)
    suspend fun listFiles(path: P): List<P>

    @Throws(IOException::class)
    suspend fun exists(path: P): Boolean

    @Throws(IOException::class)
    suspend fun canWrite(path: P): Boolean

    @Throws(IOException::class)
    suspend fun canRead(path: P): Boolean

    @Throws(IOException::class)
    suspend fun read(path: P): Source

    @Throws(IOException::class)
    suspend fun write(path: P): Sink

    @Throws(IOException::class)
    suspend fun delete(path: P): Boolean

    @Throws(IOException::class)
    suspend fun createSymlink(linkPath: P, targetPath: P): Boolean

    @Throws(IOException::class)
    suspend fun setModifiedAt(path: P, modifiedAt: Date): Boolean

    @Throws(IOException::class)
    suspend fun setPermissions(path: P, permissions: Permissions): Boolean

    @Throws(IOException::class)
    suspend fun setOwnership(path: P, ownership: Ownership): Boolean
}