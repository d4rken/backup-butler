package eu.darken.bb.backup.core.app.backup

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import eu.darken.bb.backup.core.app.backup.handler.PrivateDefaultBackupHandler

@Module
abstract class BackupHandlerModule {

    @Binds
    @IntoSet
    abstract fun privateDataDefault(handler: PrivateDefaultBackupHandler): BackupHandler

}