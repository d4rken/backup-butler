package eu.darken.bb.common.previews.model

import android.content.res.Resources
import eu.darken.bb.common.file.core.APath

data class FileData(val file: APath, val type: Type, val theme: Resources.Theme? = null) {
    enum class Type {
        IMAGE, VIDEO, APK, FALLBACK, MUSIC
    }
}
