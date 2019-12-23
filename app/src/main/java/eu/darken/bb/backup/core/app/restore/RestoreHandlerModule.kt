package eu.darken.bb.backup.core.app.restore

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import eu.darken.bb.backup.core.app.restore.handler.PrivateDefaultRestoreHandler

@Module
abstract class RestoreHandlerModule {

    @Binds
    @IntoSet
    abstract fun privateDataDefault(handler: PrivateDefaultRestoreHandler): RestoreHandler

}