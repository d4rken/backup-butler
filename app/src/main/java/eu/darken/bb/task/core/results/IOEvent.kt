package eu.darken.bb.task.core.results

import eu.darken.bb.common.AString
import eu.darken.bb.common.CAString
import eu.darken.bb.common.files.core.APath


data class IOEvent(
        val type: Type,
        val description: AString
) {

    constructor(type: Type, path: APath)
            : this(type, CAString { path.userReadablePath(it) })

    enum class Type {
        BACKUPPED,
        RESTORED
    }
}