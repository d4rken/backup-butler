package eu.darken.bb.backup.core.app.backup

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import eu.darken.bb.backup.core.app.backup.handler.PrivateDefaultBackupHandler
import eu.darken.bb.backup.core.app.backup.handler.PublicDefaultBackupHandler

@Module
abstract class BackupHandlerModule {

    @Binds
    @IntoSet
    abstract fun privateDataDefault(handler: PrivateDefaultBackupHandler): BackupHandler


    @Binds
    @IntoSet
    abstract fun publicDefaultHandler(handler: PublicDefaultBackupHandler): BackupHandler
}