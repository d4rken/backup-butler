package eu.darken.bb.common.previews

import android.content.Context
import android.content.res.Resources
import eu.darken.bb.common.file.core.APath


data class FilePreviewRequest(val file: APath, val theme: Resources.Theme? = null) {
    constructor(file: APath, context: Context) : this(file, context.theme)
}

