package eu.darken.bb.task.core.common.requirements

import android.Manifest
import android.content.Context
import androidx.core.content.PermissionChecker
import eu.darken.bb.R
import eu.darken.bb.common.CaString
import eu.darken.bb.common.toCaString

data class PermissionRequirement(
    override val satisfied: Boolean,
    override val label: CaString,
    override val description: CaString,
    override val mainActionLabel: CaString = R.string.general_grant_action.toCaString(),
    override val permission: String
) : Requirement.Permission {
    override val type: Requirement.Type = Requirement.Type.PERMISSION

    companion object {
        fun createStorageReq(context: Context): Requirement {
            val perm = Manifest.permission.WRITE_EXTERNAL_STORAGE
            return PermissionRequirement(
                satisfied = PermissionChecker.checkSelfPermission(
                    context,
                    perm
                ) == PermissionChecker.PERMISSION_GRANTED,
                label = R.string.permission_write_storage_label.toCaString(),
                description = R.string.permission_write_storage_desc.toCaString(),
                permission = perm
            )
        }
    }
}