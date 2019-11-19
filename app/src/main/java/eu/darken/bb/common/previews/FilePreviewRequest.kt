package eu.darken.bb.common.previews

import android.content.Context
import android.content.res.Resources
import eu.darken.bb.common.file.core.APathLookup


data class FilePreviewRequest(val file: APathLookup<*>, val theme: Resources.Theme? = null) {
    constructor(file: APathLookup<*>, context: Context) : this(file, context.theme)
}

