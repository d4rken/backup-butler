package eu.darken.bb.task.ui.editor.restore.config

import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.common.lists.HasStableId
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor

abstract class ConfigUIWrap(
    private val configWrap: SimpleRestoreTaskEditor.ConfigWrap,
    private val configCallback: (Restore.Config, Backup.Id?) -> Unit
) : HasStableId {

    val isCustomConfig: Boolean get() = configWrap.isCustomConfig
    val isDefaultItem: Boolean get() = configWrap.backupInfoOpt == null
    val backupInfo: Backup.Info? get() = configWrap.backupInfoOpt?.info
    val config: Restore.Config get() = configWrap.config

    fun updateConfig(config: Restore.Config) = configCallback.invoke(config, backupInfo?.backupId)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConfigUIWrap

        if (config != other.config) return false
        if (isDefaultItem != other.isDefaultItem) return false
        if (backupInfo != other.backupInfo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = config.hashCode()
        result = 31 * result + isDefaultItem.hashCode()
        result = 31 * result + (backupInfo?.hashCode() ?: 0)
        return result
    }

    override val stableId: Long
        get() = if (isDefaultItem) {
            config.restoreType.hashCode().toLong()
        } else {
            configWrap.backupInfoOpt!!.backupId.hashCode().toLong()
        }

}