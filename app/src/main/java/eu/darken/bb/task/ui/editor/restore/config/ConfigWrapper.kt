package eu.darken.bb.task.ui.editor.restore.config

import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.common.lists.HasStableId

data class ConfigWrapper(
        val config: Restore.Config?,
        val backupInfo: Backup.InfoOpt? = null,
        val isCustomConfig: Boolean,
        val callback: (Restore.Config, Backup.Id?) -> Unit
) : HasStableId {
    constructor(config: Restore.Config, callback: (Restore.Config, Backup.Id?) -> Unit)
            : this(config, null, true, callback)

    val isDefaultItem: Boolean = backupInfo == null

    fun updateConfig(config: Restore.Config) = callback.invoke(config, backupInfo?.backupId)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConfigWrapper

        if (config != other.config) return false
        if (isDefaultItem != other.isDefaultItem) return false
        if (backupInfo?.info != other.backupInfo?.info) return false

        return true
    }

    override fun hashCode(): Int {
        var result = config?.hashCode() ?: 0
        result = 31 * result + isDefaultItem.hashCode()
        result = 31 * result + (backupInfo?.info?.hashCode() ?: 0)
        return result
    }

    override val stableId: Long
        get() = if (isDefaultItem) {
            config!!.restoreType.hashCode().toLong()
        } else {
            backupInfo!!.backupId.hashCode().toLong()
        }

}