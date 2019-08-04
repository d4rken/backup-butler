package eu.darken.bb.backups.core

import android.content.Intent
import android.os.Bundle
import eu.darken.bb.backups.core.BackupTaskExtensions.CONFIGID_KEY
import java.util.*

object BackupTaskExtensions {
    internal const val CONFIGID_KEY = "generator.config.uuid"
}

fun Intent.putGeneratorId(uuid: UUID) = apply { putExtra(CONFIGID_KEY, uuid) }

fun Intent.getGeneratorId(): UUID? = getSerializableExtra(CONFIGID_KEY) as UUID?

fun Bundle.putGeneratorId(uuid: UUID) = apply { putSerializable(CONFIGID_KEY, uuid) }

fun Bundle.getGeneratorId(): UUID? = getSerializable(CONFIGID_KEY) as UUID?