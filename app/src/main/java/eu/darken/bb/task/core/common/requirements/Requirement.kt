package eu.darken.bb.task.core.common.requirements

import eu.darken.bb.common.AString
import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.common.files.core.saf.SAFPath

interface Requirement {

    val type: Type
    val satisfied: Boolean

    val label: AString
    val description: AString
    val mainActionLabel: AString

    enum class Type {
        PERMISSION,
        SAF_ACCESS
    }

    interface Permission : Requirement {
        val permission: String
    }

    interface SAFAccess : Requirement {
        val localTarget: LocalPath
        val safTarget: SAFPath
    }

}