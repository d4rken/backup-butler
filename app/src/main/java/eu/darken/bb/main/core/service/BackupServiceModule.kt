package eu.darken.bb.main.core.service

import dagger.Module
import eu.darken.bb.backup.core.BackupModule


@Module(includes = [BackupModule::class])
class BackupServiceModule