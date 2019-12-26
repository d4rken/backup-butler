package eu.darken.bb.backup.core.app.restore

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import eu.darken.bb.backup.core.app.restore.handler.PrivateDefaultRestoreHandler
import eu.darken.bb.backup.core.app.restore.handler.PublicDefaultRestoreHandler

@Module
abstract class RestoreHandlerModule {

    @Binds
    @IntoSet
    abstract fun privateDefault(handler: PrivateDefaultRestoreHandler): RestoreHandler

    @Binds
    @IntoSet
    abstract fun publicDefault(handler: PublicDefaultRestoreHandler): RestoreHandler
}