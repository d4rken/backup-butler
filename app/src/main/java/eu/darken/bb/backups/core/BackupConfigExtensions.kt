package eu.darken.bb.backups.core

import android.content.Intent
import android.os.Bundle
import eu.darken.bb.backups.core.BackupTaskExtensions.CONFIGID_KEY
import java.util.*

object BackupTaskExtensions {
    internal const val CONFIGID_KEY = "backupconfig.uuid"
}

fun Intent.putConfigId(uuid: UUID) = apply { putExtra(CONFIGID_KEY, uuid) }

fun Intent.getConfigId(): UUID? = getSerializableExtra(CONFIGID_KEY) as UUID?

fun Bundle.putConfigId(uuid: UUID) = apply { putSerializable(CONFIGID_KEY, uuid) }

fun Bundle.getConfigId(): UUID? = getSerializable(CONFIGID_KEY) as UUID?