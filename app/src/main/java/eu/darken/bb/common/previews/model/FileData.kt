package eu.darken.bb.common.previews.model

import android.content.res.Resources
import eu.darken.bb.common.files.core.APathLookup

data class FileData(val file: APathLookup<*>, val type: Type, val theme: Resources.Theme? = null) {
    enum class Type {
        IMAGE, VIDEO, APK, FALLBACK, MUSIC
    }
}
