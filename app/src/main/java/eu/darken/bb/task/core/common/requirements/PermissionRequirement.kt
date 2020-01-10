package eu.darken.bb.task.core.common.requirements

import android.Manifest
import android.content.Context
import androidx.core.content.PermissionChecker
import eu.darken.bb.R
import eu.darken.bb.common.AString
import eu.darken.bb.common.CAString

data class PermissionRequirement(
        override val satisfied: Boolean,
        override val label: AString,
        override val description: AString,
        override val mainActionLabel: AString = CAString(R.string.general_grant_action),
        override val permission: String
) : Requirement.Permission {
    override val type: Requirement.Type = Requirement.Type.PERMISSION

    companion object {
        fun createStorageReq(context: Context): Requirement {
            val perm = Manifest.permission.WRITE_EXTERNAL_STORAGE
            return PermissionRequirement(
                    satisfied = PermissionChecker.checkSelfPermission(context, perm) == PermissionChecker.PERMISSION_GRANTED,
                    label = CAString(R.string.permission_write_storage_label),
                    description = CAString(R.string.permission_write_storage_desc),
                    permission = perm
            )
        }
    }
}