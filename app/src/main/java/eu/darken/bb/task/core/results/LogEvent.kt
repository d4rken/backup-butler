package eu.darken.bb.task.core.results

import eu.darken.bb.common.CaString
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.toCaString


data class LogEvent(
    val type: Type,
    val description: CaString
) {

    constructor(type: Type, path: APath)
        : this(type = type, description = path.toCaString())

    constructor(type: Type, description: String)
        : this(type = type, description = description.toCaString())

    enum class Type(val value: String) {
        BACKUPPED("backupped"),
        RESTORED("restored");

        companion object {
            fun fromString(value: String) = values().first { it.value == value }
        }
    }
}