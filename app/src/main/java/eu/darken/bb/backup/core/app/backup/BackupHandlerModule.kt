package eu.darken.bb.backup.core.app.backup

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.multibindings.IntoSet
import eu.darken.bb.backup.core.app.backup.handler.PrivateDefaultBackupHandler
import eu.darken.bb.backup.core.app.backup.handler.PublicDefaultBackupHandler

@Module
@InstallIn(ServiceComponent::class)
abstract class BackupHandlerModule {

    @Binds
    @IntoSet
    abstract fun privateDataDefault(handler: PrivateDefaultBackupHandler): BackupHandler

    @Binds
    @IntoSet
    abstract fun publicDefaultHandler(handler: PublicDefaultBackupHandler): BackupHandler
}