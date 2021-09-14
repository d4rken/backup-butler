package eu.darken.bb.processor.core.mm.archive

import eu.darken.bb.App
import eu.darken.bb.common.files.core.*
import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.processor.core.mm.BaseRefSource
import eu.darken.bb.processor.core.mm.Props
import eu.darken.bb.processor.core.mm.generic.DirectoryProps
import eu.darken.bb.processor.core.mm.generic.FileProps
import eu.darken.bb.processor.core.mm.generic.SymlinkProps
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

open class ArchiveRefSource(
    private val sourceGenerator: (ArchiveProps) -> Source,
    propGenerator: () -> ArchiveProps
) : BaseRefSource(), ArchiveRef {

    constructor(
        gateway: GatewaySwitch,
        label: String?,
        archivePath: APath,
        targets: List<APathLookup<APath>>
    ) : this(
        sourceGenerator = genArchive(gateway, targets),
        propGenerator = genProps(label, archivePath)
    )

    override val props: ArchiveProps by lazy { propGenerator() }

    override fun doOpen(): Source {
        Timber.tag(TAG).v("Opening source for %s", props)
        return sourceGenerator(props)
    }

    override fun openArchive(): Sequence<Pair<Props, Source?>> = sequence {
        val archiveInputStream = if (props.archiveType == "tar" && props.compressionType == "gzip") {
            TarArchiveInputStream(GzipCompressorInputStream(open().buffer().inputStream()))
        } else {
            throw UnsupportedOperationException("archiveType=${props.archiveType} and compressionType=${props.compressionType} are not supported")
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
                yield(Pair(props, dataStream))
            }
        }
    }

    companion object {
        val TAG = App.logTag("MMDataRepo", "MMRef", "ArchiveRefSource")

        internal fun genProps(label: String?, archivePath: APath): () -> ArchiveProps {
            return {
                ArchiveProps(
                    label = label,
                    originalPath = if (archivePath is APathLookup<*>) archivePath.lookedUp as APath? else archivePath,
                    archiveType = "tar",
                    compressionType = "gzip"
                )
            }
        }

        internal fun <PathType : APath, GateType : APathGateway<in PathType, out APathLookup<PathType>>> genArchive(
            gateway: GateType,
            targets: List<APathLookup<PathType>>
        ): (ArchiveProps) -> Source = genfun@{ props ->
            val pipe = Pipe(8192)
            val out = TarArchiveOutputStream(GzipCompressorOutputStream(pipe.sink.buffer().outputStream())).apply {
                setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX)
                setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX)
                setAddPaxHeadersForNonAsciiNames(true)
            }
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
                                gateway.read(p.lookedUp).use {
                                    it.buffer().inputStream().copyTo(out)
                                }
                            }
                            out.closeArchiveEntry()
                        }
                    }
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "Archiving of $props failed")
                }
            }
            return@genfun SourceWithCallbacks(pipe.source) { out.close() }
        }
    }
}