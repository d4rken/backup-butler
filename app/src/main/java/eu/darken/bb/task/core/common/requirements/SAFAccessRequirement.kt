package eu.darken.bb.task.core.common.requirements

import android.content.Context
import android.net.Uri
import eu.darken.bb.R
import eu.darken.bb.common.AString
import eu.darken.bb.common.CAString
import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.common.files.core.saf.SAFPath

data class SAFAccessRequirement(
    override val satisfied: Boolean,
    override val label: AString,
    override val description: AString,
    override val mainActionLabel: AString = CAString(R.string.general_grant_action),
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