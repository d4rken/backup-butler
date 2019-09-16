package eu.darken.bb.storage.core.saf

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import eu.darken.bb.App
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.file.AFile
import eu.darken.bb.common.file.SAFFile
import timber.log.Timber
import java.io.FileDescriptor
import javax.inject.Inject

class SAFGateway @Inject constructor(
        @AppContext private val context: Context,
        private val contentResolver: ContentResolver
) {
    private fun getDocumentFile(file: SAFFile): DocumentFile? {
        val treeRoot = DocumentFile.fromTreeUri(context, file.treeRoot)
        checkNotNull(treeRoot) { "Couldn't create DocumentFile for: $treeRoot" }

        var current: DocumentFile? = treeRoot
        for (seg in file.segments) {
            current = current?.findFile(seg)
            if (current == null) break
        }

        Timber.tag(TAG).v("getDocumentFile(file=$file): ${current?.uri}")
        return current
    }

    fun create(file: SAFFile): SAFFile {
        val documentFile = createDocumentFile(file.mimeType, file.treeRoot, file.segments)
        return SAFFile.build(documentFile)
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
        Timber.tag(TAG).v("createDocumentFile(mimeType=$mimeType, treeUri=$treeUri, segments=${segments.toList()}): ${currentRoot.uri}")
        return currentRoot
    }

    fun listFiles(file: SAFFile): List<SAFFile>? {
        return getDocumentFile(file)?.listFiles()?.map { SAFFile.build(it) }
    }

    fun exists(file: SAFFile): Boolean {
        return getDocumentFile(file)?.exists() == true
    }

    fun delete(file: SAFFile): Boolean {
        return getDocumentFile(file)?.delete() == true
    }

    fun canWrite(file: SAFFile): Boolean {
        return getDocumentFile(file)?.canWrite() == true
    }

    fun isDirectory(file: SAFFile): Boolean {
        return getDocumentFile(file)?.isDirectory == true
    }

    enum class FileMode constructor(val value: String) {
        WRITE("w"), READ("r")
    }

    fun <T> openFile(file: SAFFile, mode: FileMode, action: (FileDescriptor) -> T): T {
        val docFile = getDocumentFile(file)
        checkNotNull(docFile) { "Can't find $file" }

        contentResolver.openFileDescriptor(docFile.uri, mode.value).use { pfd ->
            checkNotNull(pfd) { "Couldn't open $file" }
            val fileDescriptor = pfd.fileDescriptor
            return action.invoke(fileDescriptor)
        }
    }

    fun takePermission(file: SAFFile): Boolean {
        var permissionTaken = false
        try {
            contentResolver.takePersistableUriPermission(file.treeRoot, RW_FLAGSINT)
            permissionTaken = true
        } catch (e: SecurityException) {
            Timber.tag(TAG).e(e, "Failed to take permission")
            try {
                contentResolver.releasePersistableUriPermission(file.treeRoot, RW_FLAGSINT)
            } catch (ignore: SecurityException) {
            }
        }
        return permissionTaken
    }

    fun releasePermission(file: SAFFile): Boolean {
        contentResolver.releasePersistableUriPermission(file.treeRoot, RW_FLAGSINT)
        return true
    }

    fun getPermissions(): Collection<SAFFile> {
        return contentResolver.persistedUriPermissions.map { SAFFile.build(AFile.Type.DIRECTORY, it.uri) }
    }

    fun hasPermission(path: SAFFile): Boolean {
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

        fun isTreeUri(uri: Uri): Boolean {
            val paths = uri.pathSegments
            return paths.size >= 2 && "tree" == paths[0]
        }
    }
}