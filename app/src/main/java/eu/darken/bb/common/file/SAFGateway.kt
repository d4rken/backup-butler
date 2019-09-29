package eu.darken.bb.common.file

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import eu.darken.bb.App
import eu.darken.bb.common.dagger.AppContext
import timber.log.Timber
import java.io.FileDescriptor
import javax.inject.Inject

class SAFGateway @Inject constructor(
        @AppContext private val context: Context,
        private val contentResolver: ContentResolver
) {
    private fun getDocumentFile(file: SAFPath): DocumentFile? {
        val treeRoot = DocumentFile.fromTreeUri(context, file.treeRoot)
        checkNotNull(treeRoot) { "Couldn't create DocumentFile for: $treeRoot" }

        var current: DocumentFile? = treeRoot
        for (seg in file.crumbs) {
            current = current?.findFile(seg)
            if (current == null) break
        }

        Timber.tag(TAG).v("getDocumentFile(file=$file): ${current?.uri}")
        return current
    }

    fun createFile(path: SAFPath): SAFPath {
        return SAFPath.build(createDocumentFile(FILE_TYPE_DEFAULT, path.treeRoot, path.crumbs))
    }

    fun createDir(path: SAFPath): SAFPath {
        return SAFPath.build(createDocumentFile(DIR_TYPE, path.treeRoot, path.crumbs))
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

    fun listFiles(file: SAFPath): Array<SAFPath>? {
        return getDocumentFile(file)?.listFiles()?.map {
            val name = it.name ?: it.uri.pathSegments.last().split('/').last()
            file.child(name)
        }?.toTypedArray()
    }

    fun exists(path: SAFPath): Boolean {
        return getDocumentFile(path)?.exists() == true
    }

    fun delete(path: SAFPath): Boolean {
        return getDocumentFile(path)?.delete() == true
    }

    fun canWrite(path: SAFPath): Boolean {
        return getDocumentFile(path)?.canWrite() == true
    }

    fun isFile(path: SAFPath): Boolean {
        return getDocumentFile(path)?.isFile == true
    }

    fun isDirectory(path: SAFPath): Boolean {
        return getDocumentFile(path)?.isDirectory == true
    }

    enum class FileMode constructor(val value: String) {
        WRITE("w"), READ("r")
    }

    fun <T> openFile(path: SAFPath, mode: FileMode, action: (FileDescriptor) -> T): T {
        val docFile = getDocumentFile(path)
        checkNotNull(docFile) { "Can't find $path" }

        contentResolver.openFileDescriptor(docFile.uri, mode.value).use { pfd ->
            checkNotNull(pfd) { "Couldn't open $path" }
            val fileDescriptor = pfd.fileDescriptor
            return action.invoke(fileDescriptor)
        }
    }

    fun takePermission(path: SAFPath): Boolean {
        var permissionTaken = false
        try {
            contentResolver.takePersistableUriPermission(path.treeRoot, RW_FLAGSINT)
            permissionTaken = true
        } catch (e: SecurityException) {
            Timber.tag(TAG).e(e, "Failed to take permission")
            try {
                contentResolver.releasePersistableUriPermission(path.treeRoot, RW_FLAGSINT)
            } catch (ignore: SecurityException) {
            }
        }
        return permissionTaken
    }

    fun releasePermission(path: SAFPath): Boolean {
        contentResolver.releasePersistableUriPermission(path.treeRoot, RW_FLAGSINT)
        return true
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