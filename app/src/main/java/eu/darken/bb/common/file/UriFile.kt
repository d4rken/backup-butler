package eu.darken.bb.common.file

import android.net.Uri

data class UriFile(
        override val type: SFile.Type,
        val uri: Uri
) : SFile {

    override val pathType: SFile.SFileType = SFile.SFileType.JAVA

    override val path: String
        get() = uri.path

    override val name: String
        get() = uri.lastPathSegment

    override fun toString(): String = "UriFile(uri=$uri)"

    companion object {
        fun build(type: SFile.Type, uri: Uri): UriFile {

            return UriFile(type, uri)
        }
    }
}