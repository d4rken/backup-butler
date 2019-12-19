package eu.darken.bb.processor.core.mm.archive

import eu.darken.bb.common.files.core.Ownership
import eu.darken.bb.common.files.core.Permissions
import eu.darken.bb.common.files.core.constrain
import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.processor.core.mm.BaseRefSource
import eu.darken.bb.processor.core.mm.DirectoryProps
import eu.darken.bb.processor.core.mm.Props
import eu.darken.bb.processor.core.mm.SymlinkProps
import eu.darken.bb.processor.core.mm.file.FileProps
import okio.Source
import okio.buffer
import okio.source
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.File


class FileArchiveSource(
        private val archive: File,
        override val props: ArchiveProps
) : BaseRefSource() {

    override fun doOpen(): Source {
        return archive.source()
    }

    fun openArchive(): Sequence<Pair<Props, Source?>> = sequence {
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
                yield(Pair(props, dataStream))
            }
        }
    }
}