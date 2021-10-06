package eu.darken.bb.task.core.common.requirements

import eu.darken.bb.common.CaString
import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.common.files.core.saf.SAFPath

interface Requirement {

    val type: Type
    val satisfied: Boolean

    val label: CaString
    val description: CaString
    val mainActionLabel: CaString

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