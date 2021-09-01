package eu.darken.bb.task.core.results

import eu.darken.bb.common.AString
import eu.darken.bb.common.CAString
import eu.darken.bb.common.files.core.APath


data class LogEvent(
    val type: Type,
    val description: AString
) {

    constructor(type: Type, path: APath)
            : this(type = type, description = CAString { path.userReadablePath(it) })

    constructor(type: Type, description: String)
            : this(type = type, description = CAString(description))

    enum class Type(val value: String) {
        BACKUPPED("backupped"),
        RESTORED("restored");

        companion object {
            fun fromString(value: String) = values().first { it.value == value }
        }
    }
}