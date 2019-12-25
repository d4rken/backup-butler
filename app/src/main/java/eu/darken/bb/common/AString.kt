package eu.darken.bb.common

import android.content.Context
import androidx.annotation.StringRes
import eu.darken.bb.common.files.core.APath

interface AString {
    fun get(context: Context): String

    fun isEmpty(context: Context): Boolean = this == EMPTY || get(context).isEmpty()

    companion object {
        val EMPTY = object : AString {
            override fun get(context: Context): String = ""
        }
    }
}

open class CAString(val resolv: (Context) -> String) : AString {

    constructor(string: String)
            : this({ string })

    constructor(@StringRes stringRes: Int, vararg args: Any)
            : this({ it.getString(stringRes, *args) })

    private lateinit var cache: String

    override fun get(context: Context): String {
        if (::cache.isInitialized) return cache
        synchronized(this) {
            if (!::cache.isInitialized) cache = resolv(context)
            return cache
        }
    }
}

data class PathAString(private val path: APath) : CAString({ path.userReadablePath(it) })