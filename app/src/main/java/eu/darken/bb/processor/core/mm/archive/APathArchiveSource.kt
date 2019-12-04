package eu.darken.bb.processor.core.mm.archive

import eu.darken.bb.backup.core.app.AppBackupEndpoint
import eu.darken.bb.common.file.core.APath
import eu.darken.bb.common.file.core.APathGateway
import eu.darken.bb.common.file.core.APathLookup
import eu.darken.bb.common.file.core.SourceWithCallbacks
import eu.darken.bb.processor.core.mm.BaseRefSource
import okio.Buffer
import okio.Source
import okio.buffer
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import timber.log.Timber
import java.io.File

class APathArchiveSource<PathType : APath, GateType : APathGateway<in PathType, out APathLookup<PathType>>>(
        private val gateway: GateType,
        private val archivePath: PathType,
        private val targets: List<APathLookup<PathType>>,
        providedProps: ArchiveProps? = null
) : BaseRefSource() {

    private val autoGenProps: ArchiveProps by lazy {
        ArchiveProps(
                originalPath = archivePath,
                archiveType = "tar",
                compressionType = "gzip"
        )
    }

    override val props: ArchiveProps = providedProps ?: autoGenProps

    override fun doOpen(): Source = if (props.archiveType == "tar" && props.compressionType == "gzip") {
        useTarGz()
    } else {
        throw UnsupportedOperationException("archiveType=${props.archiveType} and compressionType=${props.compressionType} are not supported")
    }

    private fun useTarGz(): Source {
        val buffer = Buffer()
        val out = TarArchiveOutputStream(GzipCompressorOutputStream(buffer.outputStream())).apply {
            setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX)
            setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX)
            setAddPaxHeadersForNonAsciiNames(true)
        }
        out.use {
            for (p in targets) {
                Timber.tag(AppBackupEndpoint.TAG).v("Compressing $p")

                val entry: TarArchiveEntry = when (p.fileType) {
                    APath.FileType.DIRECTORY -> TarArchiveEntry(p.path + File.separator, TarArchiveEntry.LF_DIR)
                    APath.FileType.SYMBOLIC_LINK -> TarArchiveEntry(p.path, TarArchiveEntry.LF_SYMLINK).apply {
                        linkName = p.target!!.path
                    }
                    APath.FileType.FILE -> TarArchiveEntry(p.path, TarArchiveEntry.LF_NORMAL)
                }

                entry.size = p.size
                entry.setModTime(p.modifiedAt.time)

                if (p.permissions != null) {
                    entry.mode = p.permissions!!.mode
                }
                if (p.ownership != null) {
                    entry.setUserId(p.ownership!!.userId)
                    entry.setGroupId(p.ownership!!.groupId)
                }

                out.putArchiveEntry(entry)
                if (p.fileType == APath.FileType.FILE) {
                    gateway.read(p.lookedUp).use {
                        val sourceData = it.buffer().inputStream()
                        sourceData.use { input -> input.copyTo(out) }
                    }
                }
                out.closeArchiveEntry()
            }
        }
        return SourceWithCallbacks(buffer) { out.close() }
    }
}