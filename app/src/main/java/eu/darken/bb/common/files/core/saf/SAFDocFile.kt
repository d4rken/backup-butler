package eu.darken.bb.common.files.core.saf

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.system.Os
import android.system.StructStat
import android.text.TextUtils
import androidx.annotation.RequiresApi
import eu.darken.bb.common.asSequence
import eu.darken.bb.common.files.core.Ownership
import eu.darken.bb.common.files.core.Permissions
import eu.darken.bb.common.files.core.useQuietly
import timber.log.Timber
import java.io.IOException
import java.util.*


@RequiresApi(21)
internal data class SAFDocFile(
    private val context: Context,
    private val resolver: ContentResolver,
    val uri: Uri
) {

    val name: String?
        get() = queryForString(DocumentsContract.Document.COLUMN_DISPLAY_NAME)

    val mimeType: String?
        get() = queryForString(DocumentsContract.Document.COLUMN_MIME_TYPE)

    val isFile: Boolean
        get() {
            val mt = mimeType
            return DocumentsContract.Document.MIME_TYPE_DIR != (mt) && mt?.isNotEmpty() == true
        }

    val isDirectory: Boolean
        get() = mimeType == DocumentsContract.Document.MIME_TYPE_DIR

    val exists: Boolean
        get() = queryForString(DocumentsContract.Document.COLUMN_DOCUMENT_ID) != null

    val writable: Boolean
        get() {
            // Ignore if grant doesn't allow write
            if (context.checkCallingOrSelfUriPermission(
                    uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }

            val mimeTypeCached = mimeType
            val flags: Int = queryForLong(DocumentsContract.Document.COLUMN_FLAGS)?.toInt() ?: 0

            // Ignore documents without MIME
            if (TextUtils.isEmpty(mimeTypeCached)) return false

            // Deletable documents considered writable
            if (flags and DocumentsContract.Document.FLAG_SUPPORTS_DELETE != 0) {
                return true
            }

            if (DocumentsContract.Document.MIME_TYPE_DIR == mimeTypeCached && flags and DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE != 0) {
                // Directories that allow create considered writable
                return true
            } else if (!TextUtils.isEmpty(mimeTypeCached) && flags and DocumentsContract.Document.FLAG_SUPPORTS_WRITE != 0) {
                // Writable normal files considered writable
                return true
            }

            return false
        }

    val readable: Boolean
        get() {
            // Ignore if grant doesn't allow read
            if (context.checkCallingOrSelfUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }

            // Ignore documents without MIME
            if (TextUtils.isEmpty(mimeType)) {
                return false
            }
            return true
        }

    val lastModified: Date
        get() = Date(queryForLong(DocumentsContract.Document.COLUMN_LAST_MODIFIED) ?: 0)

    val length: Long
        get() = queryForLong(DocumentsContract.Document.COLUMN_SIZE) ?: 0


    fun createDirectory(name: String): SAFDocFile {
        return createFile(DocumentsContract.Document.MIME_TYPE_DIR, name)
    }

    fun createFile(mimeType: String, name: String): SAFDocFile {
        val newFileUri = DocumentsContract.createDocument(resolver, uri, mimeType, name)
        requireNotNull(newFileUri) { "createFile(mimeType=$mimeType, name=$name) failed for $uri" }
        return SAFDocFile(context, resolver, newFileUri)
    }

    // https://commonsware.com/blog/2019/11/23/scoped-storage-stories-documentscontract.html
    @SuppressLint("Recycle")
    fun findFile(name: String): SAFDocFile? {
        val childrenUri: Uri =
            DocumentsContract.buildChildDocumentsUriUsingTree(uri, DocumentsContract.getDocumentId(uri))

        val foundUris = resolver.query(
            childrenUri,
            arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME),
            "${DocumentsContract.Document.COLUMN_DISPLAY_NAME}=?",
            arrayOf(name),
            null
        )?.useQuietly { cursor ->
            cursor.asSequence()
                .map { Pair(it.getString(0), it.getString(1)) }
                .toList()
        }

        requireNotNull(foundUris) { "Unable to query for $name in $uri" }

        val pair = foundUris.singleOrNull { it.second == name }
        if (pair == null) return null

        return SAFDocFile(context, resolver, DocumentsContract.buildDocumentUriUsingTree(uri, pair.first))
    }

    @SuppressLint("Recycle") fun listFiles(): List<SAFDocFile> {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri, DocumentsContract.getDocumentId(uri))

        val foundUris = resolver.query(
            childrenUri,
            arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID),
            null,
            null,
            null
        )?.useQuietly { cursor ->
            cursor.asSequence().map { DocumentsContract.buildDocumentUriUsingTree(uri, it.getString(0)) }.toList()
        }

        requireNotNull(foundUris) { "Unable to list files for $uri" }

        return foundUris.map { SAFDocFile(context, resolver, it) }
    }

    fun delete(): Boolean {
        return DocumentsContract.deleteDocument(resolver, uri)
    }

    fun setLastModified(lastModified: Date): Boolean = try {
        val updateValues = ContentValues()
        updateValues.put(DocumentsContract.Document.COLUMN_LAST_MODIFIED, lastModified.time)
        val updated: Int = resolver.update(uri, updateValues, null, null)
        updated == 1
    } catch (e: Exception) {
        Timber.tag(SAFGateway.TAG)
            .w("setLastModified(lastModified=%s) failed on %s, due to %s", lastModified, this, e.toString())
        false
    }

    fun setPermissions(permissions: Permissions): Boolean = openPFD(resolver, FileMode.WRITE).use { pfd ->
        try {
            Os.fchmod(pfd.fileDescriptor, permissions.mode)
            true
        } catch (e: Exception) {
            Timber.tag(SAFGateway.TAG).w(e, "setPermissions(permissions=%s) failed on %s", permissions, this)
            false
        }
    }

    fun setOwnership(ownership: Ownership): Boolean = openPFD(resolver, FileMode.WRITE).use { pfd ->
        try {
            Os.fchown(pfd.fileDescriptor, ownership.userId.toInt(), ownership.groupId.toInt())
            true
        } catch (e: Exception) {
            Timber.tag(SAFGateway.TAG).w(e, "setOwnership(ownership=%s) failed on %s", ownership, this)
            false
        }
    }

    fun fstat(): StructStat? {
        return try {
            val pfd = openPFD(resolver, FileMode.READ)
            pfd.use { Os.fstat(pfd.fileDescriptor) }
        } catch (e: Exception) {
            Timber.tag(SAFGateway.TAG).w(e, "Failed to fstat SAFPath: %s", this)
            null
        }
    }

    fun openPFD(contentResolver: ContentResolver, mode: FileMode): ParcelFileDescriptor {
        val pfd = contentResolver.openFileDescriptor(this.uri, mode.value)
        if (pfd == null) throw IOException("Couldn't open $uri")
        return pfd
    }

    @SuppressLint("Recycle")
    private fun queryForString(column: String): String? {
        return try {
            resolver.query(uri, arrayOf(column), null, null, null).useQuietly { c ->
                if (c != null && c.moveToFirst() && !c.isNull(0)) {
                    c.getString(0)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Timber.tag(SAFGateway.TAG + ":SAFDocFile").w(e, "queryForString(column=%s)", column)
            null
        }
    }

    @SuppressLint("Recycle")
    private fun queryForLong(column: String): Long? {
        return try {
            resolver.query(uri, arrayOf(column), null, null, null).useQuietly { c ->
                if (c != null && c.moveToFirst() && !c.isNull(0)) {
                    c.getLong(0)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Timber.tag(SAFGateway.TAG + ":SAFDocFile").w(e, "queryForLong(column=%s)", column)
            null
        }
    }

    override fun toString(): String {
        return "SAFDocFile(uri=$uri)"
    }

    companion object {
        @RequiresApi(21)
        fun fromTreeUri(context: Context, contentResolver: ContentResolver, treeUri: Uri): SAFDocFile {
            var documentId = DocumentsContract.getTreeDocumentId(treeUri)
            if (DocumentsContract.isDocumentUri(context, treeUri)) {
                documentId = DocumentsContract.getDocumentId(treeUri)
            }
            return SAFDocFile(
                context,
                contentResolver,
                DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
            )
        }
    }
}