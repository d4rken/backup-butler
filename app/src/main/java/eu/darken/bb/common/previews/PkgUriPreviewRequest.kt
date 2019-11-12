package eu.darken.bb.common.previews

import android.content.res.Resources
import android.net.Uri


data class PkgUriPreviewRequest(val uri: Uri, val theme: Resources.Theme? = null)