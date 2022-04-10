package eu.darken.bb.common

import android.webkit.MimeTypeMap
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.asFile
import timber.log.Timber
import java.util.*

object MimeHelper {
    internal val TAG = logTag("MimeHelper")
    const val TYPE_UNSPECIFIC = "*/*"

    private val MIMES = HashMap<String, String>(1 + (66 / 0.75).toInt())

    init {
        MIMES["json"] = "application/json"
        MIMES["js"] = "application/javascript"

        MIMES["list"] = "text/plain"
        MIMES["log"] = "text/plain"
        MIMES["prop"] = "text/plain"
        MIMES["properties"] = "text/plain"
        MIMES["rc"] = "text/plain"
        MIMES["ini"] = "text/plain"
        MIMES["md"] = "text/markdown"

        MIMES["epub"] = "application/epub+zip"
        MIMES["ibooks"] = "application/x-ibooks+zip"

        MIMES["ifb"] = "text/calendar"
        MIMES["eml"] = "message/rfc822"
        MIMES["msg"] = "application/vnd.ms-outlook"

        MIMES["zip"] = "application/zip"
        MIMES["bz"] = "application/x-bzip"
        MIMES["bz2"] = "application/x-bzip2"
        MIMES["cab"] = "application/vnd.ms-cab-compressed"
        MIMES["gz"] = "application/x-gzip"
        MIMES["jar"] = "application/java-archive"
        MIMES["xz"] = "application/x-xz"
        MIMES["Z"] = "application/x-compress"
        MIMES["db"] = "application/octet-stream"

        MIMES["bat"] = "application/x-msdownload"
        MIMES["ksh"] = "text/plain"
        MIMES["sh"] = "application/x-sh"

        MIMES["xif"] = "image/vnd.xiff"
        MIMES["pct"] = "image/x-pict"
        MIMES["pic"] = "image/x-pict"
        MIMES["gif"] = "image/gif"

        MIMES["m2a"] = "audio/mpeg"
        MIMES["m3a"] = "audio/mpeg"
        MIMES["aac"] = "audio/x-aac"
        MIMES["mka"] = "audio/x-matroska"

        MIMES["jpgv"] = "video/jpeg"
        MIMES["jpgm"] = "video/jpm"
        MIMES["jpm"] = "video/jpm"
        MIMES["ogv"] = "video/ogg"
        MIMES["flv"] = "video/x-flv"
        MIMES["mkv"] = "video/x-matroska"
    }

    fun getMime(path: APath): String {
        val fileName = path.name
        var extension = MimeTypeMap.getFileExtensionFromUrl(fileName)

        if (extension.isEmpty()) {
            val index = fileName.lastIndexOf(".")
            if (index != -1) extension = fileName.substring(index + 1)
        }
        extension = extension.lowercase(Locale.ROOT)

        var mimeType: String? = null
        if (path.asFile().isDirectory) mimeType = "vnd.android.document/directory"
        if (mimeType == null) mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        if (mimeType == null) mimeType = MIMES[extension]
        if (mimeType == null) mimeType = TYPE_UNSPECIFIC
        Timber.tag(TAG).d("Resolving extension %s to mimeType=%s", extension, mimeType)
        return mimeType
    }

    fun getMime(paths: Collection<APath>): String? {
        var allType: String? = null
        for (file in paths) {
            val fType = getMime(file)
            if (allType != null && allType != fType) {
                allType = TYPE_UNSPECIFIC
                break
            } else {
                allType = fType
            }
        }
        return allType
    }
}
