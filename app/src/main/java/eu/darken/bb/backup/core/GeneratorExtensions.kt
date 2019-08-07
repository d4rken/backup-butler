package eu.darken.bb.backup.core

import android.content.Intent
import android.os.Bundle
import eu.darken.bb.backup.core.BackupTaskExtensions.CONFIGID_KEY

object BackupTaskExtensions {
    internal const val CONFIGID_KEY = "generator.config.uuid"
}

fun Intent.putGeneratorId(id: Generator.Id) = apply { putExtra(CONFIGID_KEY, id) }

fun Intent.getGeneratorId(): Generator.Id? = getParcelableExtra(CONFIGID_KEY) as Generator.Id?

fun Bundle.putGeneratorId(id: Generator.Id) = apply { putParcelable(CONFIGID_KEY, id) }

fun Bundle.getGeneratorId(): Generator.Id? = getParcelable(CONFIGID_KEY) as Generator.Id?