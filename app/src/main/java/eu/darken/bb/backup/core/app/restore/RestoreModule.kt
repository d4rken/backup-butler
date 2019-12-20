package eu.darken.bb.backup.core.app.restore

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import eu.darken.bb.backup.core.app.restore.modules.PrivateDefaultHandler

@Module
abstract class RestoreModule {

    @Binds
    @IntoSet
    abstract fun privateDataDefault(handler: PrivateDefaultHandler): RestoreHandler

}