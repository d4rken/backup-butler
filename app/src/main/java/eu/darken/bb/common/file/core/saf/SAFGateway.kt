package eu.darken.bb.common.file.core.saf

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import eu.darken.bb.App
import eu.darken.bb.common.SharedResource
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.file.core.*
import okio.*
import timber.log.Timber
import java.io.IOException
import java.util.*
import javax.inject.Inject

class SAFGateway @Inject constructor(
        @AppContext private val context: Context,
        private val contentResolver: ContentResolver
) : APathGateway<SAFPath, SAFPathLookup> {

    override val resourceTokens = SharedResource<SAFGateway>("${TAG}:SharedResource") { emitter ->
        try {
            emitter.onAvailable(this@SAFGateway)
        } catch (e: Throwable) {
            emitter.onError(e)
        }
    }

    private fun getDocumentFile(file: SAFPath): DocumentFile? {
        val treeRoot = DocumentFile.fromTreeUri(context, file.treeRoot)
        checkNotNull(treeRoot) { "Couldn't create DocumentFile for: $treeRoot" }

        var current: DocumentFile? = treeRoot
        for (seg in file.crumbs) {
            current = current?.findFile(seg)
            if (current == null) break
        }

//       if(BBDebug.isDebug()) Timber.tag(TAG).v("getDocumentFile(file=$file): ${current?.uri}")
        return current
    }

    @Throws(IOException::class)
    override fun createFile(path: SAFPath): Boolean {
        return try {
            createDocumentFile(FILE_TYPE_DEFAULT, path.treeRoot, path.crumbs)
            true
        } catch (e: Exception) {
            Timber.tag(TAG).d(e, "createFile(path=%s) failed", path)
            false
        }
    }

    @Throws(IOException::class)
    override fun createDir(path: SAFPath): Boolean {
        return try {
            createDocumentFile(DIR_TYPE, path.treeRoot, path.crumbs)
            true
        } catch (e: Exception) {
            Timber.tag(TAG).d(e, "createDir(path=%s) failed", path)
            false
        }
    }

    private fun createDocumentFile(mimeType: String, treeUri: Uri, segments: List<String>): DocumentFile {
        val root = DocumentFile.fromTreeUri(context, treeUri)
        checkNotNull(root) { "Couldn't create DocumentFile for: $treeUri" }

        var currentRoot: DocumentFile = root
        for ((index, segName) in segments.withIndex()) {
            if (index < segments.size - 1) {
                val curFile = currentRoot.findFile(segName)
                currentRoot = if (curFile == null) {
                    Timber.tag(TAG).d("$segName doesn't exist in ${currentRoot.uri}, creating.")
                    checkNotNull(currentRoot.createDirectory(segName)) { "Failed to create directory $segName in $currentRoot" }
                } else {
                    Timber.tag(TAG).d("$segName exists in ${currentRoot.uri}.")
                    curFile
                }
            } else {
                val existing = currentRoot.findFile(segName)
                check(existing == null) { "File already exists: ${existing?.uri}" }

                currentRoot = if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
                    checkNotNull(currentRoot.createDirectory(segName)) { "Failed to create final directory $segName in $currentRoot" }
                } else {
                    checkNotNull(currentRoot.createFile(mimeType, segName)) { "Failed to create file $segName in $currentRoot" }
                }

            }
        }
        Timber.tag(TAG).v("createDocumentFile(mimeType=$mimeType, treeUri=$treeUri, crumbs=${segments.toList()}): ${currentRoot.uri}")
        return currentRoot
    }

    @Throws(IOException::class)
    override fun listFiles(path: SAFPath): List<SAFPath> = try {
        getDocumentFile(path)!!
                .listFiles()
                .map {
                    val name = it.name ?: it.uri.pathSegments.last().split('/').last()
                    path.child(name)
                }
    } catch (e: Exception) {
        Timber.tag(TAG).w("lookupFiles(%s) failed.", path)
        throw ReadException(path, cause = e)
    }

    @Throws(IOException::class)
    override fun exists(path: SAFPath): Boolean {
        return getDocumentFile(path)?.exists() == true
    }

    @Throws(IOException::class)
    override fun delete(path: SAFPath): Boolean {
        return getDocumentFile(path)?.delete() == true
    }

    @Throws(IOException::class)
    override fun canWrite(path: SAFPath): Boolean {
        return getDocumentFile(path)?.canWrite() == true
    }

    @Throws(IOException::class)
    override fun canRead(path: SAFPath): Boolean {
        return getDocumentFile(path)?.canRead() == true
    }

    @Throws(IOException::class)
    override fun lookup(path: SAFPath): SAFPathLookup {
        return try {
            val file = getDocumentFile(path)!!
            val fileType: APath.FileType = when {
                file.isDirectory -> APath.FileType.DIRECTORY
                else -> APath.FileType.FILE
            }
            val fstat = file.fstat(contentResolver)

            SAFPathLookup(
                    lookedUp = path,
                    fileType = fileType,
                    modifiedAt = Date(file.lastModified()),
                    createdAt = fstat?.let { Date(it.st_ctime) } ?: Date(),
                    userId = fstat?.st_uid?.toLong() ?: -1L,
                    groupId = fstat?.st_gid?.toLong() ?: -1L,
                    permissions = fstat?.let { APathLookup.Permissions(it.st_mode) } ?: APathLookup.Permissions(-1),
                    size = file.length(),
                    target = null
            )
        } catch (e: Exception) {
            Timber.tag(TAG).w("lookup(%s) failed.", path)
            throw ReadException(path, cause = e)
        }
    }

    override fun lookupFiles(path: SAFPath): List<SAFPathLookup> = try {
        getDocumentFile(path)!!
                .listFiles()
                .map {
                    val name = it.name ?: it.uri.pathSegments.last().split('/').last()
                    path.child(name)
                }
                .map { lookup(it) }
    } catch (e: Exception) {
        Timber.tag(TAG).w("lookupFiles(%s) failed.", path)
        throw ReadException(path, cause = e)
    }

    @Throws(IOException::class)
    override fun read(path: SAFPath): Source {
        val docFile = getDocumentFile(path)
        if (docFile == null) throw ReadException(path)

        val pfd = docFile.openParcelFileDescriptor(contentResolver, FileMode.READ)
        return ParcelFileDescriptor.AutoCloseInputStream(pfd).source().buffer()
    }

    @Throws(IOException::class)
    override fun write(path: SAFPath): Sink {
        val docFile = getDocumentFile(path)
        if (docFile == null) throw WriteException(path)

        val pfd = docFile.openParcelFileDescriptor(contentResolver, FileMode.WRITE)
        return ParcelFileDescriptor.AutoCloseOutputStream(pfd).sink().buffer()
    }

    override fun createSymlink(linkPath: SAFPath, targetPath: SAFPath): Boolean {
        throw UnsupportedOperationException("SAF doesn't support symlinks. createSymlink(linkPath=$linkPath, targetPath=$targetPath)")
    }

    fun takePermission(path: SAFPath): Boolean {
        if (hasPermission(path)) {
            Timber.tag(TAG).d("Already have permission for %s", path)
            return true
        }
        Timber.tag(TAG).d("Taking uri permission for %s", path)
        var permissionTaken = false
        try {
            contentResolver.takePersistableUriPermission(path.treeRoot, RW_FLAGSINT)
            permissionTaken = true
        } catch (e: SecurityException) {
            Timber.tag(TAG).e(e, "Failed to take permission")
            try {
                contentResolver.releasePersistableUriPermission(path.treeRoot, RW_FLAGSINT)
            } catch (e2: SecurityException) {
                Timber.tag(TAG).e(e2, "Error while releasing during error...")
            }
        }
        printCurrentPermissions()
        return permissionTaken
    }

    fun releasePermission(path: SAFPath): Boolean {
        Timber.tag(TAG).d("Releasing uri permission for %s", path)
        contentResolver.releasePersistableUriPermission(path.treeRoot, RW_FLAGSINT)
        printCurrentPermissions()
        return true
    }

    private fun printCurrentPermissions() {
        val current = getPermissions()
        Timber.tag(TAG).d("Now holding %d permissions.", current.size)
        for (p in current) {
            Timber.tag(TAG).d("#%d: %s", current.indexOf(p), p)
        }
    }

    fun getPermissions(): Collection<SAFPath> {
        return contentResolver.persistedUriPermissions.map { SAFPath.build(it.uri) }
    }

    fun hasPermission(path: SAFPath): Boolean {
        return getPermissions().contains(path)
    }

    fun createPickerIntent(): Intent {
        val requestIntent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        requestIntent.putExtra("android.content.extra.SHOW_ADVANCED", true)
        return requestIntent
    }

    fun isStorageRoot(path: SAFPath): Boolean {
        return path.crumbs.isEmpty() && path.treeRoot.pathSegments[1].split(":").filter { it.isNotEmpty() }.size == 1
    }

    companion object {
        val TAG = App.logTag("SAF", "Gateway")

        const val RW_FLAGSINT = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        private const val DIR_TYPE: String = DocumentsContract.Document.MIME_TYPE_DIR
        private const val FILE_TYPE_DEFAULT: String = "application/octet-stream"

        fun isTreeUri(uri: Uri): Boolean {
            val paths = uri.pathSegments
            return paths.size >= 2 && "tree" == paths[0]
        }
    }
}