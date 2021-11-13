package eu.darken.bb.processor.core.mm.archive

import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.*
import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.processor.core.mm.BaseRefSource
import eu.darken.bb.processor.core.mm.Props
import eu.darken.bb.processor.core.mm.generic.DirectoryProps
import eu.darken.bb.processor.core.mm.generic.FileProps
import eu.darken.bb.processor.core.mm.generic.SymlinkProps
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.Pipe
import okio.Source
import okio.buffer
import okio.source
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import timber.log.Timber
import java.io.File
import kotlin.concurrent.thread

class ArchiveRefSource(
    private val sourceGenerator: suspend (ArchiveProps) -> Source,
    private val propGenerator: suspend () -> ArchiveProps
) : BaseRefSource(), ArchiveRef {

    // TODO add caching
    override suspend fun getProps(): ArchiveProps = propGenerator()

    override suspend fun doOpen(): Source {
        Timber.tag(TAG).v("Opening source for %s", getProps())
        return sourceGenerator(getProps())
    }

    override suspend fun extract(): Flow<Pair<Props, Source?>> = flow {
        val archiveInputStream = if (getProps().archiveType == "tar" && getProps().compressionType == "gzip") {
            TarArchiveInputStream(GzipCompressorInputStream(open().buffer().inputStream()))
        } else {
            throw UnsupportedOperationException("archiveType=${getProps().archiveType} and compressionType=${getProps().compressionType} are not supported")
        }.also { resources.add(it) }

        archiveInputStream.use {
            var iter: TarArchiveEntry?
            var lastDataStream: Source? = null
            while (archiveInputStream.nextTarEntry.also { iter = it } != null) {
                val entry = iter as TarArchiveEntry
                var dataStream: Source?
                lateinit var props: Props
                if (entry.isDirectory) {
                    props = DirectoryProps(
                        originalPath = LocalPath.build(entry.name),
                        modifiedAt = entry.modTime,
                        permissions = Permissions(entry.mode),
                        ownership = Ownership(entry.longUserId, entry.longGroupId)
                    )
                    dataStream = null
                } else {
                    props = if (entry.isSymbolicLink) {
                        SymlinkProps(
                            originalPath = LocalPath.build(entry.name),
                            modifiedAt = entry.modTime,
                            ownership = Ownership(entry.longUserId, entry.longGroupId),
                            symlinkTarget = LocalPath.build(entry.linkName)
                        )
                    } else {
                        FileProps(
                            originalPath = LocalPath.build(entry.name),
                            modifiedAt = entry.modTime,
                            permissions = Permissions(entry.mode),
                            ownership = Ownership(entry.longUserId, entry.longGroupId)
                        )
                    }
                    dataStream = archiveInputStream.source().buffer().constrain(entry.size)
                }
                lastDataStream?.close()
                lastDataStream = dataStream

                Timber.tag(TAG).v("Compressing ${entry.name}")
                emit(Pair(props, dataStream))
            }
        }
    }

    companion object {
        val TAG = logTag("MMDataRepo", "MMRef", "ArchiveRefSource")

        fun create(
            gateway: GatewaySwitch,
            label: String?,
            archivePath: APath,
            targets: List<APathLookup<APath>>
        ) = ArchiveRefSource(
            sourceGenerator = genArchive(gateway, targets),
            propGenerator = genProps(label, archivePath)
        )

        private fun genProps(label: String?, archivePath: APath): suspend () -> ArchiveProps = {
            ArchiveProps(
                label = label,
                originalPath = if (archivePath is APathLookup<*>) archivePath.lookedUp as APath? else archivePath,
                archiveType = "tar",
                compressionType = "gzip"
            )
        }

        private fun <PathType : APath, GateType : APathGateway<in PathType, out APathLookup<PathType>>> genArchive(
            gateway: GateType,
            targets: List<APathLookup<PathType>>
        ): suspend (ArchiveProps) -> Source = genfun@{ props ->
            val pipe = Pipe(8192)
            val out = TarArchiveOutputStream(GzipCompressorOutputStream(pipe.sink.buffer().outputStream())).apply {
                setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX)
                setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX)
                setAddPaxHeadersForNonAsciiNames(true)
            }

            // TODO Can we make this more kotlinish?
            thread(name = "Archiver Thread") {
                try {
                    out.use {
                        for (p in targets) {
                            Timber.tag(TAG).v("Compressing $p")

                            val relativePath =
                                LocalPath.build(p.path).relativeTo(LocalPath.build(props.originalPath!!.path))?.path
                            requireNotNull(relativePath) { "Relative path is null for $p and $props" }

                            val entry: TarArchiveEntry = when (p.fileType) {
                                FileType.DIRECTORY -> TarArchiveEntry(
                                    relativePath + File.separator,
                                    TarArchiveEntry.LF_DIR
                                )
                                FileType.SYMBOLIC_LINK -> TarArchiveEntry(
                                    relativePath,
                                    TarArchiveEntry.LF_SYMLINK
                                ).apply {
                                    linkName = p.target!!.path
                                }
                                FileType.FILE -> TarArchiveEntry(relativePath, TarArchiveEntry.LF_NORMAL).apply {
                                    size = p.size
                                }
                            }
                            requireNotNull(entry.name) { "Name was null for $p" }

                            entry.setModTime(p.modifiedAt.time)

                            if (p.permissions != null) {
                                entry.mode = p.permissions!!.mode
                            }

                            p.ownership?.let {
                                entry.userId = it.userId.toInt() // TODO hmmmm
                                entry.groupId = it.groupId.toInt() // TODO hmmm
                                entry.userName = it.userName ?: ""
                                entry.groupName = it.groupName ?: ""
                            }

                            try {
                                out.putArchiveEntry(entry)
                            } catch (e: Exception) {
                                Timber.tag(TAG).e(e, "Failed to write archive entry: ${entry.toHumanReadableString()}")
                                throw e
                            }
                            if (p.fileType == FileType.FILE) {
                                runBlocking {
                                    gateway.read(p.lookedUp).use {
                                        it.buffer().inputStream().copyTo(out)
                                    }
                                }
                            }
                            out.closeArchiveEntry()
                        }
                    }
                } catch (e: Exception) {
                    // TODO should we rethrow to caller somehow?
                    Timber.tag(TAG).e(e, "Archiving of $props failed")
                }
            }
            return@genfun SourceWithCallbacks(pipe.source) { out.close() }
        }
    }
}