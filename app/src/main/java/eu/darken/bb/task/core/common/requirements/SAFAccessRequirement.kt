package eu.darken.bb.task.core.common.requirements

import android.content.Context
import android.net.Uri
import eu.darken.bb.R
import eu.darken.bb.common.CaString
import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.common.files.core.saf.SAFPath
import eu.darken.bb.common.toCaString

data class SAFAccessRequirement(
    override val satisfied: Boolean,
    override val label: CaString,
    override val description: CaString,
    override val mainActionLabel: CaString = R.string.general_grant_action.toCaString(),
    override val localTarget: LocalPath,
    override val safTarget: SAFPath

) : Requirement.SAFAccess {
    override val type: Requirement.Type = Requirement.Type.SAF_ACCESS

    companion object {
        fun createReadWriteRequirement(context: Context, uri: Uri): Requirement {
            TODO()
        }
    }
}